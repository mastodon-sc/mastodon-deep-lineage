package org.mastodon.mamut.lineagemotifs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.TestUtils;
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
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

class LineageMotifsUtilsTest
{

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

				for ( Spot spot : model.getGraph().vertices() )
				{
					List< String > list =
							Arrays.asList( "218", "219", "220", "221", "222", "223", "224", "225", "226", "227", "228", "229", "230",
									"231", "232", "255", "256", "257", "258", "259", "260", "261", "262", "263", "264", "265", "266", "267",
									"268", "269", "270", "271", "272", "273", "274" );
					if ( list.contains( spot.getLabel() ) )
						selectionModel.setSelected( spot, true );
				}
				BranchSpotTree motif = LineageMotifsUtils.getSelectedMotif( model, selectionModel );
				List< Pair< BranchSpotTree, Double > > similarMotifsSpotIteration =
						LineageMotifsUtils.getMostSimilarMotifs( motif, 20, spotRef, branchSpotRef, true );
				List< Pair< BranchSpotTree, Double > > similarMotifsBranchSpotIteration =
						LineageMotifsUtils.getMostSimilarMotifs( motif, 20, spotRef, branchSpotRef, false );
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
