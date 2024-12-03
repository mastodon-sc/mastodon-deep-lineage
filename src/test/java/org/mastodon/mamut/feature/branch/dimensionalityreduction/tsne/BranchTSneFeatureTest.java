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
package org.mastodon.mamut.feature.branch.dimensionalityreduction.tsne;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import net.imglib2.util.Cast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph7;
import org.mastodon.mamut.feature.branch.sinuosity.BranchSinuosityFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionAlgorithm;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionController;
import org.mastodon.mamut.feature.dimensionalityreduction.tsne.TSneSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

@Disabled( "mvn test takes too long" )
public class BranchTSneFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private BranchTSneFeature tSneFeature;

	private final ExampleGraph7 graph7 = new ExampleGraph7();

	private FeatureProjectionSpec spec0;

	private FeatureProjectionSpec spec1;

	@BeforeEach
	public void setUp()
	{
		try (Context context = new Context())
		{
			Model model = graph7.getModel();
			FeatureModel featureModel = model.getFeatureModel();

			// declare some features as input dimensions
			BranchDisplacementDurationFeature branchDisplacementDurationFeature = Cast.unchecked(
					FeatureComputerTestUtils.getFeature( context, model, BranchDisplacementDurationFeature.SPEC ) );
			featureModel.declareFeature( branchDisplacementDurationFeature );
			BranchSinuosityFeature branchSinuosityFeature = Cast.unchecked(
					FeatureComputerTestUtils.getFeature( context, model, BranchSinuosityFeature.BRANCH_SINUOSITY_FEATURE_SPEC ) );
			featureModel.declareFeature( branchSinuosityFeature );
			List< InputDimension< BranchSpot > > inputDimensions =
					InputDimension.getListFromFeatureModel( featureModel, BranchSpot.class, BranchLink.class );

			// set up the controller and compute the feature
			Supplier< List< InputDimension< BranchSpot > > > inputDimensionsSupplier = () -> inputDimensions;
			DimensionalityReductionController controller = new DimensionalityReductionController( graph7.getModel(), context );
			TSneSettings tSneSettings = controller.getTSneSettings();
			tSneSettings.setPerplexity( 10 );
			controller.setModelGraph( false );
			controller.setAlgorithm( DimensionalityReductionAlgorithm.TSNE );
			controller.computeFeature( inputDimensionsSupplier );
			tSneFeature = FeatureUtils.getFeature( graph7.getModel(), BranchTSneFeature.BranchSpotTSneFeatureSpec.class );
			assertNotNull( tSneFeature );
			spec0 = new FeatureProjectionSpec( tSneFeature.getProjectionName( 0 ), Dimension.NONE );
			spec1 = new FeatureProjectionSpec( tSneFeature.getProjectionName( 1 ), Dimension.NONE );

		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( tSneFeature );
		FeatureProjection< BranchSpot > projection0 = getProjection( tSneFeature, spec0 );
		FeatureProjection< BranchSpot > projection1 = getProjection( tSneFeature, spec1 );
		Iterator< BranchSpot > branchSpotIterator = graph7.getModel().getBranchGraph().vertices().iterator();
		BranchSpot branchSpot = branchSpotIterator.next();
		assertFalse( Double.isNaN( projection0.value( branchSpot ) ) );
		assertNotEquals( 0, projection0.value( branchSpot ) );
		assertFalse( Double.isNaN( projection1.value( branchSpot ) ) );
		assertNotEquals( 0, projection1.value( branchSpot ) );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchTSneFeature tSneFeatureReloaded;
		try (Context context = new Context())
		{
			tSneFeatureReloaded =
					( BranchTSneFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph7.getModel(), this.tSneFeature );
		}
		assertNotNull( tSneFeatureReloaded );
		Iterator< BranchSpot > branchSpotIterator = graph7.getModel().getBranchGraph().vertices().iterator();
		BranchSpot branchSpot = branchSpotIterator.next();
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( this.tSneFeature, tSneFeatureReloaded,
				Collections.singleton( branchSpot ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		Iterator< BranchSpot > branchSpotIterator = graph7.getModel().getBranchGraph().vertices().iterator();
		BranchSpot branchSpot = branchSpotIterator.next();

		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN( getProjection( tSneFeature, spec0 ).value( branchSpot ) ) );
		assertFalse( Double.isNaN( getProjection( tSneFeature, spec1 ).value( branchSpot ) ) );

		// invalidate feature
		tSneFeature.invalidate( branchSpot );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( tSneFeature, spec0 ).value( branchSpot ) ) );
		assertTrue( Double.isNaN( getProjection( tSneFeature, spec1 ).value( branchSpot ) ) );
	}
}
