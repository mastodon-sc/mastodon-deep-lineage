package org.mastodon.mamut.treesimilarity.tree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SimpleTreeTest
{

	@Test
	public void testGetChildren()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		SimpleTree< Number > child1 = new SimpleTree<>( 1 );
		SimpleTree< Number > child2 = new SimpleTree<>( 2 );
		tree.addChild( child1 );
		tree.addChild( child2 );
		List< SimpleTree< Number > > expected = new ArrayList<>( Arrays.asList( child1, child2 ) );
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
		tree.addChild( child1 );
		assertEquals( child1, tree.getChildren().iterator().next() );
	}

	@Test
	public void testTestToString()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		assertEquals( "SimpleTree@" + tree.hashCode(), tree.toString() );
	}
}
