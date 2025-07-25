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
package org.mastodon.mamut.feature.spot.dimensionalityreduction.tsne;

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
import org.junit.jupiter.api.Disabled;
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

@Disabled( "mvn test takes too long" )
class SpotTSneFeatureTest extends AbstractFeatureTest< Spot >
{
	private SpotTSneFeature spotTSneFeature;

	private final ExampleGraph7 graph7 = new ExampleGraph7();

	private FeatureProjectionSpec spec0;

	private FeatureProjectionSpec spec1;

	@BeforeEach
	void setUp()
	{
		try (Context context = new Context())
		{
			FeatureModel featureModel = graph7.getModel().getFeatureModel();
			DimensionalityReductionController controller = new DimensionalityReductionController( graph7.getModel(), context );
			controller.setAlgorithm( DimensionalityReductionAlgorithm.TSNE );
			Supplier< List< InputDimension< Spot > > > inputDimensionsSupplier =
					() -> InputDimension.getListFromFeatureModel( featureModel, Spot.class, Link.class );
			controller.computeFeature( inputDimensionsSupplier );
			spotTSneFeature = FeatureUtils.getFeature( graph7.getModel(), SpotTSneFeature.SpotTSneFeatureSpec.class );
			assertNotNull( spotTSneFeature );
			spec0 = new FeatureProjectionSpec( spotTSneFeature.getProjectionName( 0 ), Dimension.NONE );
			spec1 = new FeatureProjectionSpec( spotTSneFeature.getProjectionName( 1 ), Dimension.NONE );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( spotTSneFeature );
		FeatureProjection< Spot > projection0 = getProjection( spotTSneFeature, spec0 );
		FeatureProjection< Spot > projection1 = getProjection( spotTSneFeature, spec1 );
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
		SpotTSneFeature spotTSneFeatureReloaded;
		try (Context context = new Context())
		{
			spotTSneFeatureReloaded =
					( SpotTSneFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph7.getModel(), spotTSneFeature );
		}
		assertNotNull( spotTSneFeatureReloaded );
		// check that the feature has correct values after saving and reloading
		Iterator< Spot > spotIterator = graph7.getModel().getGraph().vertices().iterator();
		spotIterator.next();
		Spot spot1 = spotIterator.next();
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( spotTSneFeature, spotTSneFeatureReloaded,
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
		assertFalse( Double.isNaN( getProjection( spotTSneFeature, spec0 ).value( spot1 ) ) );
		assertFalse( Double.isNaN( getProjection( spotTSneFeature, spec1 ).value( spot1 ) ) );

		// invalidate feature
		spotTSneFeature.invalidate( spot1 );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( spotTSneFeature, spec0 ).value( spot1 ) ) );
		assertTrue( Double.isNaN( getProjection( spotTSneFeature, spec1 ).value( spot1 ) ) );
	}
}
