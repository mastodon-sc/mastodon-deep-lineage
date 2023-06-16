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
		Tree< Number > emptyTree = SimpleTreeExamples.emptyTree();

		Tree< Number > tree1 = SimpleTreeExamples.tree1();
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
		assertEquals( 1, TreeUtils.size( SimpleTreeExamples.emptyTree() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree1() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree2() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeExamples.tree3() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeExamples.tree4() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree5() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree6() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeExamples.tree7() ) );
	}
}