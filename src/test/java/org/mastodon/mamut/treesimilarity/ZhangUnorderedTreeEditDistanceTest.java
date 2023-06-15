package org.mastodon.mamut.treesimilarity;

import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class ZhangUnorderedTreeEditDistanceTest
{

	@Test
	public void testDistance()
	{
		BiFunction< Number, Number, Integer > costFunction = getCostFunction();

		SimpleTree< Number > tree1 = SimpleTreeExamples.tree1();
		SimpleTree< Number > tree2 = SimpleTreeExamples.tree2();
		SimpleTree< Number > tree3 = SimpleTreeExamples.tree3();
		SimpleTree< Number > tree4 = SimpleTreeExamples.tree4();
		SimpleTree< Number > tree5 = SimpleTreeExamples.tree5();
		SimpleTree< Number > tree6 = SimpleTreeExamples.tree6();
		SimpleTree< Number > tree7 = SimpleTreeExamples.tree7();

		// similarity of trees against other trees

		// 20, because: 2 nodes with a difference of 10 each need to be changed
		assertEquals( 20, ZhangUnorderedTreeEditDistance.distance( tree1, tree2, costFunction ) );
		assertEquals( 20, ZhangUnorderedTreeEditDistance.distance( tree2, tree1, costFunction ) );
		// 4, because:
		// 2 nodes are removed (weight 1 each), e.g. node 2 and node 3 in tree 3
		// 2 nodes are added (weight 1 each), e.g. node 4 and node 5 in tree 4
		assertEquals( 4, ZhangUnorderedTreeEditDistance.distance( tree3, tree4, costFunction ) );
		assertEquals( 4, ZhangUnorderedTreeEditDistance.distance( tree4, tree3, costFunction ) );

		// 49, because: one node has a difference of 1
		// two nodes have a difference of 24 each
		assertEquals( 49, ZhangUnorderedTreeEditDistance.distance( tree5, tree6, costFunction ) );
		assertEquals( 49, ZhangUnorderedTreeEditDistance.distance( tree6, tree5, costFunction ) );

		// 69, because:
		// one node has a difference of 1
		// two nodes have a difference of 24 each
		// two extra nodes are added with a weight of 10 each
		assertEquals( 69, ZhangUnorderedTreeEditDistance.distance( tree5, tree7, costFunction ) );
		assertEquals( 69, ZhangUnorderedTreeEditDistance.distance( tree7, tree5, costFunction ) );
	}

	@Test
	public void testDistanceEmptyTrees()
	{
		BiFunction< Number, Number, Integer > costFunction = getCostFunction();

		SimpleTree< Number > emptyTree = SimpleTreeExamples.emptyTree();
		SimpleTree< Number > tree1 = SimpleTreeExamples.tree1();
		SimpleTree< Number > tree2 = SimpleTreeExamples.tree2();
		SimpleTree< Number > tree3 = SimpleTreeExamples.tree3();
		SimpleTree< Number > tree4 = SimpleTreeExamples.tree4();

		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( tree1, emptyTree, costFunction ) );
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree1, costFunction ) );
		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( tree2, emptyTree, costFunction ) );
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree2, costFunction ) );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( tree3, emptyTree, costFunction ) );
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree3, costFunction ) );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( tree4, emptyTree, costFunction ) );
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree4, costFunction ) );
	}

	@Test
	public void testDistanceNullCostFunction()
	{

		SimpleTree< Number > tree1 = SimpleTreeExamples.tree1();
		SimpleTree< Number > tree2 = SimpleTreeExamples.tree2();
		SimpleTree< Number > tree3 = SimpleTreeExamples.tree3();
		SimpleTree< Number > tree4 = SimpleTreeExamples.tree4();
		SimpleTree< Number > tree5 = SimpleTreeExamples.tree5();
		SimpleTree< Number > tree6 = SimpleTreeExamples.tree6();
		SimpleTree< Number > tree7 = SimpleTreeExamples.tree7();

		// similarity of trees against other trees

		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree1, tree2, null ) );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree2, tree1, null ) );
		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree3, tree4, null ) );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree4, tree3, null ) );

		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree5, tree6, null ) );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree6, tree5, null ) );

		// 2, because: two extra nodes are added
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree5, tree7, null ) );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree7, tree5, null ) );
	}

	private static BiFunction< Number, Number, Integer > getCostFunction()
	{
		return ( o1, o2 ) -> {
			if ( o2 == null )
				return ( Integer ) o1;
			else
				return Math.abs( ( Integer ) o1 - ( Integer ) o2 );
		};
	}
}
