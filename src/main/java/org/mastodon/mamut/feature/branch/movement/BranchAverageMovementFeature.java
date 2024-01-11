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

import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.branch.DoublePropertyFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents the mean (i.e. average) displacement and thereby the average movement speed for a BranchSpot, e.g. if a BranchSpot has 10 Spots, all 10 displacements are computed, summed and divided by 10..
 * <br>
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
public class BranchAverageMovementFeature extends DoublePropertyFeature< BranchSpot >
{
	public static final String KEY = "Branch Average Movement";

	private static final String HELP_STRING = "The average movement per frame of a spot during its life cycle.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec BRANCH_AVERAGE_MOVEMENT_FEATURE_SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchAverageMovementFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchAverageMovementFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchAverageMovementFeature( final DoublePropertyMap< BranchSpot > map )
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
		return BRANCH_AVERAGE_MOVEMENT_FEATURE_SPEC;
	}
}
