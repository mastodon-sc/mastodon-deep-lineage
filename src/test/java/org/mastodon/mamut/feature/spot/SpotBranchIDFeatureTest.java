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
package org.mastodon.mamut.feature.spot;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SpotBranchIDFeatureTest extends AbstractFeatureTest< Spot >
{
	private Feature< Spot > feature;

	private final ExampleGraph2 graph = new ExampleGraph2();

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			feature = FeatureComputerTestUtils.getFeature( context, graph.getModel(), SpotBranchIDFeature.SPEC );
		}
	}

	@Override
	@Test
	public void testFeatureComputation()
	{
		FeatureProjection< Spot > featureProjection = getProjection( feature, SpotBranchIDFeature.PROJECTION_SPEC );
		assertEquals( graph.branchSpotA.getInternalPoolIndex(), ( int ) featureProjection.value( graph.spot0 ) );
		assertEquals( graph.branchSpotE.getInternalPoolIndex(), ( int ) featureProjection.value( graph.spot10 ) );
	}

	@Override
	@Test
	public void testFeatureSerialization() throws IOException
	{
		SpotBranchIDFeature featureReloaded;
		try (Context context = new Context())
		{
			featureReloaded = ( SpotBranchIDFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(), feature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( feature, featureReloaded,
				Collections.singleton( graph.spot0 )
		) );
	}

	@Override
	@Test
	public void testFeatureInvalidate()
	{
		Spot spot = graph.spot0;
		// test, if features have a non "-1" value before invalidation
		assertNotEquals( -1, ( int ) getProjection( feature, SpotBranchIDFeature.PROJECTION_SPEC ).value( spot ) );

		// invalidate feature
		feature.invalidate( spot );

		// test, if features are "-1" after invalidation
		assertEquals( -1, ( int ) getProjection( feature, SpotBranchIDFeature.PROJECTION_SPEC ).value( spot ) );
	}
}
