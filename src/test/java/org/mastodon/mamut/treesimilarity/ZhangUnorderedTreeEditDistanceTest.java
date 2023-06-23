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
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree1, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree1, costFunction ), 0d );
		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleTree2, simpleEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree2, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree2, costFunction ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleTree3, simpleEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree3, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree3, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree3, costFunction ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleTree4, simpleEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree4, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree4, branchSpotEmptyTree, costFunction ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotEmptyTree, branchSpotTree4, costFunction ), 0d );
	}

	@Test
	public void testDistanceNullCostFunction()
	{

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


		// 0, because: the trees are topologically identical
		BiFunction< Double, Double, Double > costFunction = ( a, b ) -> ( a == null ) == ( b == null ) ? 0d : 1d;
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleTree2, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( simpleTree2, simpleTree1, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotTree2, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, branchSpotTree1, costFunction ), 0d );
		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( simpleTree3, simpleTree4, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( simpleTree4, simpleTree3, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( branchSpotTree3, branchSpotTree4, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( branchSpotTree4, branchSpotTree3, costFunction ), 0d );

		// 0, because: the trees are topologically identical
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree6, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( simpleTree6, simpleTree5, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( branchSpotTree5, branchSpotTree6, costFunction ), 0d );
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( branchSpotTree6, branchSpotTree5, costFunction ), 0d );

		// 2, because: two extra nodes are added
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree7, costFunction ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( simpleTree7, simpleTree5, costFunction ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( branchSpotTree5, branchSpotTree7, costFunction ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( branchSpotTree7, branchSpotTree5, costFunction ), 0d );
	}

	@Test
	public void testEquivalenceClasses() {
		BiFunction< Double, Double, Double > costFunction = getCostFunction();
		Tree< Double > a = SimpleTreeExamples.tree8();
		Tree< Double > b = SimpleTreeExamples.tree9();
		double distance = ZhangUnorderedTreeEditDistance.distance( a, b, costFunction );
		assertEquals( 1, distance, 0d );
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


	@Test
	public void testReorderingLeafNodes() {
		BiFunction< Double, Double, Double > costFunction = getCostFunction();
		Tree< Double > tree10 = SimpleTreeExamples.tree10();
		Tree< Double > tree11 = SimpleTreeExamples.tree11();
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree10, tree11, costFunction ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree11, tree10, costFunction ), 0d );
	}


	/**
	 * This test requires the method
	 * {@link ZhangUnorderedTreeEditDistance#getMinForestChangeCosts(Tree, Tree)}.
	 * to work correctly. (So it essentially tests that method.)
	 */
	@Test
	public void testNodeRemovalAndInsertion()
	{
		BiFunction< Double, Double, Double > costFunction = getCostFunction();
		Tree< Double > tree12 = SimpleTreeExamples.tree12();
		Tree< Double > tree13 = SimpleTreeExamples.tree13();
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree12, tree13, costFunction ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree13, tree12, costFunction ), 0d );
	}

	@Test
	public void testNonBinaryTrees()
	{
		// NB: I guess there is a bug in the code that sets up the flow network.
		// It fails if the number of child trees is not equal between the compared trees.
		Tree< Double > tree14 = SimpleTreeExamples.tree14();
		Tree< Double > tree15 = SimpleTreeExamples.nonBinaryTree();
		assertEquals( 1_000_003, ZhangUnorderedTreeEditDistance.distance( tree14, tree15, getCostFunction() ), 0d );
		assertEquals( 1_000_003, ZhangUnorderedTreeEditDistance.distance( tree15, tree14, getCostFunction() ), 0d );
	}

	/**
	 * Try a tricky example:
	 * <pre>
	 *                  1000              1000          1000
	 *                  /  \              /  \          /  \
	 *                 /    1            2    \       200  300
	 *                /    / \          / \    \
	 *              100  200 300      100 200  300
	 * </pre>
	 */
	@Test
	public void testTrickyExample()
	{
		Tree< Double > tree15 = SimpleTreeExamples.tree15();
		Tree< Double > tree16 = SimpleTreeExamples.tree16();
		assertEquals( 203, ZhangUnorderedTreeEditDistance.distance( tree15, tree16, getCostFunction() ), 0d );
		assertEquals( 203, ZhangUnorderedTreeEditDistance.distance( tree16, tree15, getCostFunction() ), 0d );

		// This is the optimal edit path: (delete the nodes with attribute 100 and 1), (insert the nodes with attribute 2 and 100)
		Tree< Double > tree17 = SimpleTreeExamples.tree17();
		assertEquals( 101, ZhangUnorderedTreeEditDistance.distance( tree15, tree17, getCostFunction() ), 0d );
		assertEquals( 101, ZhangUnorderedTreeEditDistance.distance( tree17, tree15, getCostFunction() ), 0d );
		assertEquals( 102, ZhangUnorderedTreeEditDistance.distance( tree17, tree16, getCostFunction() ), 0d );
		assertEquals( 102, ZhangUnorderedTreeEditDistance.distance( tree16, tree17, getCostFunction() ), 0d );
	}

	@Test
	public void testRecursiveRemoveNodes()
	{
		Tree< Double > tree18 = SimpleTreeExamples.tree18();
		Tree< Double > tree19 = SimpleTreeExamples.tree19();
		// The edit path is to remove the nodes with weights: 1, 2, 3, 4, 5, 6 (cost 21 = 1 + 2 + 3 + 4 + 5 + 6)
		assertEquals( 21, ZhangUnorderedTreeEditDistance.distance( tree18, tree19, getCostFunction() ), 0d );
	}
}
