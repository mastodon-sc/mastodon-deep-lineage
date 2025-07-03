package org.mastodon.mamut.lineagemotifs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

class SelectedRootsFinderTest
{

	private ExampleGraph2 graph;

	private ProjectModel projectModel;

	private Context context;

	@BeforeEach
	void setUp()
	{
		graph = new ExampleGraph2();
		context = new Context();
		final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
		projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );
	}

	@AfterEach
	void tearDown()
	{
		projectModel.close();
		context.close();
	}

	@Test
	void testGetRoots_NoVerticesSelected_ReturnsEmptySet()
	{
		RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );
		assertTrue( roots.isEmpty() );
	}

	@Test
	void testGetRoots_OneVertexSelected()
	{

		projectModel.getSelectionModel().setSelected( graph.spot2, true );

		RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );

		assertEquals( 1, roots.size() );
		assertTrue( roots.contains( graph.spot2 ) );
	}

	@Test
	void testGetRoots_TwoRoots()
	{

		projectModel.getSelectionModel().setSelected( graph.spot0, true );
		projectModel.getSelectionModel().setSelected( graph.spot1, true );
		projectModel.getSelectionModel().setSelected( graph.spot3, true );

		RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );

		assertEquals( 2, roots.size() );
		assertTrue( roots.contains( graph.spot0 ) );
		assertTrue( roots.contains( graph.spot3 ) );
	}
}
