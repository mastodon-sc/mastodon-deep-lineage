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
package org.mastodon.mamut.feature.branch.movement.direction;

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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Represents the movement direction of a BranchSpot.
 * <p>
 * The movement direction is a normalized directional vector pointing from the start Spot position to the end Spot position of the BranchSpot.
 * <p>
 * <h1>Example</h1>
 * <h2>Model-Graph (i.e. Graph of Spots)</h2>
 * <pre>
 *    Spot( 0, X=0.0, Y=0.0, Z=0.0, tp=0 )
 *                      │
 *    Spot( 1, X=3.0, Y=4.0, Z=0.0, tp=1 )
 *                      │
 *    Spot( 2, X=8.0, Y=4.0, Z=12.0, tp=2 )
 *                      │
 *    Spot( 3, X=6.0, Y=3.0, Z=6.0, tp=3 )
 * </pre>
 * <h2>Branch-Graph (i.e. Graph of BranchSpots)</h2>
 * <pre>
 *     branchSpotA
 * </pre>
 * <h2>BranchSpot Movement Direction</h2>
 * <ul>
 *     <li>{@code x = 2/3}</li>
 *     <li>{@code y = 1/3}</li>
 *     <li>{@code z = 2/3}</li>
 * </ul>
 */
public class BranchMovementDirectionFeature implements Feature< BranchSpot >, ValueIsSetEvaluator< BranchSpot >
{
	public static final String KEY = "Branch Movement Direction";

	private static final String HELP_STRING =
			"A normalized directional vector pointing from the start Spot to the end Spot of the BranchSpot thereby representing the overall movement direction of the BranchSpot.";

	private final Map< FeatureProjectionKey, FeatureProjection< BranchSpot > > projectionMap;

	public static final FeatureProjectionSpec MOVEMENT_DIRECTION_X_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Movement direction X", Dimension.NONE );

	public static final FeatureProjectionSpec MOVEMENT_DIRECTION_Y_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Movement direction Y", Dimension.NONE );

	public static final FeatureProjectionSpec MOVEMENT_DIRECTION_Z_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Movement direction Z", Dimension.NONE );

	public final DoublePropertyMap< BranchSpot > movementDirectionX;

	public final DoublePropertyMap< BranchSpot > movementDirectionY;

	public final DoublePropertyMap< BranchSpot > movementDirectionZ;

	public static final Spec BRANCH_MOVEMENT_DIRECTION_FEATURE_SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchMovementDirectionFeature, BranchSpot >
	{
		public Spec()
		{
			super( KEY, HELP_STRING, BranchMovementDirectionFeature.class, BranchSpot.class, Multiplicity.SINGLE,
					MOVEMENT_DIRECTION_X_PROJECTION_SPEC, MOVEMENT_DIRECTION_Y_PROJECTION_SPEC, MOVEMENT_DIRECTION_Z_PROJECTION_SPEC );
		}
	}

	public BranchMovementDirectionFeature( final DoublePropertyMap< BranchSpot > movementDirectionX,
			final DoublePropertyMap< BranchSpot > movementDirectionY, final DoublePropertyMap< BranchSpot > movementDirectionZ )
	{
		this.movementDirectionX = movementDirectionX;
		this.movementDirectionY = movementDirectionY;
		this.movementDirectionZ = movementDirectionZ;

		final FeatureProjectionKey keyX = key( MOVEMENT_DIRECTION_X_PROJECTION_SPEC );
		final FeatureProjectionKey keyY = key( MOVEMENT_DIRECTION_Y_PROJECTION_SPEC );
		final FeatureProjectionKey keyZ = key( MOVEMENT_DIRECTION_Z_PROJECTION_SPEC );

		this.projectionMap = new LinkedHashMap<>( 3 );

		projectionMap.put( keyX, FeatureProjections.project( keyX, movementDirectionX, Dimension.NONE_UNITS ) );
		projectionMap.put( keyY, FeatureProjections.project( keyY, movementDirectionY, Dimension.NONE_UNITS ) );
		projectionMap.put( keyZ, FeatureProjections.project( keyZ, movementDirectionZ, Dimension.NONE_UNITS ) );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > getSpec()
	{
		return BRANCH_MOVEMENT_DIRECTION_FEATURE_SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		movementDirectionX.remove( branchSpot );
		movementDirectionY.remove( branchSpot );
		movementDirectionZ.remove( branchSpot );
	}

	@Override
	public boolean valueIsSet( final BranchSpot branchSpot )
	{
		return movementDirectionX.isSet( branchSpot ) && movementDirectionY.isSet( branchSpot ) && movementDirectionZ.isSet( branchSpot );
	}
}
