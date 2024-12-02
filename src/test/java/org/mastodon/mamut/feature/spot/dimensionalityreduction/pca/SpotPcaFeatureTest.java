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
package org.mastodon.mamut.feature.spot.dimensionalityreduction.pca;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph7;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionAlgorithm;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionController;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

public class SpotPcaFeatureTest extends AbstractFeatureTest< Spot >
{
	private SpotPcaFeature spotPcaFeature;

	private final ExampleGraph7 graph7 = new ExampleGraph7();

	private FeatureProjectionSpec spec0;

	private FeatureProjectionSpec spec1;

	@BeforeEach
	public void setUp()
	{
		try (Context context = new Context())
		{
			FeatureModel featureModel = graph7.getModel().getFeatureModel();
			DimensionalityReductionController controller = new DimensionalityReductionController( graph7.getModel(), context );
			controller.setAlgorithm( DimensionalityReductionAlgorithm.PCA );
			Supplier< List< InputDimension< Spot > > > inputDimensionsSupplier =
					() -> InputDimension.getListFromFeatureModel( featureModel, Spot.class, Link.class );
			controller.computeFeature( inputDimensionsSupplier );
			spotPcaFeature = FeatureUtils.getFeature( graph7.getModel(), SpotPcaFeature.SpotPcaFeatureSpec.class );
			assertNotNull( spotPcaFeature );
			spec0 = new FeatureProjectionSpec( spotPcaFeature.getProjectionName( 0 ), Dimension.NONE );
			spec1 = new FeatureProjectionSpec( spotPcaFeature.getProjectionName( 1 ), Dimension.NONE );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( spotPcaFeature );
		FeatureProjection< Spot > projection0 = getProjection( spotPcaFeature, spec0 );
		FeatureProjection< Spot > projection1 = getProjection( spotPcaFeature, spec1 );
		Iterator< Spot > spotIterator = graph7.getModel().getGraph().vertices().iterator();
		Spot spot0 = spotIterator.next();
		assertTrue( Double.isNaN( projection0.value( spot0 ) ) );
		assertTrue( Double.isNaN( projection1.value( spot0 ) ) );
		Spot spot1 = spotIterator.next();
		assertFalse( Double.isNaN( projection0.value( spot1 ) ) );
		assertFalse( Double.isNaN( projection1.value( spot1 ) ) );
		assertNotEquals( 0, projection0.value( spot1 ) );
		assertNotEquals( 0, projection1.value( spot1 ) );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		SpotPcaFeature spotPcaFeatureReloaded;
		try (Context context = new Context())
		{
			spotPcaFeatureReloaded =
					( SpotPcaFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph7.getModel(), spotPcaFeature );
		}
		assertNotNull( spotPcaFeatureReloaded );
		// check that the feature has correct values after saving and reloading
		Iterator< Spot > spotIterator = graph7.getModel().getGraph().vertices().iterator();
		spotIterator.next();
		Spot spot1 = spotIterator.next();
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality(
				spotPcaFeature, spotPcaFeatureReloaded,
				Collections.singleton( spot1 ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		Iterator< Spot > spotIterator = graph7.getModel().getGraph().vertices().iterator();
		spotIterator.next();
		Spot spot1 = spotIterator.next();
		assertFalse( Double.isNaN( getProjection( spotPcaFeature, spec0 ).value( spot1 ) ) );
		assertFalse( Double.isNaN( getProjection( spotPcaFeature, spec1 ).value( spot1 ) ) );

		// invalidate feature
		spotPcaFeature.invalidate( spot1 );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( spotPcaFeature, spec0 ).value( spot1 ) ) );
		assertTrue( Double.isNaN( getProjection( spotPcaFeature, spec1 ).value( spot1 ) ) );
	}
}
