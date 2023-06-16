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
		HashSet< SimpleTree< Number > > children = new HashSet<>();
		children.add( child1 );
		children.add( child2 );
		assertEquals( children, tree.getChildren() );
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
