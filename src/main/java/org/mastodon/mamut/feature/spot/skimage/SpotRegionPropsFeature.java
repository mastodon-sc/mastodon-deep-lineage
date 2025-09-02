package org.mastodon.mamut.feature.spot.skimage;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.lang.invoke.MethodHandles;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotRegionPropsFeature implements Feature< Spot >, ValueIsSetEvaluator< Spot >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static final String KEY = "Trackastra linker features";

	private static final String HELP_STRING =
			"Computes region properties needed for Trackastra based linking using Scikit-image library.";

	/**
	 * Enumeration of all supported region properties.
	 * Each enum constant defines its {@link FeatureProjectionSpec}.
	 */
	public enum RegionProp
	{
		EQUIVALENT_DIAMETER_AREA( "Equivalent Diameter Area" ),
		INTENSITY_MEAN( "Intensity Mean" ),
		INERTIA_TENSOR_XX( "Inertia Tensor xx" ),
		INERTIA_TENSOR_XY( "Inertia Tensor xy" ),
		INERTIA_TENSOR_XZ( "Inertia Tensor xz" ),
		INERTIA_TENSOR_YX( "Inertia Tensor yx" ),
		INERTIA_TENSOR_YY( "Inertia Tensor yy" ),
		INERTIA_TENSOR_YZ( "Inertia Tensor yz" ),
		INERTIA_TENSOR_ZX( "Inertia Tensor zx" ),
		INERTIA_TENSOR_ZY( "Inertia Tensor zy" ),
		INERTIA_TENSOR_ZZ( "Inertia Tensor zz" ),
		BORDER_DIST( "Border Distance" ),
		X( "x coordinate in image coordinate space" ),
		Y( "y coordinate in image coordinate space" ),
		Z( "z coordinate in image coordinate space" );

		private final FeatureProjectionSpec spec;

		RegionProp( String title )
		{
			this.spec = new FeatureProjectionSpec( title, Dimension.NONE );
		}

		public FeatureProjectionSpec spec()
		{
			return spec;
		}
	}

	final Map< RegionProp, List< DoublePropertyMap< Spot > > > valuesByProp;

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	@Plugin( type = FeatureSpec.class )
	public static class SpotRegionPropsFeatureSpec extends FeatureSpec< SpotRegionPropsFeature, Spot >
	{
		public SpotRegionPropsFeatureSpec()
		{
			super( KEY,
					HELP_STRING,
					SpotRegionPropsFeature.class,
					Spot.class,
					Multiplicity.ON_SOURCES,
					// collect all specs from enum
					collectSpecs() );
		}

		private static FeatureProjectionSpec[] collectSpecs()
		{
			RegionProp[] props = RegionProp.values();
			FeatureProjectionSpec[] specs = new FeatureProjectionSpec[ props.length ];
			for ( int i = 0; i < props.length; i++ )
			{
				specs[ i ] = props[ i ].spec();
			}
			return specs;
		}
	}

	public static final SpotRegionPropsFeatureSpec FEATURE_SPEC = new SpotRegionPropsFeatureSpec();

	/**
	 * Constructor: supply all property maps grouped by RegionProp.
	 */
	public SpotRegionPropsFeature( final Map< RegionProp, List< DoublePropertyMap< Spot > > > valuesByProp )
	{
		this.valuesByProp = new EnumMap<>( valuesByProp );
		this.projectionMap = new LinkedHashMap<>( valuesByProp.size() * valuesByProp.values().iterator().next().size() );

		for ( Map.Entry< RegionProp, List< DoublePropertyMap< Spot > > > entry : valuesByProp.entrySet() )
		{
			RegionProp prop = entry.getKey();
			List< DoublePropertyMap< Spot > > maps = entry.getValue();
			for ( int source = 0; source < maps.size(); source++ )
			{
				FeatureProjectionKey projKey = key( prop.spec(), source );
				projectionMap.put( projKey, FeatureProjections.project( projKey, maps.get( source ), Dimension.NONE_UNITS ) );
			}
		}
	}

	@Override
	public FeatureProjection< Spot > project( FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
	{
		return FEATURE_SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		for ( List< DoublePropertyMap< Spot > > maps : valuesByProp.values() )
		{
			for ( DoublePropertyMap< Spot > map : maps )
			{
				map.remove( spot );
			}
		}
	}

	@Override
	public boolean valueIsSet( final Spot spot )
	{
		for ( List< DoublePropertyMap< Spot > > maps : valuesByProp.values() )
		{
			for ( DoublePropertyMap< Spot > map : maps )
			{
				if ( !map.isSet( spot ) )
				{
					return false;
				}
			}
		}
		return true;
	}
}
