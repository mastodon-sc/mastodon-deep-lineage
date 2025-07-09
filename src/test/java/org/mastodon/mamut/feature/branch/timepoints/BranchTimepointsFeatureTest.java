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
package org.mastodon.mamut.feature.branch.timepoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

class BranchTimepointsFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private Feature< BranchSpot > branchTimepointsFeature;

	private final ExampleGraph2 graph = new ExampleGraph2();

	@BeforeEach
	void setUp()
	{
		try (Context context = new Context())
		{
			branchTimepointsFeature = FeatureComputerTestUtils.getFeature( context, graph.getModel(), BranchTimepointsFeature.SPEC );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		FeatureProjection< BranchSpot > startTimepointsProjection =
				getProjection( branchTimepointsFeature, BranchTimepointsFeature.START_PROJECTION_SPEC );
		FeatureProjection< BranchSpot > endTimepointsProjection =
				getProjection( branchTimepointsFeature, BranchTimepointsFeature.END_PROJECTION_SPEC );

		assertEquals( 0, startTimepointsProjection.value( graph.branchSpotA ), 0 );
		assertEquals( 3, startTimepointsProjection.value( graph.branchSpotB ), 0 );
		assertEquals( 3, startTimepointsProjection.value( graph.branchSpotC ), 0 );
		assertEquals( 5, startTimepointsProjection.value( graph.branchSpotD ), 0 );
		assertEquals( 5, startTimepointsProjection.value( graph.branchSpotE ), 0 );

		assertEquals( 2, endTimepointsProjection.value( graph.branchSpotA ), 0 );
		assertEquals( 4, endTimepointsProjection.value( graph.branchSpotB ), 0 );
		assertEquals( 5, endTimepointsProjection.value( graph.branchSpotC ), 0 );
		assertEquals( 7, endTimepointsProjection.value( graph.branchSpotD ), 0 );
		assertEquals( 7, endTimepointsProjection.value( graph.branchSpotE ), 0 );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchTimepointsFeature branchTimepointsFeatureReloaded;
		try (Context context = new Context())
		{
			branchTimepointsFeatureReloaded = ( BranchTimepointsFeature ) FeatureSerializerTestUtils.saveAndReload( context,
					graph.getModel(), branchTimepointsFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchTimepointsFeature, branchTimepointsFeatureReloaded,
				Arrays.asList( graph.branchSpotA, graph.branchSpotB, graph.branchSpotC, graph.branchSpotD, graph.branchSpotE ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not -1 (i.e. default value) before invalidation
		assertNotEquals( -1,
				getProjection( branchTimepointsFeature, BranchTimepointsFeature.START_PROJECTION_SPEC ).value( graph.branchSpotA ), 0d );
		assertNotEquals( -1,
				getProjection( branchTimepointsFeature, BranchTimepointsFeature.END_PROJECTION_SPEC ).value( graph.branchSpotA ), 0d );

		// invalidate feature
		branchTimepointsFeature.invalidate( graph.branchSpotA );

		// test, if features are -1 (i.e. default value) after invalidation
		assertEquals( -1,
				getProjection( branchTimepointsFeature, BranchTimepointsFeature.START_PROJECTION_SPEC ).value( graph.branchSpotA ), 0d );
		assertEquals( -1, getProjection( branchTimepointsFeature, BranchTimepointsFeature.END_PROJECTION_SPEC ).value( graph.branchSpotA ),
				0d );
	}
}
