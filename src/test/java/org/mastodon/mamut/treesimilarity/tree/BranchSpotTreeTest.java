package org.mastodon.mamut.treesimilarity.tree;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BranchSpotTreeTest
{

	@Test
	public void testGetChildren()
	{
		assertEquals( 2, BranchSpotTreeExamples.tree1().getChildren().size() );
		assertEquals( 2, BranchSpotTreeExamples.tree2().getChildren().size() );
	}

	@Test
	public void testGetAttribute()
	{
		assertEquals( 20d, BranchSpotTreeExamples.tree1().getAttribute(), 0d );
		assertEquals( 30d, BranchSpotTreeExamples.tree2().getAttribute(), 0d );
	}

	@Test
	public void testIsLeaf()
	{
		Tree< Double > tree1 = BranchSpotTreeExamples.tree1();
		assertFalse( tree1.isLeaf() );
		Iterator< Tree< Double > > iterator1 = tree1.getChildren().iterator();
		assertTrue( iterator1.next().isLeaf() );
		assertTrue( iterator1.next().isLeaf() );

		Tree< Double > tree2 = BranchSpotTreeExamples.tree2();
		assertFalse( tree2.isLeaf() );
		Iterator< Tree< Double > > iterator2 = tree1.getChildren().iterator();
		assertTrue( iterator2.next().isLeaf() );
		assertTrue( iterator2.next().isLeaf() );
	}

	@Test
	public void testGetBranchSpot()
	{
		assertEquals( 0d, BranchSpotTreeExamples.emptyTree().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 20d, BranchSpotTreeExamples.tree1().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 30d, BranchSpotTreeExamples.tree2().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 1d, BranchSpotTreeExamples.tree3().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 1d, BranchSpotTreeExamples.tree4().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 13d, BranchSpotTreeExamples.tree5().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 12d, BranchSpotTreeExamples.tree6().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 12d, BranchSpotTreeExamples.tree7().getBranchSpot().getTimepoint(), 0d );
	}

	@Test
	public void testToString()
	{
		assertEquals( "spot1", BranchSpotTreeExamples.tree1().toString() );
	}

	@Test
	public void testBranchSpot()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		final int offset = 50;

		Spot spot1 = modelGraph.addVertex();
		spot1.init( offset, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 20 + offset, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 20 + offset, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 30 + offset, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 20 + offset, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 50 + offset, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		assertThrows( IllegalArgumentException.class, () -> new BranchSpotTree( branchSpot, 20 ) );
		assertThrows( IllegalArgumentException.class, () -> new BranchSpotTree( null, 0 ) );
	}
}
