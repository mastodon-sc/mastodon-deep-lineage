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
package org.mastodon.mamut.feature.branch.movement.average;

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
 * Represents the mean (i.e. average) displacement of BranchSpot.
 * Assumed that the time between time points (tp) of the spots that are represented by the branch spot is constant,
 * this also represents the average movement speed of a BranchSpot.
 * <p>
 * Cf. the following example:
 * <p>
 * <strong>Model-Graph (i.e. Graph of Spots)</strong>
 * <pre>
 *    Spot( 0, X=0.0, Y=0.0, Z=0.0, tp=0 )
 *                      │
 *                      │
 *    Spot( 1, X=3.0, Y=4.0, Z=0.0, tp=1 )
 *                      │
 *                      │
 *    Spot( 2, X=8.0, Y=4.0, Z=12.0, tp=2 )
 *                      │
 *                      │
 *                      │
 *                      │
 *    Spot( 3, X=8.0, Y=12.0, Z=27.0, tp=*4* )
 * </pre>
 * <strong>Branch-Graph (i.e. Graph of BranchSpots)</strong>
 * <pre>
 *     branchSpotA
 * </pre>
 * <p>
 * <strong>Spot Movements</strong>
 * <ul>
 * <li>{@code spot 0 -> spot 1 = 5}</li>
 * <li>{@code spot 1 -> spot 2 = 13}</li>
 * <li>{@code spot 2 -> spot 3 = 17}</li>
 * </ul>
 * <p>
 * <strong>BranchSpot Average Movement</strong>
 * <ul>
 *     <li>{@code branchSpotA = (5 + 13 + 17) / *4* = 8.75}</li>
 * </ul>
 */
public class BranchAverageMovementFeature implements Feature< BranchSpot >, ValueIsSetEvaluator< BranchSpot >
{
	public static final String KEY = "Branch Average Movement";

	private static final String HELP_STRING = "The average movement per frame of a spot during its life cycle.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.LENGTH );

	public final DoublePropertyMap< BranchSpot > averageMovement;

	protected final FeatureProjection< BranchSpot > projection;

	public static final Spec BRANCH_AVERAGE_MOVEMENT_FEATURE_SPEC = new Spec();

	final String lengthUnits;

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

	public BranchAverageMovementFeature( final DoublePropertyMap< BranchSpot > map, final String lengthUnits )
	{
		this.averageMovement = map;
		this.lengthUnits = lengthUnits;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, lengthUnits );
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
		return BRANCH_AVERAGE_MOVEMENT_FEATURE_SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		averageMovement.remove( branchSpot );
	}

	@Override
	public boolean valueIsSet( final BranchSpot vertex )
	{
		return averageMovement.isSet( vertex );
	}
}
