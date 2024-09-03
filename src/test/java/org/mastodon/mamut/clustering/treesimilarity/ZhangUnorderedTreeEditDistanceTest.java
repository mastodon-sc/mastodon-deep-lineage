/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.clustering.treesimilarity;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.clustering.treesimilarity.tree.DenseSimpleTreeExamples;
import org.mastodon.mamut.clustering.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTreeExamples;
import org.mastodon.mamut.clustering.treesimilarity.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.ToDoubleBiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZhangUnorderedTreeEditDistanceTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final ToDoubleBiFunction< Double, Double > defaultCosts = TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION;

	@SuppressWarnings("all")
	@Test
	void testExceptions()
	{
		Tree< Double > simpleTree1 = SimpleTreeExamples.tree1();
		Tree< Double > simpleTree2 = SimpleTreeExamples.tree2();
		assertThrows( IllegalArgumentException.class, () -> ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleTree2, null ) );
	}

	@Test
	void testNullTrees()
	{
		assertEquals( 0, ZhangUnorderedTreeEditDistance.distance( null, null, defaultCosts ), 0d );
	}

	@Test
	void testDistance()
	{
		Tree< Double > simpleTree1 = SimpleTreeExamples.tree1();
		Tree< Double > simpleTree2 = SimpleTreeExamples.tree2();
		Tree< Double > simpleTree3 = SimpleTreeExamples.tree3();
		Tree< Double > simpleTree4 = SimpleTreeExamples.tree4();
		Tree< Double > simpleTree5 = SimpleTreeExamples.tree5();
		Tree< Double > simpleTree6 = SimpleTreeExamples.tree6();
		Tree< Double > simpleTree7 = SimpleTreeExamples.tree7();
		Tree< Double > simpleTree8 = SimpleTreeExamples.tree8();
		Tree< Double > simpleTree9 = SimpleTreeExamples.tree9();

		Tree< Double > branchSpotTree1 = BranchSpotTreeExamples.tree1();
		Tree< Double > branchSpotTree2 = BranchSpotTreeExamples.tree2();
		Tree< Double > branchSpotTree3 = BranchSpotTreeExamples.tree3();
		Tree< Double > branchSpotTree4 = BranchSpotTreeExamples.tree4();
		Tree< Double > branchSpotTree5 = BranchSpotTreeExamples.tree5();
		Tree< Double > branchSpotTree6 = BranchSpotTreeExamples.tree6();
		Tree< Double > branchSpotTree7 = BranchSpotTreeExamples.tree7();
		Tree< Double > branchSpotTree8 = BranchSpotTreeExamples.tree8();
		Tree< Double > branchSpotTree9 = BranchSpotTreeExamples.tree9();
		Tree< Double > branchSpotTree10 = BranchSpotTreeExamples.tree10();

		// 20, because: 2 nodes with a difference of 10 each need to be changed
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleTree2, defaultCosts ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( simpleTree2, simpleTree1, defaultCosts ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotTree2, defaultCosts ), 0d );
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, branchSpotTree1, defaultCosts ), 0d );

		// 4, because:
		// 2 nodes are removed (weight 1 each), e.g. node 2 and node 3 in tree 3
		// 2 nodes are added (weight 1 each), e.g. node 4 and node 5 in tree 4
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( simpleTree3, simpleTree4, defaultCosts ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( simpleTree4, simpleTree3, defaultCosts ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree3, branchSpotTree4, defaultCosts ), 0d );
		assertEquals( 4d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree4, branchSpotTree3, defaultCosts ), 0d );

		// 49, because: one node has a difference of 1
		// two nodes have a difference of 24 each
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree6, defaultCosts ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( simpleTree6, simpleTree5, defaultCosts ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree5, branchSpotTree6, defaultCosts ), 0d );
		assertEquals( 49d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree6, branchSpotTree5, defaultCosts ), 0d );

		// 69, because:
		// one node has a difference of 1
		// two nodes have a difference of 24 each
		// two extra nodes are added with a weight of 10 each
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( simpleTree5, simpleTree7, defaultCosts ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( simpleTree7, simpleTree5, defaultCosts ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree5, branchSpotTree7, defaultCosts ), 0d );
		assertEquals( 69d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree7, branchSpotTree5, defaultCosts ), 0d );

		// 1, because: one node has a difference of 1
		assertEquals( 1d, ZhangUnorderedTreeEditDistance.distance( simpleTree8, simpleTree9, defaultCosts ), 0d );
		assertEquals( 1d, ZhangUnorderedTreeEditDistance.distance( simpleTree9, simpleTree8, defaultCosts ), 0d );
		assertEquals( 1d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree8, branchSpotTree9, defaultCosts ), 0d );
		assertEquals( 1d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree9, branchSpotTree8, defaultCosts ), 0d );

		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, branchSpotTree10, defaultCosts ), 0d );
	}

	@Test
	void testDistanceEmptyTrees()
	{
		Tree< Double > simpleEmptyTree = SimpleTreeExamples.emptyTree();
		Tree< Double > simpleTree1 = SimpleTreeExamples.tree1();
		Tree< Double > simpleTree2 = SimpleTreeExamples.tree2();
		Tree< Double > simpleTree3 = SimpleTreeExamples.tree3();
		Tree< Double > simpleTree4 = SimpleTreeExamples.tree4();

		Tree< Double > branchSpotTree1 = BranchSpotTreeExamples.tree1();
		Tree< Double > branchSpotTree2 = BranchSpotTreeExamples.tree2();
		Tree< Double > branchSpotTree3 = BranchSpotTreeExamples.tree3();
		Tree< Double > branchSpotTree4 = BranchSpotTreeExamples.tree4();


		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleTree1, simpleEmptyTree, defaultCosts ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree1, defaultCosts ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree1, null, defaultCosts ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( null, branchSpotTree1, defaultCosts ), 0d );
		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleTree2, simpleEmptyTree, defaultCosts ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree2, defaultCosts ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree2, null, defaultCosts ), 0d );
		assertEquals( 60d, ZhangUnorderedTreeEditDistance.distance( null, branchSpotTree2, defaultCosts ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleTree3, simpleEmptyTree, defaultCosts ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree3, defaultCosts ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree3, null, defaultCosts ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( null, branchSpotTree3, defaultCosts ), 0d );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleTree4, simpleEmptyTree, defaultCosts ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( simpleEmptyTree, simpleTree4, defaultCosts ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( branchSpotTree4, null, defaultCosts ), 0d );
		assertEquals( 104d, ZhangUnorderedTreeEditDistance.distance( null, branchSpotTree4, defaultCosts ), 0d );
	}

	@Test
	void testDistanceTopologicalCostFunction()
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
		ToDoubleBiFunction< Double, Double > costFunction = ( a, b ) -> ( a == null ) == ( b == null ) ? 0d : 1d;
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

	/**
	 * Tests the remove subtree operation (3a)
	 * Special case: the root node is removed
	 * @see ZhangUnorderedTreeEditDistance
	 */
	@Test
	void testRemoveSubtreeKeepOneChild()
	{
		Tree< Double > tree10 = SimpleTreeExamples.tree10();
		Tree< Double > tree11 = SimpleTreeExamples.tree11();
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree10, tree11, defaultCosts ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree11, tree10, defaultCosts ), 0d );
	}


	/**
	 * Tests the remove subtree operation (4a)
	 * @see ZhangUnorderedTreeEditDistance
	 */
	@Test
	void testRemoveSubtreeKeepAllChildren()
	{
		Tree< Double > tree12 = SimpleTreeExamples.tree12();
		Tree< Double > tree13 = SimpleTreeExamples.tree13();
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree12, tree13, defaultCosts ), 0d );
		assertEquals( 2, ZhangUnorderedTreeEditDistance.distance( tree13, tree12, defaultCosts ), 0d );
	}

	@Test
	void testNonBinaryTrees()
	{
		Tree< Double > tree14 = SimpleTreeExamples.tree14();
		Tree< Double > tree15 = SimpleTreeExamples.nonBinaryTree();
		assertEquals( 1_000_003, ZhangUnorderedTreeEditDistance.distance( tree14, tree15, defaultCosts ), 0d );
		assertEquals( 1_000_003, ZhangUnorderedTreeEditDistance.distance( tree15, tree14, defaultCosts ), 0d );
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
	void testTrickyExample()
	{
		Tree< Double > tree15 = SimpleTreeExamples.tree15();
		Tree< Double > tree16 = SimpleTreeExamples.tree16();
		assertEquals( 203, ZhangUnorderedTreeEditDistance.distance( tree15, tree16, defaultCosts ), 0d );
		assertEquals( 203, ZhangUnorderedTreeEditDistance.distance( tree16, tree15, defaultCosts ), 0d );

		// This is the optimal edit path: (delete the nodes with attribute 100 and 1), (insert the nodes with attribute 2 and 100)
		Tree< Double > tree17 = SimpleTreeExamples.tree17();
		assertEquals( 101, ZhangUnorderedTreeEditDistance.distance( tree15, tree17, defaultCosts ), 0d );
		assertEquals( 101, ZhangUnorderedTreeEditDistance.distance( tree17, tree15, defaultCosts ), 0d );
		assertEquals( 102, ZhangUnorderedTreeEditDistance.distance( tree17, tree16, defaultCosts ), 0d );
		assertEquals( 102, ZhangUnorderedTreeEditDistance.distance( tree16, tree17, defaultCosts ), 0d );
	}

	@Test
	void testRecursiveRemoveNodes()
	{
		Tree< Double > tree18 = SimpleTreeExamples.tree18();
		Tree< Double > tree19 = SimpleTreeExamples.tree19();
		// The edit path is to remove the nodes with weights: 1, 2, 3, 4, 5, 6 (cost 21 = 1 + 2 + 3 + 4 + 5 + 6)
		assertEquals( 21, ZhangUnorderedTreeEditDistance.distance( tree18, tree19, defaultCosts ), 0d );
	}

	@Test
	void testDistanceBug()
	{
		Tree< Double > tree20 = SimpleTreeExamples.tree20();
		Tree< Double > tree21 = SimpleTreeExamples.tree21();
		assertEquals( 170d, ZhangUnorderedTreeEditDistance.distance( tree20, tree21, defaultCosts ), 0d );
	}

	@Test
	void testChangeLabelOperation()
	{
		Tree< Double > tree22 = SimpleTreeExamples.tree22();
		Tree< Double > tree23 = SimpleTreeExamples.tree23();
		assertEquals( 20d, ZhangUnorderedTreeEditDistance.distance( tree22, tree23, defaultCosts ), 0d );
	}

	@Test
	void testComplexExample()
	{
		Tree< Double > tree1a111 = SimpleTreeExamples.tree1a111();
		Tree< Double > tree2c2 = SimpleTreeExamples.tree2c2();
		assertEquals( 1_654d, ZhangUnorderedTreeEditDistance.distance( tree1a111, tree2c2, defaultCosts ), 0d );
	}

	@Test
	void testDenseTreeExample()
	{
		Tree< Double > tree2aba = DenseSimpleTreeExamples.tree2aba();
		Tree< Double > tree1bab = DenseSimpleTreeExamples.tree1bab();
		assertEquals( 39_214d, ZhangUnorderedTreeEditDistance.distance( tree2aba, tree1bab, defaultCosts ), 0d );
	}

	public static void main( String[] args )
	{
		// NB: This exists in addition to testDenseTreeExample(), because time measurement does not work in the test.
		Tree< Double > tree2aba = DenseSimpleTreeExamples.tree2aba();
		Tree< Double > tree1bab = DenseSimpleTreeExamples.tree1bab();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		double costs = ZhangUnorderedTreeEditDistance.distance( tree2aba, tree1bab, defaultCosts );
		stopWatch.stop();
		logger.debug( "Costs: {}, time: {}ms", costs, stopWatch.getTime() );
	}
}
