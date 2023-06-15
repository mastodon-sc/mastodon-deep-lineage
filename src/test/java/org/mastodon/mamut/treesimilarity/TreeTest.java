package org.mastodon.mamut.treesimilarity;

import junit.framework.TestCase;

import java.util.Iterator;

public class TreeTest extends TestCase
{

	public void testIsLeaf()
	{
		assertTrue( SimpleTreeExamples.emptyTree().isLeaf() );
		SimpleTree< Number > tree1 = SimpleTreeExamples.tree1();
		Iterator< Tree< Number > > iterator = tree1.getChildren().iterator();
		assertTrue( iterator.next().isLeaf() );
		assertTrue( iterator.next().isLeaf() );
	}
}
