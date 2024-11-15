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
package org.mastodon.mamut.feature.branch.dimensionalityreduction.umap;

import net.imglib2.util.Cast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.branch.BranchDepthFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.branch.sinuosity.BranchSinuosityFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionController;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BranchUmapFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private BranchUmapFeature umapFeature;

	private final ExampleGraph2 graph2 = new ExampleGraph2();

	private FeatureProjectionSpec spec0;

	private FeatureProjectionSpec spec1;

	@BeforeEach
	public void setUp()
	{
		try (Context context = new Context())
		{
			Model model = graph2.getModel();
			FeatureModel featureModel = model.getFeatureModel();

			// declare some features as input dimensions
			BranchDepthFeature branchDepthFeature =
					new BranchDepthFeature( new IntPropertyMap<>( model.getBranchGraph().vertices().getRefPool(), -1 ) );
			featureModel.declareFeature( branchDepthFeature );
			BranchSinuosityFeature branchSinuosityFeature = Cast.unchecked(
					FeatureComputerTestUtils.getFeature( context, model, BranchSinuosityFeature.BRANCH_SINUOSITY_FEATURE_SPEC ) );
			featureModel.declareFeature( branchSinuosityFeature );
			List< InputDimension< BranchSpot > > inputDimensions =
					InputDimension.getListFromFeatureModel( featureModel, BranchSpot.class, BranchLink.class );

			// set up the controller and compute the feature
			Supplier< List< InputDimension< BranchSpot > > > inputDimensionsSupplier = () -> inputDimensions;
			DimensionalityReductionController controller = new DimensionalityReductionController( graph2.getModel(), context );
			UmapSettings umapSettings = controller.getUmapSettings();
			umapSettings.setNumberOfNeighbors( 3 );
			controller.setModelGraph( false );
			controller.computeFeature( inputDimensionsSupplier );
			umapFeature = FeatureUtils.getFeature( graph2.getModel(), BranchUmapFeature.BranchSpotUmapFeatureSpec.class );
			assertNotNull( umapFeature );
			spec0 = new FeatureProjectionSpec( umapFeature.getProjectionName( 0 ), Dimension.NONE );
			spec1 = new FeatureProjectionSpec( umapFeature.getProjectionName( 1 ), Dimension.NONE );

		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( umapFeature );
		FeatureProjection< BranchSpot > projection0 = getProjection( umapFeature, spec0 );
		FeatureProjection< BranchSpot > projection1 = getProjection( umapFeature, spec1 );
		assertFalse( Double.isNaN( projection0.value( graph2.branchSpotA ) ) );
		assertNotEquals( 0, projection0.value( graph2.branchSpotA ) );
		assertFalse( Double.isNaN( projection1.value( graph2.branchSpotA ) ) );
		assertNotEquals( 0, projection1.value( graph2.branchSpotA ) );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchUmapFeature umapFeatureReloaded;
		try (Context context = new Context())
		{
			umapFeatureReloaded =
					( BranchUmapFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph2.getModel(), this.umapFeature );
		}
		assertNotNull( umapFeatureReloaded );
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( this.umapFeature, umapFeatureReloaded,
				Collections.singleton( graph2.branchSpotA ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN( getProjection( umapFeature, spec0 ).value( graph2.branchSpotA ) ) );
		assertFalse( Double.isNaN( getProjection( umapFeature, spec1 ).value( graph2.branchSpotA ) ) );

		// invalidate feature
		umapFeature.invalidate( graph2.branchSpotA );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( umapFeature, spec0 ).value( graph2.branchSpotA ) ) );
		assertTrue( Double.isNaN( getProjection( umapFeature, spec1 ).value( graph2.branchSpotA ) ) );
	}
}
