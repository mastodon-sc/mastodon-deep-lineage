package org.mastodon.mamut.treesimilarity;

import org.junit.Test;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class ZhangUnorderedTreeEditDistanceTest
{

	@Test
	public void testDistance()
	{
		BiFunction< Number, Number, Double > costFunctionSimpleTree = getCostFunctionSimpleTree();

		Tree< Number > simpleTree1 = SimpleTreeExamples.tree1();
		Tree< Number > simpleTree2 = SimpleTreeExamples.tree2();
		Tree< Number > simpleTree3 = SimpleTreeExamples.tree3();
		Tree< Number > simpleTree4 = SimpleTreeExamples.tree4();
		Tree< Number > simpleTree5 = SimpleTreeExamples.tree5();
		Tree< Number > simpleTree6 = SimpleTreeExamples.tree6();
		Tree< Number > simpleTree7 = SimpleTreeExamples.tree7();

		BiFunction< Integer, Integer, Double > costFunctionBranchSpotTree = getCostFunctionBranchSpotTree();
		Tree< Integer > branchSpotTree1 = BranchSpotTreeExamples.tree1();
		Tree< Integer > branchSpotTree2 = BranchSpotTreeExamples.tree2();


		// 20, because: 2 nodes with a difference of 10 each need to be changed
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotTree2, costFunctionBranchSpotTree ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, branchSpotTree1, costFunctionBranchSpotTree ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleTree2, costFunctionSimpleTree ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( simpleTree2, simpleTree1, costFunctionSimpleTree ), 0d );

		// 4, because:
		// 2 nodes are removed (weight 1 each), e.g. node 2 and node 3 in tree 3
		// 2 nodes are added (weight 1 each), e.g. node 4 and node 5 in tree 4
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( simpleTree3, simpleTree4, costFunctionSimpleTree ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( simpleTree4, simpleTree3, costFunctionSimpleTree ), 0d );

		// 49, because: one node has a difference of 1
		// two nodes have a difference of 24 each
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree6, costFunctionSimpleTree ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( simpleTree6, simpleTree5, costFunctionSimpleTree ), 0d );

		// 69, because:
		// one node has a difference of 1
		// two nodes have a difference of 24 each
		// two extra nodes are added with a weight of 10 each
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree7, costFunctionSimpleTree ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( simpleTree7, simpleTree5, costFunctionSimpleTree ), 0d );
	}

	@Test
	public void testDistanceEmptyTrees()
	{
		BiFunction< Number, Number, Double > costFunction = getCostFunctionSimpleTree();

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

	private static BiFunction< Number, Number, Double > getCostFunctionSimpleTree()
	{
		return ( o1, o2 ) -> {
			if ( o2 == null )
				return o1.doubleValue();
			else
				return Math.abs( o1.doubleValue() - o2.doubleValue() );
		};
	}

	private static BiFunction< Integer, Integer, Double > getCostFunctionBranchSpotTree()
	{
		return ( o1, o2 ) -> {
			if ( o2 == null )
				return o1.doubleValue();
			else
				return Math.abs( o1.doubleValue() - o2.doubleValue() );
		};
	}
}
