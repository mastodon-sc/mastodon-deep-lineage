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
import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.linking.trackastra.appose.computation.LinkPrediction;
import org.mastodon.mamut.linking.trackastra.appose.types.RegionProps;
import org.mastodon.mamut.linking.trackastra.appose.computation.RegionPropsComputation;
import org.mastodon.mamut.linking.trackastra.appose.types.SingleTimepointRegionProps;
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

	@Override
	public void mutate1( final ReadOnlyGraph< V, E > graph, final SpatioTemporalIndex< V > index )
	{
		long start = System.currentTimeMillis();
		try
		{
			List< SingleTimepointRegionProps > regionProps = computeRegionProps( index );
			runLinkPrediction( index, regionProps );
			ok = true;
		}
		catch ( TrackastraLinkingException e )
		{
			log.error( "Error during Trackastra Linking: {}", StringUtils.defaultString( e.getMessage(), e.toString() ) );
			ok = false;
			errorMessage = e.getMessage();
		}
		finally
		{
			processingTime = System.currentTimeMillis() - start;
		}
	}

	private List< SingleTimepointRegionProps > computeRegionProps( final SpatioTemporalIndex< V > index ) throws TrackastraLinkingException
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

		try (RegionPropsComputation computation = new RegionPropsComputation( logger, model, windowSize ))
		{
			return computation.computeRegionPropsForSource( source, level, Cast.unchecked( index ), minTimepoint, maxTimepoint );
		}
		catch ( Exception e )
		{
			throw new TrackastraLinkingException( "Failed to compute region props", e );
		}
	}

	private void runLinkPrediction( final SpatioTemporalIndex< V > index, final List< SingleTimepointRegionProps > regionProps )
			throws TrackastraLinkingException
	{
		log.info( "Performing Trackastra link prediction" );
		try (RegionProps props = new RegionProps( regionProps );
				LinkPrediction prediction =
						new LinkPrediction( settings, Cast.unchecked( index ), Cast.unchecked( edgeCreator ), props, logger ))
		{
			prediction.predictAndCreateLinks();
		}
		catch ( Exception e )
		{
			throw new TrackastraLinkingException( "Failed to perform link prediction", e );
		}
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
