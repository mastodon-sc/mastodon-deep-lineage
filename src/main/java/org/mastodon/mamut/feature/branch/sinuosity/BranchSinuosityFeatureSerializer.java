package org.mastodon.mamut.feature.branch.sinuosity;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.mamut.feature.branch.BranchSpotDoublePropertyFeatureSerializer;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes the {@link BranchSinuosityFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchSinuosityFeatureSerializer extends BranchSpotDoublePropertyFeatureSerializer< BranchSinuosityFeature >
{
	@Override
	public FeatureSpec< BranchSinuosityFeature, BranchSpot > getFeatureSpec()
	{
		return BranchSinuosityFeature.BRANCH_SINUOSITY_FEATURE_SPEC;
	}

	@Override
	protected BranchSinuosityFeature createFeature( DoublePropertyMap< BranchSpot > map )
	{
		return new BranchSinuosityFeature( map );
	}

	@Override
	protected DoublePropertyMap< BranchSpot > extractPropertyMap( BranchSinuosityFeature feature )
	{
		return feature.map;
	}
}
