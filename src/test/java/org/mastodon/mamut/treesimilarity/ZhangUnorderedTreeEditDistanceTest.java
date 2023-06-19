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
		BiFunction< Double, Double, Double > costFunction = getCostFunction();

		Tree< Double > simpleTree1 = SimpleTreeExamples.tree1();
		Tree< Double > simpleTree2 = SimpleTreeExamples.tree2();
		Tree< Double > simpleTree3 = SimpleTreeExamples.tree3();
		Tree< Double > simpleTree4 = SimpleTreeExamples.tree4();
		Tree< Double > simpleTree5 = SimpleTreeExamples.tree5();
		Tree< Double > simpleTree6 = SimpleTreeExamples.tree6();
		Tree< Double > simpleTree7 = SimpleTreeExamples.tree7();

		Tree< Double > branchSpotTree1 = BranchSpotTreeExamples.tree1();
		Tree< Double > branchSpotTree2 = BranchSpotTreeExamples.tree2();
		Tree< Double > branchSpotTree3 = BranchSpotTreeExamples.tree3();
		Tree< Double > branchSpotTree4 = BranchSpotTreeExamples.tree4();
		Tree< Double > branchSpotTree5 = BranchSpotTreeExamples.tree5();
		Tree< Double > branchSpotTree6 = BranchSpotTreeExamples.tree6();
		Tree< Double > branchSpotTree7 = BranchSpotTreeExamples.tree7();

		// 20, because: 2 nodes with a difference of 10 each need to be changed
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleTree2, costFunction ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( simpleTree2, simpleTree1, costFunction ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotTree2, costFunction ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, branchSpotTree1, costFunction ), 0d );

		// 4, because:
		// 2 nodes are removed (weight 1 each), e.g. node 2 and node 3 in tree 3
		// 2 nodes are added (weight 1 each), e.g. node 4 and node 5 in tree 4
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( simpleTree3, simpleTree4, costFunction ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( simpleTree4, simpleTree3, costFunction ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree3, branchSpotTree4, costFunction ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree4, branchSpotTree3, costFunction ), 0d );

		// 49, because: one node has a difference of 1
		// two nodes have a difference of 24 each
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree6, costFunction ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( simpleTree6, simpleTree5, costFunction ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree5, branchSpotTree6, costFunction ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree6, branchSpotTree5, costFunction ), 0d );

		// 69, because:
		// one node has a difference of 1
		// two nodes have a difference of 24 each
		// two extra nodes are added with a weight of 10 each
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree7, costFunction ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( simpleTree7, simpleTree5, costFunction ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree5, branchSpotTree7, costFunction ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree7, branchSpotTree5, costFunction ), 0d );
	}

	@Test
	public void testDistanceEmptyTrees()
	{
		BiFunction< Double, Double, Double > costFunction = getCostFunction();

		Tree< Double > simpleEmptyTree = SimpleTreeExamples.emptyTree();
		Tree< Double > simpleTree1 = SimpleTreeExamples.tree1();
		Tree< Double > simpleTree2 = SimpleTreeExamples.tree2();
		Tree< Double > simpleTree3 = SimpleTreeExamples.tree3();
		Tree< Double > simpleTree4 = SimpleTreeExamples.tree4();

		Tree< Double > branchSpotEmptyTree = BranchSpotTreeExamples.emptyTree();
		Tree< Double > branchSpotTree1 = BranchSpotTreeExamples.tree1();
		Tree< Double > branchSpotTree2 = BranchSpotTreeExamples.tree2();
		Tree< Double > branchSpotTree3 = BranchSpotTreeExamples.tree3();
		Tree< Double > branchSpotTree4 = BranchSpotTreeExamples.tree4();


		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleTree1, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, simpleTree1, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree1, costFunction ), 0d );
		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleTree2, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, simpleTree2, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree2, costFunction ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleTree3, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, simpleTree3, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree3, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree3, costFunction ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleTree4, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, simpleTree4, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree4, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree4, costFunction ), 0d );
	}

	@Test
	public void testDistanceNullCostFunction()
	{

		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		Tree< Double > tree2 = SimpleTreeExamples.tree2();
		Tree< Double > tree3 = SimpleTreeExamples.tree3();
		Tree< Double > tree4 = SimpleTreeExamples.tree4();
		Tree< Double > tree5 = SimpleTreeExamples.tree5();
		Tree< Double > tree6 = SimpleTreeExamples.tree6();
		Tree< Double > tree7 = SimpleTreeExamples.tree7();


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

	private static BiFunction< Double, Double, Double > getCostFunction()
	{
		return ( o1, o2 ) -> {
			if ( o2 == null )
				return o1;
			else
				return Math.abs( o1 - o2 );
		};
	}
}
