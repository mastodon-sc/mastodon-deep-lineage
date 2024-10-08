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
package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import net.imglib2.util.Cast;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.util.UmapInputDimension;
import org.mastodon.mamut.feature.spot.dimensionalityreduction.umap.SpotUmapFeature;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.ui.coloring.feature.DefaultFeatureRangeCalculatorTest;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UmapControllerTest
{
	@Test
	void testSaveSettingsToPreferences()
	{
		try (Context context = new Context())
		{
			int numberOfOutputDimensions = 5;
			int numberOfNeighbors = 10;
			double minimumDistance = 0.5;
			boolean standardizeFeatures = false;

			Model model = new Model();
			UmapController umapController = new UmapController( model, context );
			UmapFeatureSettings umapFeatureSettings = umapController.getFeatureSettings();
			umapFeatureSettings.setNumberOfOutputDimensions( numberOfOutputDimensions );
			umapFeatureSettings.setNumberOfNeighbors( numberOfNeighbors );
			umapFeatureSettings.setMinimumDistance( minimumDistance );
			umapFeatureSettings.setStandardizeFeatures( standardizeFeatures );
			umapController.saveSettingsToPreferences();

			UmapController umapController2 = new UmapController( model, context );
			UmapFeatureSettings umapFeatureSettings2 = umapController2.getFeatureSettings();
			assertEquals( numberOfOutputDimensions, umapFeatureSettings2.getNumberOfOutputDimensions() );
			assertEquals( numberOfNeighbors, umapFeatureSettings2.getNumberOfNeighbors() );
			assertEquals( minimumDistance, umapFeatureSettings2.getMinimumDistance() );
			assertEquals( standardizeFeatures, umapFeatureSettings2.isStandardizeFeatures() );

			context.getService( PrefService.class ).clear( UmapController.class );
		}
	}

	@Test
	void testGetVertexType()
	{
		try (Context context = new Context())
		{
			Model model = new Model();
			UmapController umapController = new UmapController( model, context );
			assertEquals( Spot.class, umapController.getVertexType() );
			umapController.setModelGraph( false );
			assertEquals( BranchSpot.class, umapController.getVertexType() );
		}
	}

	@Test
	void testGetEdgeType()
	{
		try (Context context = new Context())
		{
			Model model = new Model();
			UmapController umapController = new UmapController( model, context );
			assertEquals( Link.class, umapController.getEdgeType() );
			umapController.setModelGraph( false );
			assertEquals( BranchLink.class, umapController.getEdgeType() );
		}
	}

	@Test
	void testIllegalArgumentExceptions()
	{
		try (Context context = new Context())
		{
			int numberOfOutputDimensions = 10;

			Model model = new Model();
			UmapController umapController = new UmapController( model, context );
			UmapFeatureSettings umapFeatureSettings = umapController.getFeatureSettings();
			umapFeatureSettings.setNumberOfOutputDimensions( numberOfOutputDimensions );
			List< UmapInputDimension< Spot > > umapInputDimensions =
					UmapInputDimension.getListFromFeatureModel( model.getFeatureModel(), Spot.class, Link.class );
			assertThrows( IllegalArgumentException.class, () -> umapController.computeFeature( () -> umapInputDimensions ) );
			Supplier< List< UmapInputDimension< Spot > > > emptyInputDimensionsSupplier = Collections::emptyList;
			assertThrows( IllegalArgumentException.class, () -> umapController.computeFeature( emptyInputDimensionsSupplier ) );
		}
	}

	@Test
	void testIllegalData()
	{
		ExampleGraph2 graph2 = new ExampleGraph2();
		DefaultFeatureRangeCalculatorTest.TestDoubleFeature testDoubleFeature = new DefaultFeatureRangeCalculatorTest.TestDoubleFeature(
				new DoublePropertyMap<>( graph2.getModel().getGraph().vertices().getRefPool(), Double.NaN ) );
		graph2.getModel().getFeatureModel().declareFeature( testDoubleFeature );

		try (Context context = new Context())
		{
			UmapController umapController = new UmapController( graph2.getModel(), context );
			UmapFeatureSettings settings = umapController.getFeatureSettings();
			settings.setNumberOfNeighbors( 5 );
			Supplier< List< UmapInputDimension< Spot > > > inputDimensionsSupplier =
					() -> UmapInputDimension.getListFromFeatureModel( graph2.getModel().getFeatureModel(), Spot.class, Link.class );
			assertThrows( IllegalArgumentException.class, () -> umapController.computeFeature( inputDimensionsSupplier ) );
		}
	}

	@Test
	void testPartiallyIllegalData()
	{
		ExampleGraph2 graph2 = new ExampleGraph2();
		FeatureModel featureModel = graph2.getModel().getFeatureModel();
		DefaultFeatureRangeCalculatorTest.TestDoubleFeature testDoubleFeature = new DefaultFeatureRangeCalculatorTest.TestDoubleFeature(
				new DoublePropertyMap<>( graph2.getModel().getGraph().vertices().getRefPool(), Double.NaN ) );
		featureModel.declareFeature( testDoubleFeature );
		testDoubleFeature.doubleValues.set( graph2.spot0, 5 );
		testDoubleFeature.doubleValues.set( graph2.spot1, 10 );
		testDoubleFeature.doubleValues.set( graph2.spot2, 15 );
		testDoubleFeature.doubleValues.set( graph2.spot3, 20 );
		testDoubleFeature.doubleValues.set( graph2.spot6, 35 );
		testDoubleFeature.doubleValues.set( graph2.spot7, 40 );
		testDoubleFeature.doubleValues.set( graph2.spot8, 45 );
		testDoubleFeature.doubleValues.set( graph2.spot10, 50 );
		testDoubleFeature.doubleValues.set( graph2.spot11, 55 );

		try (Context context = new Context())
		{
			UmapController umapController = new UmapController( graph2.getModel(), context );
			UmapFeatureSettings settings = umapController.getFeatureSettings();
			settings.setNumberOfNeighbors( 5 );
			Supplier< List< UmapInputDimension< Spot > > > inputDimensionsSupplier =
					() -> UmapInputDimension.getListFromFeatureModel( graph2.getModel().getFeatureModel(), Spot.class, Link.class );
			umapController.computeFeature( inputDimensionsSupplier );
			Feature< Spot > spotUmapFeature = Cast.unchecked( featureModel.getFeature( SpotUmapFeature.GENERIC_SPEC ) );
			Set< FeatureProjection< Spot > > projections = spotUmapFeature.projections();
			assertEquals( 2, projections.size() );
			FeatureProjection< Spot > projection0 = spotUmapFeature.projections().iterator().next();
			assertTrue( Double.isNaN( projection0.value( graph2.spot0 ) ) ); // NB: the link feature for spot0 is NaN, since it has no incoming edges
			assertFalse( Double.isNaN( projection0.value( graph2.spot1 ) ) );
			assertTrue( Double.isNaN( projection0.value( graph2.spot13 ) ) );
		}
	}

	@Test
	void testMultipleIncomingEdges()
	{
		Model model = new Model();
		ModelGraph graph = model.getGraph();
		Spot spot00 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 0 );
		Spot spot01 = graph.addVertex().init( 0, new double[] { 3, 4, 0 }, 0 );
		Spot spot02 = graph.addVertex().init( 0, new double[] { 6, 8, 0 }, 0 );
		Spot spot1 = graph.addVertex().init( 1, new double[] { 3, 4, 0 }, 0 );
		graph.addEdge( spot00, spot1 ).init();
		graph.addEdge( spot01, spot1 ).init();
		graph.addEdge( spot02, spot1 ).init();

		try (Context context = new Context())
		{
			UmapController umapController = new UmapController( model, context );
			FeatureModel featureModel = model.getFeatureModel();
			Supplier< List< UmapInputDimension< Spot > > > inputDimensionsSupplier =
					() -> UmapInputDimension.getListFromFeatureModel( featureModel, Spot.class, Link.class );
			umapController.computeFeature( inputDimensionsSupplier );
			Feature< Spot > spotUmapFeature = Cast.unchecked( featureModel.getFeature( SpotUmapFeature.GENERIC_SPEC ) );
			Set< FeatureProjection< Spot > > projections = spotUmapFeature.projections();
			assertEquals( 2, projections.size() );
			FeatureProjection< Spot > projection0 = spotUmapFeature.projections().iterator().next();
			assertFalse( Double.isNaN( projection0.value( spot1 ) ) );
		}
	}

}
