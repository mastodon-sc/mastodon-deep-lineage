package org.mastodon.mamut.lineagemotifs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.TestUtils;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetStructure;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mpicbg.spim.data.SpimDataException;

class LineageMotifsUtilsTest
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final ExampleGraph1 graph1 = new ExampleGraph1();

	private final ExampleGraph2 graph2 = new ExampleGraph2();

	@Test
	void testGetNumberOfDivisions_NoDivisions()
	{
		Model model = graph1.getModel();
		int division = LineageMotifsUtils.getNumberOfDivisions( new BranchSpotTree( graph1.branchSpotA, 0, 3, model ) );
		assertEquals( 0, division );
	}

	@Test
	void testGetNumberOfDivisions_MultipleDivisions()
	{
		Model model = graph2.getModel();
		int division = LineageMotifsUtils.getNumberOfDivisions( new BranchSpotTree( graph2.branchSpotA, 0, 7, model ) );
		assertEquals( 2, division );
	}

	@Test
	void testGetSelectedMotif()
	{
		try (final Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph2.getModel(), context );

			SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();
			selectionModel.setSelected( graph2.spot2, true );
			selectionModel.setSelected( graph2.spot3, true );
			selectionModel.setSelected( graph2.spot4, true );
			selectionModel.setSelected( graph2.spot5, true );
			selectionModel.setSelected( graph2.spot8, true );

			BranchSpotTree motif = LineageMotifsUtils.getSelectedMotif( projectModel.getModel(), selectionModel );

			assertEquals( graph2.branchSpotA, motif.getBranchSpot() );
			assertEquals( 2, motif.getStartTimepoint() );
			assertEquals( 5, motif.getEndTimepoint() );
		}
	}

	@Test
	void testGetSelectedMotif_EmptySelection()
	{
		try (final Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph2.getModel(), context );

			Model model = projectModel.getModel();
			SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();
			assertThrows( InvalidLineageMotifSelection.class,
					() -> LineageMotifsUtils.getSelectedMotif( model, selectionModel ) );
		}
	}

	@Test
	void testGetSelectedMotif_MultipleMotifs()
	{
		try (final Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph2.getModel(), context );

			Model model = projectModel.getModel();
			SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();
			selectionModel.setSelected( graph2.spot2, true );
			selectionModel.setSelected( graph2.spot4, true );
			selectionModel.setSelected( graph2.spot5, true );
			selectionModel.setSelected( graph2.spot6, true );
			selectionModel.setSelected( graph2.spot7, true );
			selectionModel.setSelected( graph2.spot8, true );
			selectionModel.setSelected( graph2.spot10, true );
			selectionModel.setSelected( graph2.spot11, true );

			assertThrows( InvalidLineageMotifSelection.class,
					() -> LineageMotifsUtils.getSelectedMotif( model, selectionModel ) );
		}
	}

	@Test
	void testGetMostSimilarMotifs() throws IOException, SpimDataException
	{
		try (final Context context = new Context())
		{
			File tempFile1 = TestUtils.getTempFileCopy(
					"src/test/resources/org/mastodon/mamut/lineagemotifs/util/lineage_motifs_small_medium_large.mastodon", "model",
					".mastodon"
			);
			ProjectModel projectModel = ProjectLoader.open( tempFile1.getAbsolutePath(), context, false, true );
			Model model = projectModel.getModel();
			Spot spotRef = model.getGraph().vertexRef();
			BranchSpot branchSpotRef = model.getBranchGraph().vertexRef();
			try
			{
				SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();

				List< String > list = Arrays.asList( "small a", "749", "750", "751", "752", "753", "754", "755", "756", "757", "758", "759",
						"760", "761", "762", "763", "764", "765" );
				for ( Spot spot : model.getGraph().vertices() )
				{
					if ( list.contains( spot.getLabel() ) )
						selectionModel.setSelected( spot, true );
				}
				BranchSpotTree motifSmallA = LineageMotifsUtils.getSelectedMotif( model, selectionModel );
				int motifLength = LineageMotifsUtils.getMotifLength( motifSmallA );
				logger.debug( "motif length: {}", motifLength );

				Spot smallB = model.getGraph().vertices().stream().filter( spot -> spot.getLabel().equals( "small b" ) ).findFirst()
						.orElseThrow( () -> new IllegalStateException( "Spot with label 'small b' not found." ) );
				BranchSpot branchSpotSmallB = model.getBranchGraph().getBranchVertex( smallB, branchSpotRef );
				BranchSpotTree motifSmallB = new BranchSpotTree( branchSpotSmallB, 0, 11, model );

				Spot medium = model.getGraph().vertices().stream().filter( spot -> spot.getLabel().equals( "medium" ) ).findFirst()
						.orElseThrow( () -> new IllegalStateException( "Spot with label 'medium' not found." ) );
				BranchSpot branchSpotMedium = model.getBranchGraph().getBranchVertex( medium, branchSpotRef );
				BranchSpotTree motifMedium = new BranchSpotTree( branchSpotMedium, 0, 20, model );

				Spot large = model.getGraph().vertices().stream().filter( spot -> spot.getLabel().equals( "large" ) ).findFirst()
						.orElseThrow( () -> new IllegalStateException( "Spot with label 'large' not found." ) );
				BranchSpot branchSpotLarge = model.getBranchGraph().getBranchVertex( large, branchSpotRef );
				BranchSpotTree motifLarge = new BranchSpotTree( branchSpotLarge, 0, 30, model );

				double distanceSmallASmallB = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE.compute( motifSmallA, motifSmallB );
				logger.debug( "normalized zhang, distance small a -> small b: {}", distanceSmallASmallB );
				double distanceSmallAMedium = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE.compute( motifSmallA, motifMedium );
				logger.debug( "normalized zhang, distance small a -> medium: {}", distanceSmallAMedium );
				double distanceSmallALarge = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE.compute( motifSmallA, motifLarge );
				logger.debug( "normalized zhang, distance small a -> large: {}", distanceSmallALarge );
				assertTrue( distanceSmallASmallB <= distanceSmallAMedium );
				assertTrue( distanceSmallAMedium <= distanceSmallALarge );

				distanceSmallASmallB =
						SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE_WITH_LOCAL_NORMALIZATION.compute( motifSmallA, motifSmallB );
				logger.debug( "normalized zhang with local normalization, distance small a -> small b: {}", distanceSmallASmallB );
				distanceSmallAMedium =
						SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE_WITH_LOCAL_NORMALIZATION.compute( motifSmallA, motifMedium );
				logger.debug( "normalized zhang with local normalization, distance small a -> medium: {}", distanceSmallAMedium );
				distanceSmallALarge =
						SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE_WITH_LOCAL_NORMALIZATION.compute( motifSmallA, motifLarge );
				logger.debug( "normalized zhang with local normalization, distance small a -> large: {}", distanceSmallALarge );
				assertTrue( distanceSmallASmallB <= distanceSmallAMedium );
				assertTrue( distanceSmallAMedium <= distanceSmallALarge );

				distanceSmallASmallB = SimilarityMeasure.PER_BRANCH_ZHANG_DISTANCE.compute( motifSmallA, motifMedium );
				logger.debug( "per branch zhang, distance small a -> small b: {}", distanceSmallASmallB );
				distanceSmallAMedium = SimilarityMeasure.PER_BRANCH_ZHANG_DISTANCE.compute( motifSmallA, motifMedium );
				logger.debug( "per branch zhang, distance small a -> medium: {}", distanceSmallAMedium );
				distanceSmallALarge = SimilarityMeasure.PER_BRANCH_ZHANG_DISTANCE.compute( motifSmallA, motifLarge );
				logger.debug( "per branch zhang, distance small a -> large: {}", distanceSmallALarge );
				assertTrue( distanceSmallASmallB <= distanceSmallAMedium );
				assertTrue( distanceSmallAMedium <= distanceSmallALarge );

				distanceSmallASmallB = SimilarityMeasure.ZHANG_DISTANCE.compute( motifSmallA, motifSmallB );
				logger.debug( "zhang, distance small a -> small b: {}", distanceSmallASmallB );
				distanceSmallAMedium = SimilarityMeasure.ZHANG_DISTANCE.compute( motifSmallA, motifMedium );
				logger.debug( "zhang, distance small a -> medium: {}", distanceSmallAMedium );
				distanceSmallALarge = SimilarityMeasure.ZHANG_DISTANCE.compute( motifSmallA, motifLarge );
				logger.debug( "zhang, distance small a -> large: {}", distanceSmallALarge );
				assertTrue( distanceSmallASmallB <= distanceSmallAMedium );
				assertTrue( distanceSmallAMedium <= distanceSmallALarge );

				distanceSmallASmallB = SimilarityMeasure.ZHANG_DISTANCE_WITH_LOCAL_NORMALIZATION.compute( motifSmallA, motifSmallB );
				logger.debug( "zhang with local normalization, distance small a -> small b: {}", distanceSmallASmallB );
				distanceSmallAMedium = SimilarityMeasure.ZHANG_DISTANCE_WITH_LOCAL_NORMALIZATION.compute( motifSmallA, motifMedium );
				logger.debug( "zhang with local normalization, distance small a -> medium: {}", distanceSmallAMedium );
				distanceSmallALarge = SimilarityMeasure.ZHANG_DISTANCE_WITH_LOCAL_NORMALIZATION.compute( motifSmallA, motifLarge );
				logger.debug( "zhang with local normalization, distance small a -> large: {}", distanceSmallALarge );
				assertTrue( distanceSmallASmallB <= distanceSmallAMedium );
				assertTrue( distanceSmallAMedium <= distanceSmallALarge );
			}
			finally
			{
				model.getGraph().releaseRef( spotRef );
				model.getBranchGraph().releaseRef( branchSpotRef );
				projectModel.close();
			}
		}
	}

	@Test
	void testGetLineageMotifName()
	{
		BranchSpotTree motif = new BranchSpotTree( graph2.branchSpotA, 1, 5, graph2.getModel() );
		String name = LineageMotifsUtils.getLineageMotifName( motif );
		assertEquals( "1", name );
	}

	@Test
	void testTagLineageMotifs()
	{
		String tagSetName = "testTagSet";
		Color color = Color.RED;
		Model model = graph2.getModel();
		List< Pair< BranchSpotTree, Double > > lineageMotifs = new ArrayList<>();
		lineageMotifs.add( Pair.of( new BranchSpotTree( graph2.branchSpotA, 0, 5, model ), 0.5d ) );
		lineageMotifs.add( Pair.of( new BranchSpotTree( graph2.branchSpotB, 4, 7, model ), 0.7d ) );
		lineageMotifs.add( Pair.of( new BranchSpotTree( graph2.branchSpotC, 3, 5, model ), 0.9d ) );
		LineageMotifsUtils.tagLineageMotifs( graph2.getModel(), tagSetName, lineageMotifs, color );

		List< TagSetStructure.TagSet > tagSets = graph2.getModel().getTagSetModel().getTagSetStructure().getTagSets();
		TagSetStructure.TagSet tagSet = tagSets.get( 0 );
		List< TagSetStructure.Tag > tags = tagSet.getTags();
		TagSetStructure.Tag tag0 = tags.get( 0 );
		TagSetStructure.Tag tag1 = tags.get( 1 );
		TagSetStructure.Tag tag2 = tags.get( 2 );
		assertEquals( 1, tagSets.size() );
		assertEquals( 3, tags.size() );
		assertEquals( "Lineage Motif 0 (distance: " + String.format( "%.2f", 0.5d ) + ")", tag0.label() );
		assertEquals( "Lineage Motif 4 (distance: " + String.format( "%.2f", 0.7d ) + ")", tag1.label() );
		assertEquals( "Lineage Motif 11 (distance: " + String.format( "%.2f", 0.9d ) + ")", tag2.label() );
		assertEquals( Color.RED.getRGB(), tag0.color() );
		assertEquals( -35981, tag1.color() );
		assertEquals( -6426, tag2.color() );
		assertEquals( tag0.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph1.spot0 ).label() );
		assertEquals( tag0.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot1 ).label() );
		assertEquals( tag0.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot2 ).label() );
		assertEquals( tag0.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot3 ).label() );
		assertEquals( tag1.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot4 ).label() );
		assertEquals( tag1.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot5 ).label() );
		assertEquals( tag1.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot6 ).label() );
		assertEquals( tag1.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot7 ).label() );
		assertEquals( tag1.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot8 ).label() );
		assertEquals( tag1.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot10 ).label() );
		assertEquals( tag2.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot11 ).label() );
		assertEquals( tag2.label(), model.getTagSetModel().getVertexTags().tags( tagSet ).get( graph2.spot13 ).label() );
	}

	@Test
	void testGetSimilarMotifs() throws IOException, SpimDataException
	{
		try (final Context context = new Context())
		{
			File tempFile1 = TestUtils.getTempFileCopy(
					"src/test/resources/org/mastodon/mamut/lineagemotifs/util/lineage_modules.mastodon", "model",
					".mastodon"
			);
			ProjectModel projectModel = ProjectLoader.open( tempFile1.getAbsolutePath(), context, false, true );
			Model model = projectModel.getModel();
			Spot spotRef = model.getGraph().vertexRef();
			BranchSpot branchSpotRef = model.getBranchGraph().vertexRef();
			try
			{
				SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();

				List< String > list =
						Arrays.asList( "218", "219", "220", "221", "222", "223", "224", "225", "226", "227", "228", "229", "230",
								"231", "232", "255", "256", "257", "258", "259", "260", "261", "262", "263", "264", "265", "266", "267",
								"268", "269", "270", "271", "272", "273", "274" );
				for ( Spot spot : model.getGraph().vertices() )
				{
					if ( list.contains( spot.getLabel() ) )
						selectionModel.setSelected( spot, true );
				}
				BranchSpotTree motif = LineageMotifsUtils.getSelectedMotif( model, selectionModel );
				List< Pair< BranchSpotTree, Double > > similarMotifsSpotIteration = LineageMotifsUtils.getMostSimilarMotifs( motif, 20,
						SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, spotRef, branchSpotRef, true );
				List< Pair< BranchSpotTree, Double > > similarMotifsBranchSpotIteration = LineageMotifsUtils.getMostSimilarMotifs( motif,
						20, SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE, spotRef, branchSpotRef, false );
				boolean containsZeroValueSpotIteration = similarMotifsSpotIteration.stream().anyMatch( pair -> pair.getValue() == 0.0 );
				boolean containsBranchSpot220SpotIteration =
						similarMotifsSpotIteration.stream().anyMatch( pair -> pair.getKey().getBranchSpot().getLabel().equals( "220" ) );
				boolean containsZeroValueBranchSpotIteration =
						similarMotifsBranchSpotIteration.stream().anyMatch( pair -> pair.getValue() == 0.0 );
				boolean containsBranchSpot220BranchSpotIteration = similarMotifsBranchSpotIteration.stream()
						.anyMatch( pair -> pair.getKey().getBranchSpot().getLabel().equals( "220" ) );

				assertEquals( "220", motif.getBranchSpot().getLabel() );
				assertEquals( 16, motif.getStartTimepoint() );
				assertEquals( 30, motif.getEndTimepoint() );

				assertEquals( 21, similarMotifsSpotIteration.size() );
				assertTrue( containsZeroValueSpotIteration );
				assertTrue( containsBranchSpot220SpotIteration );

				assertEquals( 18, similarMotifsBranchSpotIteration.size() );
				assertTrue( containsZeroValueBranchSpotIteration );
				assertTrue( containsBranchSpot220BranchSpotIteration );
			}
			finally
			{
				model.getGraph().releaseRef( spotRef );
				model.getBranchGraph().releaseRef( branchSpotRef );
				projectModel.close();
			}
		}
	}
}
