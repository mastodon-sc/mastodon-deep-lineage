package org.mastodon.mamut.feature.spot;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

public class SpotBranchIDFeature implements Feature< Spot >
{

	public static final String KEY = "Branch spot ID";

	private static final String HELP_STRING = "Returns the ID of the branch each spot belongs to.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final SpotBranchIDFeature.Spec SPEC = new SpotBranchIDFeature.Spec();

	final IntPropertyMap< Spot > map;

	private final IntFeatureProjection< Spot > projection;

	@Plugin(type = FeatureSpec.class)
	public static class Spec extends FeatureSpec< SpotBranchIDFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotBranchIDFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC
			);
		}
	}

	SpotBranchIDFeature( final IntPropertyMap< Spot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public int get( final Spot spot )
	{
		return map.getInt( spot );
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public SpotBranchIDFeature.Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		map.remove( spot );
	}
}
