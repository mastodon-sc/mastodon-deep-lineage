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
package org.mastodon.mamut.feature.spot.dimensionalityreduction.umap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapController;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapFeatureSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.util.UmapInputDimension;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpotUmapFeatureTest extends AbstractFeatureTest< Spot >
{
	private SpotUmapFeature spotUmapFeature;

	private final ExampleGraph2 graph2 = new ExampleGraph2();

	private FeatureProjectionSpec spec0;

	private FeatureProjectionSpec spec1;

	@BeforeEach
	public void setUp()
	{
		try (Context context = new Context())
		{
			FeatureModel featureModel = graph2.getModel().getFeatureModel();
			UmapController umapController = new UmapController( graph2.getModel(), context );
			UmapFeatureSettings settings = umapController.getFeatureSettings();
			settings.setNumberOfNeighbors( 5 );
			Supplier< List< UmapInputDimension< Spot, Link > > > inputDimensionsSupplier =
					() -> UmapInputDimension.getListFromFeatureModel( featureModel, Spot.class, Link.class );
			umapController.computeFeature( inputDimensionsSupplier );
			spotUmapFeature = FeatureUtils.getFeature( graph2.getModel(), SpotUmapFeature.SpotUmapFeatureSpec.class );
			assertNotNull( spotUmapFeature );
			spec0 = new FeatureProjectionSpec( spotUmapFeature.getProjectionName( 0 ), Dimension.NONE );
			spec1 = new FeatureProjectionSpec( spotUmapFeature.getProjectionName( 1 ), Dimension.NONE );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( spotUmapFeature );
		FeatureProjection< Spot > projection0 = getProjection( spotUmapFeature, spec0 );
		FeatureProjection< Spot > projection1 = getProjection( spotUmapFeature, spec1 );
		assertFalse( Double.isNaN( projection0.value( graph2.spot13 ) ) );
		assertNotEquals( 0, projection0.value( graph2.spot13 ) );
		assertFalse( Double.isNaN( projection1.value( graph2.spot13 ) ) );
		assertNotEquals( 0, projection1.value( graph2.spot13 ) );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		SpotUmapFeature spotUmapFeatureReloaded;
		try (Context context = new Context())
		{
			spotUmapFeatureReloaded =
					( SpotUmapFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph2.getModel(), spotUmapFeature );
		}
		assertNotNull( spotUmapFeatureReloaded );
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( spotUmapFeature, spotUmapFeatureReloaded,
				Collections.singleton( graph2.spot13 ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN( getProjection( spotUmapFeature, spec0 ).value( graph2.spot13 ) ) );
		assertFalse( Double.isNaN( getProjection( spotUmapFeature, spec1 ).value( graph2.spot13 ) ) );

		// invalidate feature
		spotUmapFeature.invalidate( graph2.spot13 );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( spotUmapFeature, spec0 ).value( graph2.spot13 ) ) );
		assertTrue( Double.isNaN( getProjection( spotUmapFeature, spec1 ).value( graph2.spot13 ) ) );
	}
}
