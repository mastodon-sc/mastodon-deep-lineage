package org.mastodon.mamut.treesimilarity;

import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class ZhangUnorderedTreeEditDistanceTest
{

	@Test
	public void testDistance()
	{
		BiFunction< Number, Number, Double > costFunction = getCostFunction();

		Tree< Number > tree1 = SimpleTreeExamples.tree1();
		Tree< Number > tree2 = SimpleTreeExamples.tree2();
		Tree< Number > tree3 = SimpleTreeExamples.tree3();
		Tree< Number > tree4 = SimpleTreeExamples.tree4();
		Tree< Number > tree5 = SimpleTreeExamples.tree5();
		Tree< Number > tree6 = SimpleTreeExamples.tree6();
		Tree< Number > tree7 = SimpleTreeExamples.tree7();


		// 20, because: 2 nodes with a difference of 10 each need to be changed
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( tree1, tree2, costFunction ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( tree2, tree1, costFunction ), 0d );
		// 4, because:
		// 2 nodes are removed (weight 1 each), e.g. node 2 and node 3 in tree 3
		// 2 nodes are added (weight 1 each), e.g. node 4 and node 5 in tree 4
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( tree3, tree4, costFunction ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( tree4, tree3, costFunction ), 0d );

		// 49, because: one node has a difference of 1
		// two nodes have a difference of 24 each
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( tree5, tree6, costFunction ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( tree6, tree5, costFunction ), 0d );

		// 69, because:
		// one node has a difference of 1
		// two nodes have a difference of 24 each
		// two extra nodes are added with a weight of 10 each
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( tree5, tree7, costFunction ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( tree7, tree5, costFunction ), 0d );
	}

	@Test
	public void testDistanceEmptyTrees()
	{
		BiFunction< Number, Number, Double > costFunction = getCostFunction();

		Tree< Number > emptyTree = SimpleTreeExamples.emptyTree();
		Tree< Number > tree1 = SimpleTreeExamples.tree1();
		Tree< Number > tree2 = SimpleTreeExamples.tree2();
		Tree< Number > tree3 = SimpleTreeExamples.tree3();
		Tree< Number > tree4 = SimpleTreeExamples.tree4();


		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( tree1, emptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree1, costFunction ), 0d );
		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( tree2, emptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree2, costFunction ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( tree3, emptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree3, costFunction ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( tree4, emptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree4, costFunction ), 0d );
	}

	@Test
	public void testDistanceNullCostFunction()
	{

		Tree< Number > tree1 = SimpleTreeExamples.tree1();
		Tree< Number > tree2 = SimpleTreeExamples.tree2();
		Tree< Number > tree3 = SimpleTreeExamples.tree3();
		Tree< Number > tree4 = SimpleTreeExamples.tree4();
		Tree< Number > tree5 = SimpleTreeExamples.tree5();
		Tree< Number > tree6 = SimpleTreeExamples.tree6();
		Tree< Number > tree7 = SimpleTreeExamples.tree7();


		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree1, tree2, null ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree2, tree1, null ), 0d );
		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree3, tree4, null ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree4, tree3, null ), 0d );

		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree5, tree6, null ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( tree6, tree5, null ), 0d );

		// 2, because: two extra nodes are added
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree5, tree7, null ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree7, tree5, null ), 0d );
	}

	private static BiFunction< Number, Number, Double > getCostFunction()
	{
		return ( o1, o2 ) -> {
			if ( o2 == null )
				return ( Double ) o1;
			else
				return Math.abs( ( Double ) o1 - ( Double ) o2 );
		};
	}
}
