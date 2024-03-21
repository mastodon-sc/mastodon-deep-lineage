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

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.movement.average.BranchAverageMovementFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BranchAverageMovementFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private Feature< BranchSpot > branchAverageMovementFeature;

	private final ExampleGraph1 graph = new ExampleGraph1();

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			branchAverageMovementFeature = FeatureComputerTestUtils.getFeature( context, graph.getModel(),
					BranchAverageMovementFeature.BRANCH_AVERAGE_MOVEMENT_FEATURE_SPEC );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		FeatureProjection< BranchSpot > projection =
				getProjection( branchAverageMovementFeature, BranchAverageMovementFeature.PROJECTION_SPEC );
		double expected = 4 * Math.sqrt( 1 + 4 + 9 ) / 3;
		assertEquals( expected, projection.value( graph.branchSpotA ), 0 );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchAverageMovementFeature branchAverageMovementFeatureReloaded;
		try (Context context = new Context())
		{
			branchAverageMovementFeatureReloaded =
					( BranchAverageMovementFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(),
							branchAverageMovementFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchAverageMovementFeature,
				branchAverageMovementFeatureReloaded,
				Collections.singleton( graph.branchSpotA ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN(
				getProjection( branchAverageMovementFeature, BranchAverageMovementFeature.PROJECTION_SPEC ).value( graph.branchSpotA ) ) );

		// invalidate feature
		branchAverageMovementFeature.invalidate( graph.branchSpotA );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN(
				getProjection( branchAverageMovementFeature, BranchAverageMovementFeature.PROJECTION_SPEC ).value( graph.branchSpotA ) ) );
	}
}
