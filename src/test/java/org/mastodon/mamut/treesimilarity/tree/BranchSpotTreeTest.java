package org.mastodon.mamut.treesimilarity.tree;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		Tree< Integer > tree1 = BranchSpotTreeExamples.tree1();
		assertEquals( 20d, tree1.getAttribute().doubleValue(), 0d );
		Iterator< Tree< Integer > > iterator1 = tree1.getChildren().iterator();
		assertEquals( 30d, iterator1.next().getAttribute().doubleValue(), 0d );
		assertEquals( 10d, iterator1.next().getAttribute().doubleValue(), 0d );

		Tree< Integer > tree2 = BranchSpotTreeExamples.tree2();
		assertEquals( 30d, tree2.getAttribute().doubleValue(), 0d );
		Iterator< Tree< Integer > > iterator2 = tree2.getChildren().iterator();
		assertEquals( 20d, iterator2.next().getAttribute().doubleValue(), 0d );
		assertEquals( 10d, iterator2.next().getAttribute().doubleValue(), 0d );
	}

	@Test
	public void testIsLeaf()
	{
		Tree< Integer > tree1 = BranchSpotTreeExamples.tree1();
		assertFalse( tree1.isLeaf() );
		Iterator< Tree< Integer > > iterator1 = tree1.getChildren().iterator();
		assertTrue( iterator1.next().isLeaf() );
		assertTrue( iterator1.next().isLeaf() );

		Tree< Integer > tree2 = BranchSpotTreeExamples.tree2();
		assertFalse( tree2.isLeaf() );
		Iterator< Tree< Integer > > iterator2 = tree1.getChildren().iterator();
		assertTrue( iterator2.next().isLeaf() );
		assertTrue( iterator2.next().isLeaf() );
	}
}
