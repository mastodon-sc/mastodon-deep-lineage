package org.mastodon.mamut.feature.spot.skimage;

import static org.mastodon.util.TreeUtils.getMaxTimepoint;
import static org.mastodon.util.TreeUtils.getMinTimepoint;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.imglib2.realtransform.AffineTransform3D;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.feature.AbstractResettableFeatureComputer;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.feature.spot.skimage.SpotRegionPropsFeature.RegionProp;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.tracking.detection.DetectionUtil;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.SourceAndConverter;

@Plugin( type = MamutFeatureComputer.class )
public class SpotRegionPropsFeatureComputer extends AbstractResettableFeatureComputer
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotRegionPropsFeature output;

	public SpotRegionPropsFeatureComputer() throws IOException
	{
		super();
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotRegionPropsFeature.FEATURE_SPEC );
			if ( null != feature )
			{
				output = ( SpotRegionPropsFeature ) feature;
				return;
			}
			Map< RegionProp, List< DoublePropertyMap< Spot > > > propertyMaps =
					createEmptyRegionPropsMap( bdvData.getSources().size(), model );
			// Create a new output.
			output = new SpotRegionPropsFeature( propertyMaps );
		}
	}

	public static Map< RegionProp, List< DoublePropertyMap< Spot > > > createEmptyRegionPropsMap( final int nSources, final Model model )
	{
		final Map< RegionProp, List< DoublePropertyMap< Spot > > > map = new EnumMap<>( RegionProp.class );
		for ( RegionProp regionProp : RegionProp.values() )
		{
			map.put( regionProp, new ArrayList<>( nSources ) );
		}

		for ( int i = 0; i < nSources; i++ )
		{
			for ( RegionProp regionProp : RegionProp.values() )
			{
				map.get( regionProp ).add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
			}
		}
		return map;
	}

	@Override
	public void run()
	{
		super.run();

		try (TrackAstraRegionProps trackAstraRegionProps = new TrackAstraRegionProps())
		{
			int minTimepoint = getMinTimepoint( model );
			int maxTimepoint = getMaxTimepoint( model );
			int level = 0;
			List< SourceAndConverter< ? > > sources = bdvData.getSources();
			for ( int sourceId = 0; sourceId < sources.size(); sourceId++ )
			{
				if ( sourceId > 0 ) // TODO for testing only compute for first source
					break;
				trackAstraRegionProps.compute( sources, model, output, status, minTimepoint, maxTimepoint, sourceId, level );
				addCoords( level, minTimepoint, maxTimepoint, sources );

			}

		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	private void addCoords( final int resolutionLevel, final int minTimepoint, final int maxTimepoint,
			final List< SourceAndConverter< ? > > sources )
	{
		for ( AtomicInteger sourceId = new AtomicInteger( 0 ); sourceId.get() < sources.size(); sourceId.getAndIncrement() )
		{
			for ( int t = minTimepoint; t <= maxTimepoint; t++ )
			{
				AffineTransform3D transform = DetectionUtil.getTransform( sources, t, sourceId.get(), resolutionLevel );

				model.getSpatioTemporalIndex().getSpatialIndex( t ).forEach( spot -> {
					double[] spotPosition = new double[ 3 ];
					double[] transformedPosition = new double[ 3 ];
					for ( int i = 0; i < 3; i++ )
					{
						spotPosition[ i ] = spot.getDoublePosition( i );
					}
					transform.applyInverse( transformedPosition, spotPosition );
					output.valuesByProp.get( RegionProp.X ).get( sourceId.get() ).set( spot, transformedPosition[ 0 ] );
					output.valuesByProp.get( RegionProp.Y ).get( sourceId.get() ).set( spot, transformedPosition[ 1 ] );
					output.valuesByProp.get( RegionProp.Z ).get( sourceId.get() ).set( spot, transformedPosition[ 2 ] );
				} );
			}
		}
	}

	@Override
	protected void reset()
	{
		for ( RegionProp regionProp : RegionProp.values() )
		{
			output.valuesByProp.get( regionProp ).forEach( DoublePropertyMap::beforeClearPool );
		}
	}
}
