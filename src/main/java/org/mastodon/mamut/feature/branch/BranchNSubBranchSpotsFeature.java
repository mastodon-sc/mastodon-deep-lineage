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
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents the total number of successors of a branch spot in the whole track sub-tree of this branch spot.
 *
 * In the following example this number would equal to following branchSpots as
 * follows:
 *
 * <pre>
 *                         branchSpot0
 *  	       ┌──────────────┴─────────────────┐
 *  	       │                                │
 *  	   branchspot1                      branchSpot2
 *  	┌──────┴───────┐
 *  	│              │
 *  branchspot3 branchSpot4
 * </pre>
 *
 * <ul>
 * <li>{@code branchSpot0 = 4}</li>
 * <li>{@code branchSpot1 = 2}</li>
 * <li>{@code branchSpot2 = 0}</li>
 * <li>{@code branchSpot3 = 0}</li>
 * <li>{@code branchSpot4 = 0}</li>
 * </ul>
 */
public class BranchNSubBranchSpotsFeature implements Feature< BranchSpot >
{
	public static final String KEY = "Branch N sub branch spots";

	private static final String HELP_STRING = "Computes the total number of successors of a branch spot in the whole track sub-tree of this branch spot.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec SPEC = new Spec();

	final IntPropertyMap< BranchSpot > map;

	private final IntFeatureProjection< BranchSpot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchNSubBranchSpotsFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchNSubBranchSpotsFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchNSubBranchSpotsFeature( final IntPropertyMap< BranchSpot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public int get( final BranchSpot branch )
	{
		return map.getInt( branch );
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
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchSpot spot )
	{
		map.remove( spot );
	}
}
