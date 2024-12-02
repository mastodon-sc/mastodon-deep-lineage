/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.feature.branch.dimensionalityreduction.umap;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.List;

/**
 * Represents a UMAP feature for BranchSpots in the Mastodon project.
 * <br>
 * This feature is used to store the UMAP outputs for BranchSpots.
 * <br>
 * The UMAP outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the UMAP output.
 */
public class BranchUmapFeature extends AbstractUmapFeature< BranchSpot >
{
	public static final String KEY = "Branch UMAP output";

	private final BranchSpotUmapFeatureSpec adaptedSpec;

	public static final BranchSpotUmapFeatureSpec GENERIC_SPEC = new BranchSpotUmapFeatureSpec();

	public BranchUmapFeature( final List< DoublePropertyMap< BranchSpot > > umapOutputMaps )
	{
		super( umapOutputMaps );
		FeatureProjectionSpec[] projectionSpecs =
				projectionMap.keySet().stream().map( FeatureProjectionKey::getSpec ).toArray( FeatureProjectionSpec[]::new );
		this.adaptedSpec = new BranchSpotUmapFeatureSpec( projectionSpecs );
	}

	@Plugin( type = FeatureSpec.class )
	public static class BranchSpotUmapFeatureSpec extends FeatureSpec< BranchUmapFeature, BranchSpot >
	{
		public BranchSpotUmapFeatureSpec()
		{
			super( KEY, HELP_STRING, BranchUmapFeature.class, BranchSpot.class, Multiplicity.SINGLE );
		}

		public BranchSpotUmapFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, BranchUmapFeature.class, BranchSpot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	@Override
	public FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > getSpec()
	{
		return adaptedSpec;
	}
}
