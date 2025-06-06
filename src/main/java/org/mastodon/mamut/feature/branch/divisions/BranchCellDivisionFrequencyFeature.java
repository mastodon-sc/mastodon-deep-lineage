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
package org.mastodon.mamut.feature.branch.divisions;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Computes the cell division frequency, i.e. the number of subsequent cell divisions divided by the total duration of the branches in the subtree.
 * NB: This feature assumes a binary tree. Thus, the number of cell divisions is the number of leaves in the subtree minus one.
 * <br>
 * <br>
 * Example:
 * <pre>
 *                  branchSpotA (duration=3)
 * 	       ┌──────────────┴─────────────┐
 * 	       │                            │
 * 	 branchSpotB (duration=2)     branchSpotC (duration=1)
 * 	                             ┌──────┴───────────────────┐
 * 	                             │                          │
 * 	                       branchSpotD (duration=2)   branchSpotE (duration=3)
 * </pre>
 * In this example, the cell division frequency for each branch spot is:
 * <ul>
 *     <li>branchSpotA = 0.18 (2/11)</li>
 *     <li>branchSpotB = 0</li>
 *     <li>branchSpotC = 0.17 (1/6)</li>
 *     <li>branchSpotD = 0</li>
 *     <li>branchSpotE = 0</li>
 * </ul>
 */
public class BranchCellDivisionFrequencyFeature implements Feature< BranchSpot >, ValueIsSetEvaluator< BranchSpot >
{
	public static final String KEY = "Cell division frequency";

	private static final String HELP_STRING =
			"Number of cell divisions in subsequent tree divided by total duration of branches in subsequent tree.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.NONE );

	public final DoublePropertyMap< BranchSpot > frequency;

	protected final FeatureProjection< BranchSpot > projection;

	public static final Spec FEATURE_SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchCellDivisionFrequencyFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchCellDivisionFrequencyFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchCellDivisionFrequencyFeature( final DoublePropertyMap< BranchSpot > map )
	{
		this.frequency = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > getSpec()
	{
		return FEATURE_SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		frequency.remove( branchSpot );
	}

	@Override
	public boolean valueIsSet( final BranchSpot branchSpot )
	{
		return frequency.isSet( branchSpot );
	}
}
