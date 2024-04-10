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
package org.mastodon.mamut.feature.branch.movement.direction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BranchMovementDirectionFeatureTest extends AbstractFeatureTest< BranchSpot >
{

	private Feature< BranchSpot > branchMovementDirectionFeature;

	private final ExampleGraph1 graph = new ExampleGraph1();

	@BeforeEach
	public void setUp()
	{
		try (Context context = new Context())
		{
			branchMovementDirectionFeature = FeatureComputerTestUtils.getFeature( context, graph.getModel(),
					BranchMovementDirectionFeature.BRANCH_MOVEMENT_DIRECTION_FEATURE_SPEC );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		double distance = Math.sqrt( 16 + 64 + 144 );
		double expectedX = 4 / distance;
		double expectedY = 8 / distance;
		double expectedZ = 12 / distance;

		FeatureProjection< BranchSpot > xProjection =
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_X_PROJECTION_SPEC );
		FeatureProjection< BranchSpot > yProjection =
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC );
		FeatureProjection< BranchSpot > zProjection =
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Z_PROJECTION_SPEC );

		assertEquals( expectedX, xProjection.value( graph.branchSpotA ), 0.0001d );
		assertEquals( expectedY, yProjection.value( graph.branchSpotA ), 0.0001d );
		assertEquals( expectedZ, zProjection.value( graph.branchSpotA ), 0.0001d );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchMovementDirectionFeature branchMovementDirectionFeatureReloaded;
		try (Context context = new Context())
		{
			branchMovementDirectionFeatureReloaded =
					( BranchMovementDirectionFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(),
							branchMovementDirectionFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchMovementDirectionFeature,
				branchMovementDirectionFeatureReloaded,
				Collections.singleton( graph.branchSpotA ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC )
						.value( graph.branchSpotA ) ) );
		assertFalse( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC )
						.value( graph.branchSpotA ) ) );
		assertFalse( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Z_PROJECTION_SPEC )
						.value( graph.branchSpotA ) ) );

		// invalidate feature
		branchMovementDirectionFeature.invalidate( graph.branchSpotA );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_X_PROJECTION_SPEC ).value(
						graph.branchSpotA ) ) );
		assertTrue( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC ).value(
						graph.branchSpotA ) ) );
		assertTrue( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Z_PROJECTION_SPEC ).value(
						graph.branchSpotA ) ) );
	}
}
