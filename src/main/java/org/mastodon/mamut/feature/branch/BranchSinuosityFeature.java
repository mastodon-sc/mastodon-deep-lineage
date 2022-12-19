package org.mastodon.mamut.feature.branch;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class BranchSinuosityFeature implements Feature< BranchSpot >
{
	public static final String KEY = "Branch Sinuosity";

	private static final String HELP_STRING = "Computes the directness of movement a spot during a single cell life cycle.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final BranchSinuosityFeature.Spec SPEC = new BranchSinuosityFeature.Spec();

	final DoublePropertyMap< BranchSpot > map;

	private final FeatureProjection< BranchSpot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchSinuosityFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchSinuosityFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchSinuosityFeature( final DoublePropertyMap< BranchSpot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public double get( final BranchSpot branchSpot )
	{
		return map.getDouble( branchSpot );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public BranchSinuosityFeature.Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		map.remove( branchSpot );
	}
}
