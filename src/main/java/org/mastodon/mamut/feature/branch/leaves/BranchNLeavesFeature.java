package org.mastodon.mamut.feature.branch.leaves;

import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.branch.BranchSpotIntPropertyFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents the total number of leaves of a branch spot in the whole track subtree of this branch spot.
 * <p>
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
 * <li>{@code branchSpot0 = 3}</li>
 * <li>{@code branchSpot1 = 2}</li>
 * <li>{@code branchSpot2 = 1}</li>
 * <li>{@code branchSpot3 = 1}</li>
 * <li>{@code branchSpot4 = 1}</li>
 * </ul>
 */
public class BranchNLeavesFeature extends BranchSpotIntPropertyFeature< BranchSpot >
{
	public static final String KEY = "Branch N leaves";

	private static final String HELP_STRING = "Counts the leaves in the sub-tree of this branch spot.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final BranchNLeavesFeature.Spec BRANCH_N_LEAVES_FEATURE_SPEC = new BranchNLeavesFeature.Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchNLeavesFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchNLeavesFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchNLeavesFeature( final IntPropertyMap< BranchSpot > map )
	{
		super( map );
	}

	@Override
	public FeatureProjectionSpec getFeatureProjectionSpec()
	{
		return PROJECTION_SPEC;
	}

	@Override
	public BranchNLeavesFeature.Spec getSpec()
	{
		return BRANCH_N_LEAVES_FEATURE_SPEC;
	}
}
