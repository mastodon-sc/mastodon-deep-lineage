package org.mastodon.mamut.feature.branch.leaves;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.mamut.feature.branch.BranchSpotIntPropertyFeatureSerializer;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes the {@link BranchNLeavesFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchNLeavesFeatureSerializer extends BranchSpotIntPropertyFeatureSerializer< BranchNLeavesFeature >
{
	@Override
	public FeatureSpec< BranchNLeavesFeature, BranchSpot > getFeatureSpec()
	{
		return BranchNLeavesFeature.BRANCH_N_LEAVES_FEATURE_SPEC;
	}

	@Override
	protected BranchNLeavesFeature createFeature( IntPropertyMap< BranchSpot > map )
	{
		return new BranchNLeavesFeature( map );
	}

	@Override
	protected IntPropertyMap< BranchSpot > extractPropertyMap( BranchNLeavesFeature feature )
	{
		return feature.map;
	}
}
