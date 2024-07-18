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
package org.mastodon.mamut.classification;

import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.classification.config.ClusteringMethod;
import org.mastodon.mamut.classification.config.CropCriteria;
import org.mastodon.mamut.classification.config.SimilarityMeasure;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.MastodonProjectService;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassifyLineagesControllerTest
{
	@Test
	void testCreateTagSet()
	{
		final Model model = new Model();

		try (Context context = new Context())
		{
			final Img< FloatType > dummyImg = ArrayImgs.floats( 1, 1, 1 );
			final ImagePlus dummyImagePlus =
					ImgToVirtualStack.wrap( new ImgPlus<>( dummyImg, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z } ) );
			SharedBigDataViewerData dummyBdv = Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( dummyImagePlus ) );
			ProjectModel projectModel = ProjectModel.create( context, model, dummyBdv, null );

			final ModelGraph modelGraph = model.getGraph();

			addLineageTree11( modelGraph );
			addLineageTree21( modelGraph );
			addLineageTree31( modelGraph );
			addLineageTree41( modelGraph );
			addLineageTree51( modelGraph );
			addEmptyTree( modelGraph );

			String tagSetName = "Test Tag Set";
			TagSetUtils.addNewTagSetToModel( model, tagSetName, Collections.emptyList() );
			ClassifyLineagesController controller = new ClassifyLineagesController( projectModel );
			controller.setInputParams( CropCriteria.TIMEPOINT, 0, 100, 1 );
			controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );
			controller.setShowDendrogram( true ); // NB: increase test coverage
			controller.createTagSet();

			List< TagSetStructure.TagSet > tagSets = model.getTagSetModel().getTagSetStructure().getTagSets();
			TagSetStructure.TagSet tagSet1 = model.getTagSetModel().getTagSetStructure().getTagSets().get( 1 );
			List< TagSetStructure.Tag > tags = tagSet1.getTags();
			TagSetStructure.Tag tag0 = tags.get( 0 );
			TagSetStructure.Tag tag1 = tags.get( 1 );
			TagSetStructure.Tag tag2 = tags.get( 2 );

			Collection< Spot > tag0Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag0 );
			Collection< Spot > tag1Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag1 );
			Collection< Spot > tag2Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag2 );

			Set< String > expectedClassNames = new HashSet<>( Arrays.asList( "Class 1", "Class 2", "Class 3" ) );
			Set< String > actualClassNames = new HashSet<>( Arrays.asList( tag0.label(), tag1.label(), tag2.label() ) );

			Set< Integer > expectedClassCounts = new HashSet<>( Arrays.asList( 9, 12, 14 ) );
			Set< Integer > actualClassCounts = new HashSet<>( Arrays.asList( tag0Spots.size(), tag1Spots.size(), tag2Spots.size() ) );

			assertEquals( "Classification (time: 0-100, classes: 3, min. div: 1) ", tagSet1.getName() );
			assertTrue( controller.isValidParams() );
			assertEquals( 2, tagSets.size() );
			assertEquals( 3, tags.size() );
			assertEquals( expectedClassNames, actualClassNames );
			assertEquals( expectedClassCounts, actualClassCounts );
		}
	}

	@Test
	void testCreateTagSetWithExternalProjects() throws IOException, SpimDataException
	{
		final Model model1 = new Model();
		final Model model2 = new Model();

		try (Context context = new Context())
		{
			final Img< FloatType > dummyImg = ArrayImgs.floats( 1, 1, 1 );
			final ImagePlus dummyImagePlus =
					ImgToVirtualStack.wrap( new ImgPlus<>( dummyImg, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z } ) );
			SharedBigDataViewerData dummyBdv = Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( dummyImagePlus ) );
			ProjectModel projectModel1 = ProjectModel.create( context, model1, dummyBdv, null );

			final ModelGraph modelGraph1 = model1.getGraph();
			final ModelGraph modelGraph2 = model2.getGraph();

			addLineageTree11( modelGraph1 );
			addLineageTree21( modelGraph1 );
			addLineageTree31( modelGraph1 );
			addLineageTree41( modelGraph1 );
			addLineageTree51( modelGraph1 );
			addEmptyTree( modelGraph1 );

			addLineageTree12( modelGraph2 );
			addLineageTree22( modelGraph2 );
			addLineageTree32( modelGraph2 );
			addLineageTree42( modelGraph2 );
			addLineageTree52( modelGraph2 );
			addEmptyTree( modelGraph2 );

			File file2 = DemoUtils.saveAppModelToTempFile( dummyImg, model2 );
			File[] files = { file2 };

			String tagSetName = "Test Tag Set";
			TagSetUtils.addNewTagSetToModel( model1, tagSetName, Collections.emptyList() );
			PrefService prefService = context.getService( PrefService.class );
			MastodonProjectService projectService = context.getService( MastodonProjectService.class );
			ClassifyLineagesController controller = new ClassifyLineagesController( projectModel1, prefService, projectService );
			controller.setInputParams( CropCriteria.TIMEPOINT, 0, 100, 1 );
			controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );
			controller.setShowDendrogram( false );
			controller.setExternalProjects( files, true );
			controller.createTagSet();

			Set< String > expectedClassNames = new HashSet<>( Arrays.asList( "Class 1", "Class 2", "Class 3" ) );
			Set< Integer > expectedClassCounts = new HashSet<>( Arrays.asList( 9, 12, 14 ) );

			assertTrue( controller.isValidParams() );
			assertClassificationEquals( model1, 1, expectedClassNames, expectedClassCounts );
			ProjectModel pm2 = ProjectLoader.open( file2.getAbsolutePath(), context, false, true );
			assertClassificationEquals( pm2.getModel(), 0, expectedClassNames, expectedClassCounts );
		}
	}

	private void assertClassificationEquals( Model model, int existingTagSets, Set< String > expectedClassNames,
			Set< Integer > expectedClassCounts )
	{
		List< TagSetStructure.TagSet > tagSets = model.getTagSetModel().getTagSetStructure().getTagSets();
		int expectedTagSets = existingTagSets + 1;
		TagSetStructure.TagSet tagSet = tagSets.get( existingTagSets );
		List< TagSetStructure.Tag > tags1 = tagSet.getTags();
		TagSetStructure.Tag tag0 = tags1.get( 0 );
		TagSetStructure.Tag tag1 = tags1.get( 1 );
		TagSetStructure.Tag tag2 = tags1.get( 2 );

		Collection< Spot > tag0Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag0 );
		Collection< Spot > tag1Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag1 );
		Collection< Spot > tag2Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag2 );

		Set< String > actualClassNames = new HashSet<>( Arrays.asList( tag0.label(), tag1.label(), tag2.label() ) );
		Set< Integer > actualClassCounts = new HashSet<>( Arrays.asList( tag0Spots.size(), tag1Spots.size(), tag2Spots.size() ) );

		assertEquals( "Average Classification (time: 0-100, classes: 3, min. div: 1) ", tagSet.getName() );
		assertEquals( expectedTagSets, tagSets.size() );
		assertEquals( 3, tags1.size() );
		assertEquals( expectedClassNames, actualClassNames );
		assertEquals( expectedClassCounts, actualClassCounts );
	}

	@Test
	void testSetExternalProjects() throws IOException
	{
		Model model = new Model();
		try (Context context = new Context())
		{
			final Img< FloatType > dummyImg = ArrayImgs.floats( 1, 1, 1 );
			final ImagePlus dummyImagePlus =
					ImgToVirtualStack.wrap( new ImgPlus<>( dummyImg, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z } ) );
			SharedBigDataViewerData dummyBdv = Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( dummyImagePlus ) );
			ProjectModel projectModel = ProjectModel.create( context, model, dummyBdv, null );

			File file1 = DemoUtils.saveAppModelToTempFile( dummyImg, model );
			File file2 = DemoUtils.saveAppModelToTempFile( dummyImg, model );
			File file3 = File.createTempFile( "test", ".mastodon" );
			File[] files = { file1, file2 };

			PrefService prefService = context.getService( PrefService.class );
			MastodonProjectService projectService = context.getService( MastodonProjectService.class );
			ClassifyLineagesController controller = new ClassifyLineagesController( projectModel, prefService, projectService );
			controller.setInputParams( CropCriteria.TIMEPOINT, 0, 100, 1 );
			controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 0 );
			controller.setExternalProjects( files, false );
			assertEquals( 0, controller.getFeedback().size() );
			controller.setExternalProjects( null, false );
			assertEquals( 0, controller.getFeedback().size() );
			files = new File[] { file1, file3 };
			controller.setExternalProjects( files, false );
			assertEquals( 1, controller.getFeedback().size() );
			File[] files2 = { file1, file2 };
			controller.setExternalProjects( files2, false );
			assertEquals( 0, controller.getFeedback().size() );
			File[] files3 = { file1 };
			controller.setExternalProjects( files3, false );
			assertEquals( 0, controller.getFeedback().size() );
		}
	}

	@Test
	void testGetFeedback()
	{
		ExampleGraph2 exampleGraph = new ExampleGraph2();
		ProjectModel projectModel = DummyProjectModel.createModel( exampleGraph.getModel() );
		ClassifyLineagesController controller = new ClassifyLineagesController( projectModel );
		controller.setInputParams( CropCriteria.TIMEPOINT, 1, 0, 1 );
		controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );
		controller.setShowDendrogram( false );
		assertEquals( 2, controller.getFeedback().size() );
		assertFalse( controller.isValidParams() );
		assertThrows( IllegalArgumentException.class, controller::createTagSet );

		controller.setInputParams( CropCriteria.NUMBER_OF_SPOTS, 5, 10, 0 );
		assertEquals( 3, controller.getFeedback().size() );
		controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 2 );
		controller.setInputParams( CropCriteria.NUMBER_OF_SPOTS, 0, 3, 0 );
		assertEquals( 1, controller.getFeedback().size() );
	}

	@Test
	void testGetParameters()
	{
		ExampleGraph2 exampleGraph = new ExampleGraph2();
		ProjectModel projectModel = DummyProjectModel.createModel( exampleGraph.getModel() );
		ClassifyLineagesController controller = new ClassifyLineagesController( projectModel );
		controller.setInputParams( CropCriteria.TIMEPOINT, 1, 10, 1 );
		controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );

		assertEquals(
				"Crop criterion: Timepoint, Crop start: 1, Crop end: 10, Number of classes: 3, Minimum cell divisions: 1, Similarity measure: Normalized Zhang Tree Distance, Clustering method: Average linkage, Resulting lineage trees: 1",
				controller.getParameters() );
	}

	/**
	 * <pre>
	 *                             branchSpot1(lifespan=20)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  branchSpot2(lifespan=10)    branchSpot3(lifespan=30)
	 * </pre>
	 */
	private static void addLineageTree11( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 20, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 20, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 20, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 50, new double[ 3 ], 0 );

		spot1.setLabel( "tree1" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	/**
	 * <pre>
	 *                             branchSpot1(lifespan=21)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  branchSpot2(lifespan=10)    branchSpot3(lifespan=30)
	 * </pre>
	 */
	private static void addLineageTree12( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 21, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 21, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 31, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 21, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 51, new double[ 3 ], 0 );

		spot1.setLabel( "tree1" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	/**
	 * <pre>
	 *                               branchSpot1(lifespan=30)
	 *                      ┌-─────────┴─────────────┐
	 *                      │                        │
	 *                    branchSpot2(lifespan=10) branchSpot3(lifespan=20)
	 * </pre>
	 */
	public static void addLineageTree21( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 40, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 50, new double[ 3 ], 0 );

		spot1.setLabel( "tree2" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	/**
	 * <pre>
	 *                               branchSpot1(lifespan=31)
	 *                      ┌-─────────┴─────────────┐
	 *                      │                        │
	 *                    branchSpot2(lifespan=10) branchSpot3(lifespan=20)
	 * </pre>
	 */
	public static void addLineageTree22( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 31, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 41, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 31, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 51, new double[ 3 ], 0 );

		spot1.setLabel( "tree2" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	/**
	 * <pre>
	 *                              branchSpot1(lifespan=1)
	 *                     ┌-─────────┴─────────────┐
	 *                     │                        │
	 *                   branchSpot2(lifespan=1)  branchSpot3(lifespan=1)
	 *          ┌-─────────┴─────────────┐
	 *          │                        │
	 *        branchSpot4(lifespan=1)  branchSpot5(lifespan=100)
	 * </pre>
	 */
	private static void addLineageTree31( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex().init( 102, new double[ 3 ], 0 );

		spot1.setLabel( "tree3" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );
	}

	/**
	 * <pre>
	 *                              branchSpot1(lifespan=1)
	 *                     ┌-─────────┴─────────────┐
	 *                     │                        │
	 *                   branchSpot2(lifespan=1)  branchSpot3(lifespan=1)
	 *          ┌-─────────┴─────────────┐
	 *          │                        │
	 *        branchSpot4(lifespan=1)  branchSpot5(lifespan=101)
	 * </pre>
	 */
	private static void addLineageTree32( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex().init( 103, new double[ 3 ], 0 );

		spot1.setLabel( "tree3" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );
	}

	/**
	 * <pre>
	 *                       branchSpot1(lifespan=3)
	 *              ┌-─────────┴─────────────────────────────────────────┐
	 *              │                                                    │
	 *            branchSpot2(lifespan=8)                     branchSpot3(lifespan=8)
	 *  ┌-───────────┴─────────────┐                        ┌-───────────┴─────────────┐
	 * branchSpot4(lifespan=4)   branchSpot5(lifespan=4)  branchSpot6(lifespan=1)   branchSpot7(lifespan=2)
	 *
	 * </pre>
	 */
	private static void addLineageTree41( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex().init( 15, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex().init( 15, new double[ 3 ], 0 );

		Spot spot11 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot12 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot13 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot14 = modelGraph.addVertex().init( 13, new double[ 3 ], 0 );

		spot1.setLabel( "tree4" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelGraph.addEdge( spot4, spot11 );
		modelGraph.addEdge( spot4, spot13 );
		modelGraph.addEdge( spot11, spot12 );
		modelGraph.addEdge( spot13, spot14 );
	}

	/**
	 * <pre>
	 *                       branchSpot1(lifespan=3)
	 *              ┌-─────────┴─────────────────────────────────────────┐
	 *              │                                                    │
	 *            branchSpot2(lifespan=9)                     branchSpot3(lifespan=9)
	 *  ┌-───────────┴─────────────┐                        ┌-───────────┴─────────────┐
	 * branchSpot4(lifespan=4)   branchSpot5(lifespan=4)  branchSpot6(lifespan=1)   branchSpot7(lifespan=2)
	 *
	 * </pre>
	 */
	private static void addLineageTree42( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex().init( 16, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex().init( 16, new double[ 3 ], 0 );

		Spot spot11 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot12 = modelGraph.addVertex().init( 13, new double[ 3 ], 0 );
		Spot spot13 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot14 = modelGraph.addVertex().init( 14, new double[ 3 ], 0 );

		spot1.setLabel( "tree4" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelGraph.addEdge( spot4, spot11 );
		modelGraph.addEdge( spot4, spot13 );
		modelGraph.addEdge( spot11, spot12 );
		modelGraph.addEdge( spot13, spot14 );
	}

	/**
	 * <pre>
	 *                             branchSpot1(lifespan=20)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  branchSpot2(lifespan=10)    branchSpot3(lifespan=30)
	 * </pre>
	 */
	private static void addLineageTree51( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 101, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 121, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 121, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 131, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 121, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 151, new double[ 3 ], 0 );

		spot1.setLabel( "tree5" );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	/**
	 * <pre>
	 *                             branchSpot1(lifespan=21)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  branchSpot2(lifespan=10)    branchSpot3(lifespan=30)
	 * </pre>
	 */
	private static void addLineageTree52( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 101, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 122, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 122, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 132, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 122, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 152, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		spot1.setLabel( "tree5" );
	}

	private static void addEmptyTree( final ModelGraph modelGraph )
	{
		modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
	}

}
