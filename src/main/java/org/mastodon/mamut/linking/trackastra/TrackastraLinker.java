package org.mastodon.mamut.linking.trackastra;

import java.lang.invoke.MethodHandles;

import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.Benchmark;
import net.imglib2.util.Cast;

import org.mastodon.Ref;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.tracking.linking.graph.AbstractGraphParticleLinkerOp;
import org.mastodon.tracking.linking.graph.GraphParticleLinkerOp;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin( type = GraphParticleLinkerOp.class )
public class TrackastraLinker< V extends Vertex< E > & HasTimepoint & RealLocalizable & Ref< V >, E extends Edge< V > >
		extends AbstractGraphParticleLinkerOp< V, E >
		implements Benchmark
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void mutate1( final ReadOnlyGraph< V, E > graph, final SpatioTemporalIndex< V > index )
	{
		/*
		try (final TrackAstraRegionProps trackAstraRegionProps = new TrackAstraRegionProps())
		{
			final SpotRegionPropsFeature feature;
			final Feature< ? > existingFeature = featureModel.getFeature( SpotRegionPropsFeature.FEATURE_SPEC );
			if ( existingFeature != null )
				feature = ( SpotRegionPropsFeature ) existingFeature;
			else
			{
				Map< SpotRegionPropsFeature.RegionProp, List< DoublePropertyMap< Spot > > > map =
						SpotRegionPropsFeatureComputer.createEmptyRegionPropsMap( 1, null );
				feature = new SpotRegionPropsFeature( map );
				featureModel.declareFeature( feature );
			}
			int minTimepoint = ( int ) settings.get( KEY_MIN_TIMEPOINT );
			int maxTimepoint = ( int ) settings.get( KEY_MAX_TIMEPOINT );
			int sourceId = ( int ) settings.get( KEY_SETUP_ID );
			int level = (int) settings.get(KEY_LEVEL);
			trackAstraRegionProps.compute( null, null, feature, null, minTimepoint, maxTimepoint, sourceId, level ); // TODO: selection
		}
		catch ( Exception e )
		{
			logger.error( "Error during TrackAstra RegionProps computing", e );
			ok = false;
			errorMessage = e.getMessage();
			return;
		}
		
		 */

		logger.info( "perform track astra linking" );
		try (final TrackastraLinkPrediction trackAstraLinkPrediction =
				new TrackastraLinkPrediction( settings, featureModel, Cast.unchecked( index ), Cast.unchecked( edgeCreator ) ))
		{
			trackAstraLinkPrediction.compute();
		}
		catch ( Exception e )
		{
			logger.error( "Error during TrackAstra linking", e );
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
		return "All good!";
	}
}
