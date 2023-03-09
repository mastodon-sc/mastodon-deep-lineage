package org.mastodon.mamut.feature.branch.successors;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.mamut.feature.branch.BranchSpotIntPropertyFeatureSerializer;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes the {@link BranchNSuccessorsFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchNSuccessorsFeatureSerializer
		extends BranchSpotIntPropertyFeatureSerializer< BranchNSuccessorsFeature >
{

	@Override
	public FeatureSpec< BranchNSuccessorsFeature, BranchSpot > getFeatureSpec()
	{
		return BranchNSuccessorsFeature.BRANCH_N_SUCCESSORS_FEATURE;
	}

	@Override
	protected BranchNSuccessorsFeature createFeature( IntPropertyMap< BranchSpot > map )
	{
		return new BranchNSuccessorsFeature( map );
	}

	@Override
	protected IntPropertyMap< BranchSpot > extractPropertyMap( BranchNSuccessorsFeature feature )
	{
		return feature.map;
	}
}
