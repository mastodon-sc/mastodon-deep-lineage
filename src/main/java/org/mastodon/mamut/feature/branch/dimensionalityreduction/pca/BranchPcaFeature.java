/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.feature.branch.dimensionalityreduction.pca;

import java.util.List;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.dimensionalityreduction.pca.AbstractPcaFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents a PCA feature for BranchSpots in the Mastodon project.
 * <br>
 * This feature is used to store the PCA outputs for BranchSpots.
 * <br>
 * The PCA outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the PCA output.
 */
public class BranchPcaFeature extends AbstractPcaFeature< BranchSpot >
{
	public static final String KEY = "Branch PCA outputs";

	private final BranchSpotPcaFeatureSpec adaptedSpec;

	public static final BranchSpotPcaFeatureSpec GENERIC_SPEC = new BranchSpotPcaFeatureSpec();

	public BranchPcaFeature( final List< DoublePropertyMap< BranchSpot > > outputMaps )
	{
		super( outputMaps );
		FeatureProjectionSpec[] projectionSpecs =
				projectionMap.keySet().stream().map( FeatureProjectionKey::getSpec ).toArray( FeatureProjectionSpec[]::new );
		this.adaptedSpec = new BranchSpotPcaFeatureSpec( projectionSpecs );
	}

	@Plugin( type = FeatureSpec.class )
	public static class BranchSpotPcaFeatureSpec extends FeatureSpec< BranchPcaFeature, BranchSpot >
	{
		public BranchSpotPcaFeatureSpec()
		{
			super( KEY, HELP_STRING, BranchPcaFeature.class, BranchSpot.class, Multiplicity.SINGLE );
		}

		public BranchSpotPcaFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, BranchPcaFeature.class, BranchSpot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	@Override
	public FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > getSpec()
	{
		return adaptedSpec;
	}
}
