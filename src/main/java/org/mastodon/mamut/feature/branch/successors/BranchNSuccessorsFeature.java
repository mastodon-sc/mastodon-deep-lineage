/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 N/A
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
package org.mastodon.mamut.feature.branch.successors;

import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.branch.IntPropertyFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents the total number of successors of a branch spot in the whole track subtree of this branch spot.
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
public class BranchNSuccessorsFeature extends IntPropertyFeature< BranchSpot >
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
