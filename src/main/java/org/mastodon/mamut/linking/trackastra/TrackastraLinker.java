package org.mastodon.mamut.linking.trackastra;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_WINDOW_SIZE;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.mamut.linking.trackastra.TrackastraUtils.KEY_SOURCE;

import java.lang.invoke.MethodHandles;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.util.Cast;

import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.linking.trackastra.appose.RegionProps;
import org.mastodon.mamut.linking.trackastra.appose.SingleTimepointRegionProps;
import org.mastodon.mamut.linking.trackastra.appose.TrackastraLinkPrediction;
import org.mastodon.mamut.linking.trackastra.appose.RegionPropsComputation;
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

	private static final Logger slf4jLogger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void mutate1( final ReadOnlyGraph< V, E > graph, final SpatioTemporalIndex< V > index )
	{
		slf4jLogger.info( "compute region props for trackastra linking" );
		List< SingleTimepointRegionProps > singleTimepointRegionProps;
		String model = ( ( TrackastraModel ) settings.get( TrackastraUtils.KEY_MODEL ) ).getName();
		int windowSize = ( Integer ) settings.get( KEY_WINDOW_SIZE );
		try (final RegionPropsComputation regionPropsComputation = new RegionPropsComputation( logger, model, windowSize ))
		{
			int minTimepoint = ( int ) settings.get( KEY_MIN_TIMEPOINT );
			int maxTimepoint = ( int ) settings.get( KEY_MAX_TIMEPOINT );
			int timeRange = maxTimepoint - minTimepoint + 1;
			if ( windowSize > ( maxTimepoint - minTimepoint + 1 ) )
			{
				throw new IllegalArgumentException( "The window size (" + windowSize + ") is larger than the time range (" + timeRange
						+ ").\nPlease adjust the window size or the time range." );
			}
			int level = (int) settings.get(KEY_LEVEL);
			Source< ? > source = ( Source< ? > ) settings.get( KEY_SOURCE );
			logger.info( "Computing region props for source: " + source + "\n" );
			singleTimepointRegionProps = regionPropsComputation.computeRegionPropsForSource( source, level, Cast.unchecked( index ),
					minTimepoint, maxTimepoint );
		}
		catch ( Exception e )
		{
			slf4jLogger.error( "Error during TrackAstra RegionProps computing: {}", e.getMessage(), e );
			ok = false;
			errorMessage = e.getMessage();
			return;
		}

		slf4jLogger.info( "Perform trackastra linking\n" );
		try (RegionProps regionProps = new RegionProps( singleTimepointRegionProps );
				final TrackastraLinkPrediction trackAstraLinkPrediction = new TrackastraLinkPrediction( settings, Cast.unchecked( index ),
						Cast.unchecked( edgeCreator ), regionProps, logger ))
		{
			logger.info( "Perform linking\n" );
			trackAstraLinkPrediction.compute();
		}
		catch ( Exception e )
		{
			slf4jLogger.error( "Error during TrackAstra linking: {}", e.getMessage(), e );
			ok = false;
			errorMessage = e.getMessage();
			return;
		}
		ok = true;
	}

	@Override
	public long getProcessingTime()
	{
		return 0;
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
