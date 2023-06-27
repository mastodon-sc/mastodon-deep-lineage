package org.mastodon.mamut.treesimilarity.tree;

import org.junit.Test;
import org.mastodon.mamut.treesimilarity.tree.SimpleTree;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class SimpleTreeTest
{

	@Test
	public void testGetChildren()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		SimpleTree< Number > child1 = new SimpleTree<>( 1 );
		SimpleTree< Number > child2 = new SimpleTree<>( 2 );
		tree.addSubtree( child1 );
		tree.addSubtree( child2 );
		Set< SimpleTree< Number > > expected = new HashSet( Arrays.asList( child1, child2 ) )
		assertEquals( expected, tree.getChildren() );
	}

	@Test
	public void testGetAttribute()
	{
		int attribute = 1;
		SimpleTree< Number > tree = new SimpleTree<>( attribute );
		assertEquals( attribute, tree.getAttribute() );
	}

	@Test
	public void testAddSubtree()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		SimpleTree< Number > child1 = new SimpleTree<>( 1 );
		tree.addSubtree( child1 );
		assertEquals( child1, tree.getChildren().iterator().next() );
	}

	@Test
	public void testTestToString()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		assertEquals( "SimpleTree@" + tree.hashCode(), tree.toString() );
	}
}
