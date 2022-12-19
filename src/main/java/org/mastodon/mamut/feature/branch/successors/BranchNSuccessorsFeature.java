package org.mastodon.mamut.feature.branch.successors;

import static org.mastodon.feature.FeatureProjectionKey.key;

import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.branch.IntPropertyFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents the total number of successors of a branch spot in the whole track sub-tree of this branch spot.
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
 * <li>{@code branchSpot0 = 4}</li>
 * <li>{@code branchSpot1 = 2}</li>
 * <li>{@code branchSpot2 = 0}</li>
 * <li>{@code branchSpot3 = 0}</li>
 * <li>{@code branchSpot4 = 0}</li>
 * </ul>
 */
public class BranchNSuccessorsFeature extends IntPropertyFeature
{
	public static final String KEY = "Branch N sub branch spots";

	private static final String HELP_STRING = "Counts the successors in the sub-tree of this branch spot.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec BRANCH_N_SUCCESSORS_FEATURE = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchNSuccessorsFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchNSuccessorsFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchNSuccessorsFeature( final IntPropertyMap< BranchSpot > map )
	{
		super( map );
	}

	@Override
	public FeatureProjectionSpec getFeatureProjectionSpec()
	{
		return PROJECTION_SPEC;
	}

	@Override
	public Spec getSpec()
	{
		return BRANCH_N_SUCCESSORS_FEATURE;
	}
}
