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
package org.mastodon.mamut.clustering;

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
import org.mastodon.mamut.TestUtils;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
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

class ClusterLineagesControllerTest
{
	@Test
	void testCreateTagSet() throws SpimDataException, IOException
	{
		try (Context context = new Context())
		{
			File tempFile = TestUtils.getTempFileCopy( "src/test/resources/org/mastodon/mamut/clustering/model1.mastodon", "model",
					".mastodon" );
			ProjectModel projectModel = ProjectLoader.open( tempFile.getAbsolutePath(), context, false, true );
			Model model = projectModel.getModel();

			String tagSetName = "Test Tag Set";
			TagSetUtils.addNewTagSetToModel( model, tagSetName, Collections.emptyList() );
			ClusterLineagesController controller = new ClusterLineagesController( projectModel );
			controller.setInputParams( CropCriteria.TIMEPOINT, 0, 100, 1 );
			controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );
			controller.setShowDendrogram( false );
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

			Set< String > expectedGroupNames = new HashSet<>( Arrays.asList( "Group 1", "Group 2", "Group 3" ) );
			Set< String > actualGroupNames = new HashSet<>( Arrays.asList( tag0.label(), tag1.label(), tag2.label() ) );

			Set< Integer > expectedSpotsPerGroup = new HashSet<>( Arrays.asList( 5, 13, 12 ) );
			Set< Integer > actualSpotsPerGroup = new HashSet<>( Arrays.asList( tag0Spots.size(), tag1Spots.size(), tag2Spots.size() ) );

			assertEquals( "Hierarchical clustering of lineages (time: 0-100, clusters: 3, min. div: 1) ", tagSet1.getName() );
			assertTrue( controller.isValidParams() );
			assertEquals( 2, tagSets.size() );
			assertEquals( 3, tags.size() );
			assertEquals( expectedGroupNames, actualGroupNames );
			assertEquals( expectedSpotsPerGroup, actualSpotsPerGroup );
		}
	}

	@Test
	void testCreateTagSetWithExternalProjects() throws IOException, SpimDataException
	{
		try (Context context = new Context())
		{
			File tempFile1 = TestUtils.getTempFileCopy( "src/test/resources/org/mastodon/mamut/clustering/model1.mastodon", "model",
					".mastodon" );
			File tempFile2 = TestUtils.getTempFileCopy( "src/test/resources/org/mastodon/mamut/clustering/model2.mastodon", "model",
					".mastodon" );

			ProjectModel projectModel1 = ProjectLoader.open( tempFile1.getAbsolutePath(), context, false, true );
			Model model1 = projectModel1.getModel();

			File[] files = { tempFile2 };

			String tagSetName = "Test Tag Set";
			TagSetUtils.addNewTagSetToModel( model1, tagSetName, Collections.emptyList() );
			PrefService prefService = context.getService( PrefService.class );
			ClusterLineagesController controller = new ClusterLineagesController( projectModel1, prefService, context );
			controller.setInputParams( CropCriteria.TIMEPOINT, 0, 100, 1 );
			controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );
			controller.setShowDendrogram( false );
			controller.setExternalProjects( files, true );
			controller.createTagSet();

			Set< String > expectedGroupNames = new HashSet<>( Arrays.asList( "Group 1", "Group 2", "Group 3" ) );
			Set< Integer > expectedSpotsPerGroup = new HashSet<>( Arrays.asList( 5, 13, 12 ) );

			assertTrue( controller.isValidParams() );
			assertClusteringEquals( model1, 1, expectedGroupNames, expectedSpotsPerGroup );
			ProjectModel pm2 = ProjectLoader.open( tempFile2.getAbsolutePath(), context, false, true );
			assertClusteringEquals( pm2.getModel(), 0, expectedGroupNames, expectedSpotsPerGroup );
		}
	}

	private void assertClusteringEquals( Model model, int existingTagSets, Set< String > expectedGroupNames,
			Set< Integer > expectedSpotsPerGroup )
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

		Set< String > actualGroupNames = new HashSet<>( Arrays.asList( tag0.label(), tag1.label(), tag2.label() ) );
		Set< Integer > actualSpotsPerGroup = new HashSet<>( Arrays.asList( tag0Spots.size(), tag1Spots.size(), tag2Spots.size() ) );

		assertEquals( "Average Hierarchical clustering of lineages (time: 0-100, clusters: 3, min. div: 1) ", tagSet.getName() );
		assertEquals( expectedTagSets, tagSets.size() );
		assertEquals( 3, tags1.size() );
		assertEquals( expectedGroupNames, actualGroupNames );
		assertEquals( expectedSpotsPerGroup, actualSpotsPerGroup );
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
			ClusterLineagesController controller = new ClusterLineagesController( projectModel, prefService, context );
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
		ClusterLineagesController controller = new ClusterLineagesController( projectModel );
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
		ClusterLineagesController controller = new ClusterLineagesController( projectModel );
		controller.setInputParams( CropCriteria.TIMEPOINT, 1, 10, 1 );
		controller.setComputeParams( SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 );

		assertEquals(
				"Crop criterion: Timepoint, Crop start: 1, Crop end: 10, Number of clusters: 3, Minimum cell divisions: 1, Similarity measure: Normalized Zhang Tree Distance, Clustering method: Average linkage, Resulting lineage trees: 1",
				controller.getParameters() );
	}

}
