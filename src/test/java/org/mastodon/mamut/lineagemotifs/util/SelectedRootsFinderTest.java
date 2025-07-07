package org.mastodon.mamut.lineagemotifs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

class SelectedRootsFinderTest
{

	private final ExampleGraph2 graph = new ExampleGraph2();

	@Test
	void testGetRoots_NoVerticesSelected_ReturnsEmptySet()
	{
		try (Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );
			RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );
			assertTrue( roots.isEmpty() );
		}
	}

	@Test
	void testGetRoots_OneVertexSelected()
	{
		try (Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );

			RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );

			assertEquals( 1, roots.size() );
			assertTrue( roots.contains( graph.spot2 ) );
		}
	}

	@Test
	void testGetRoots_TwoRoots()
	{
		try (Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );

			projectModel.getSelectionModel().setSelected( graph.spot0, true );
			projectModel.getSelectionModel().setSelected( graph.spot1, true );
			projectModel.getSelectionModel().setSelected( graph.spot3, true );

			RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );

			assertEquals( 2, roots.size() );
			assertTrue( roots.contains( graph.spot0 ) );
			assertTrue( roots.contains( graph.spot3 ) );
		}
	}
}
