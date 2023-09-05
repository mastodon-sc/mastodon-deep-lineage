package org.mastodon.mamut.treesimilarity.tree;

import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
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
		ExampleGraph2 example = new ExampleGraph2();
		BranchSpotTree tree = new BranchSpotTree( example.branchSpotB, 20 );
		// Note: spot3 is the first spot of branchSpotB.
		assertEquals( example.spot3.getLabel(), tree.toString() );
	}

	@Test
	public void testBranchSpot()
	{
		ExampleGraph2 example = new ExampleGraph2();
		// NB: 2 is not a timepoint of branchSpotB, it only starts at 4.
		assertThrows( IllegalArgumentException.class, () -> new BranchSpotTree( example.branchSpotB, 2 ) );
		assertThrows( IllegalArgumentException.class, () -> new BranchSpotTree( null, 0 ) );
	}
}
