package org.mastodon.mamut.treesimilarity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TreeUtilsTest
{

	@Test
	public void testListOfSubtrees()
	{
		SimpleTree< Number > emptyTree = SimpleTreeFactory.emptyTree();

		SimpleTree< Number > tree1 = SimpleTreeFactory.tree1();
		List< Tree< Number > > subtrees1 = new ArrayList<>();
		subtrees1.add( tree1 );
		subtrees1.addAll( tree1.getChildren() );

		assertEquals( Collections.singletonList( emptyTree ), TreeUtils.listOfSubtrees( emptyTree ) );
		assertEquals( subtrees1, TreeUtils.listOfSubtrees( tree1 ) );
	}

	@Test
	public void testSize()
	{
		assertThrows( IllegalArgumentException.class, () -> TreeUtils.size( null ) );
		assertEquals( 1, TreeUtils.size( SimpleTreeFactory.emptyTree() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeFactory.tree1() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeFactory.tree2() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeFactory.tree3() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeFactory.tree4() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeFactory.tree5() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeFactory.tree6() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeFactory.tree7() ) );
	}
}
