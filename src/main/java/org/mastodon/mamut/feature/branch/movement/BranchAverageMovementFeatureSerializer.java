/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature.branch.movement;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.mamut.feature.branch.BranchSpotDoublePropertyFeatureSerializer;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes the {@link BranchAverageMovementFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchAverageMovementFeatureSerializer extends BranchSpotDoublePropertyFeatureSerializer< BranchAverageMovementFeature >
{

	@Override
	public FeatureSpec< BranchAverageMovementFeature, BranchSpot > getFeatureSpec()
	{
		return BranchAverageMovementFeature.BRANCH_AVERAGE_MOVEMENT_FEATURE_SPEC;
	}

	@Override
	protected BranchAverageMovementFeature createFeature( DoublePropertyMap< BranchSpot > map )
	{
		return new BranchAverageMovementFeature( map );
	}

	@Override
	protected DoublePropertyMap< BranchSpot > extractPropertyMap( BranchAverageMovementFeature feature )
	{
		return feature.map;
	}

}
