package org.mastodon.mamut.linking.trackastra;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_SOURCE;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_WINDOW_SIZE;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;

import java.lang.invoke.MethodHandles;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.util.Cast;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apposed.appose.Appose;
import org.apposed.appose.BuildException;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.apposed.appose.TaskException;
import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.detection.PythonRuntimeException;
import org.mastodon.mamut.linking.trackastra.appose.computation.LinkPrediction;
import org.mastodon.mamut.linking.trackastra.appose.computation.RegionPropsComputation;
import org.mastodon.mamut.linking.trackastra.appose.types.RegionProps;
import org.mastodon.mamut.linking.trackastra.appose.types.SingleTimepointRegionProps;
import org.mastodon.mamut.util.ResourceUtils;
import org.mastodon.mamut.util.appose.ApposeUtils;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.linking.graph.AbstractGraphParticleLinkerOp;
import org.mastodon.tracking.linking.graph.GraphParticleLinkerOp;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;

@Plugin( type = GraphParticleLinkerOp.class )
public class TrackastraLinker< V extends Vertex< E > & HasTimepoint & RealLocalizable & Ref< V >, E extends Edge< V > >
		extends AbstractGraphParticleLinkerOp< V, E >
		implements Benchmark
{

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private long processingTime;

	private boolean confirmEnvInstallation = true;

	@Override
	public void mutate1( final ReadOnlyGraph< V, E > graph, final SpatioTemporalIndex< V > index )
	{
		long start = System.currentTimeMillis();
		statusService.clearStatus();
		statusService.showStatus( "Trackastra linking." );
		Environment environment;
		try
		{
			environment = prepareEnvironment();
		}
		catch ( BuildException e )
		{
			ok = false;
			errorMessage = "Failed to prepare Trackastra environment: " + e.getMessage();
			log.error( errorMessage, e );
			return;
		}
		if ( environment == null )
			return;
		try (Service python = environment.python())
		{
			if ( isWindows() )
				python.init(
						"import numpy\nimport trackastra.data.wrfeat\nimport trackastra.utils\nimport trackastra.model.pretrained as pretrained\nfrom trackastra.model import Trackastra\nimport trackastra.model.predict as predict\n" );
			String importScripts = ResourceUtils
					.readResourceAsString( "org/mastodon/mamut/linking/trackastra/appose/region_props_imports.py", getClass() );
			Service.Task importTask = python.task( importScripts, "main" );
			importTask.waitFor();
			List< SingleTimepointRegionProps > regionProps = computeRegionProps( index, python );
			if ( !isCanceled() )
			{
				importScripts = ResourceUtils
						.readResourceAsString( "org/mastodon/mamut/linking/trackastra/appose/link_prediction_imports.py", getClass() );
				importTask = python.task( importScripts, "main" );
				importTask.waitFor();
				runLinkPrediction( index, regionProps, python );
			}
			ok = true;
			statusService.clearStatus();
		}
		catch ( TrackastraLinkingException | TaskException e )
		{
			Throwable cause = e.getCause();
			String msg = "";
			if ( cause != null )
				msg = cause.getMessage();

			log.error( "Error during Trackastra Linking: {}. Cause: {}.", StringUtils.defaultString( e.getMessage(), e.toString() ), msg );
			ok = false;
			errorMessage = e.getMessage() + ( ( msg != null && !msg.isEmpty() ) ? " Caused by: " + msg : "" );
		}
		catch ( InterruptedException e )
		{
			Thread.currentThread().interrupt();
			throw new PythonRuntimeException( e );
		}
		finally
		{
			processingTime = System.currentTimeMillis() - start;
		}
	}

	private List< SingleTimepointRegionProps > computeRegionProps( final SpatioTemporalIndex< V > index, final Service python )
			throws TrackastraLinkingException
	{
		log.info( "Computing region props for Trackastra" );
		String model = ( ( TrackastraModel ) settings.get( TrackastraUtils.KEY_MODEL ) ).getName();
		int windowSize = ( Integer ) settings.get( KEY_WINDOW_SIZE );
		int minTimepoint = ( int ) settings.get( KEY_MIN_TIMEPOINT );
		int maxTimepoint = ( int ) settings.get( KEY_MAX_TIMEPOINT );
		int level = ( int ) settings.get( KEY_LEVEL );
		Source< ? > source = ( Source< ? > ) settings.get( KEY_SOURCE );

		log.info( "Source: {}", source );

		int timeRange = maxTimepoint - minTimepoint + 1;
		if ( windowSize > timeRange )
			throw new IllegalArgumentException(
					String.format( "Window size (%d) exceeds time range (%d). Adjust window size or time range.", windowSize, timeRange ) );

		try
		{
			RegionPropsComputation computation = new RegionPropsComputation( logger, model, this, statusService, python );
			return computation.computeRegionPropsForSource( source, level, Cast.unchecked( index ), minTimepoint, maxTimepoint );
		}
		catch ( Exception e )
		{
			throw new TrackastraLinkingException( "Failed to compute region props", e );
		}
	}

	private void runLinkPrediction( final SpatioTemporalIndex< V > index, final List< SingleTimepointRegionProps > regionProps,
			final Service python )
			throws TrackastraLinkingException
	{
		log.info( "Performing Trackastra link prediction" );
		try (RegionProps props = new RegionProps( regionProps ))
		{
			LinkPrediction prediction = new LinkPrediction( settings, Cast.unchecked( index ), Cast.unchecked( edgeCreator ), props, logger,
					this, statusService, python );
			prediction.predictAndCreateLinks();
		}
		catch ( Exception e )
		{
			throw new TrackastraLinkingException( "Failed to perform link prediction", e );
		}
	}

	private static boolean isWindows()
	{
		String os = System.getProperty( "os.name" ).toLowerCase();
		return os.contains( "win" );
	}

	/**
	 * Prepares and returns the Appose environment required for Trackastra execution.
	 * Handles optional environment existence checking and consolidates duplicated build code.
	 */
	private Environment prepareEnvironment() throws BuildException
	{
		if ( confirmEnvInstallation )
		{
			Pair< Boolean, String > check = ApposeUtils.confirmEnvInstallation( TrackastraUtils.ENV_NAME, logger );
			if ( !Boolean.TRUE.equals( check.getKey() ) )
			{
				ok = false;
				errorMessage = check.getValue();
				return null;
			}
		}
		return buildEnvironment();
	}

	/**
	 * Builds an Appose environment using the Trackastra environment.yml descriptor.
	 */
	private Environment buildEnvironment() throws BuildException
	{
		return Appose.mamba().scheme( "environment.yml" ).content( TrackastraUtils.ENV_FILE_CONTENT ).logDebug()
				.subscribeProgress( ( title, cur, max ) -> log.info( "{}: {}/{}", title, cur, max ) ).subscribeOutput( log::info )
				.subscribeError( log::error ).build();
	}

	public void setConfirmEnvInstallation( final boolean confirmEnvInstallation )
	{
		this.confirmEnvInstallation = confirmEnvInstallation;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public boolean isSuccessful()
	{
		return ok;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
