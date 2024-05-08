package org.mastodon.mamut.treesimilarity;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.treesimilarity.tree.SimpleTree;
import org.mastodon.mamut.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeDistancesTest
{

	private final static BiFunction< Double, Double, Double > defaultCosts = TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION;

	@Test
	void testNormalizedDistance()
	{
		Tree< Double > emptyTree = SimpleTreeExamples.emptyTree();
		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		Tree< Double > tree2 = SimpleTreeExamples.tree2();
		assertEquals( 0d, TreeDistances.normalizedDistance( null, null, defaultCosts ), 0d );
		assertEquals( 0d, TreeDistances.normalizedDistance( emptyTree, emptyTree, defaultCosts ), 0d );
		assertEquals( 20d / 120d, TreeDistances.normalizedDistance( tree1, tree2, defaultCosts ), 0d );
	}

	@Test
	void testAverageDistance()
	{
		Tree< Double > emptyTree = SimpleTreeExamples.emptyTree();
		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		Tree< Double > tree2 = SimpleTreeExamples.tree2();
		assertEquals( 0d, TreeDistances.averageDistance( null, null, defaultCosts ), 0d );
		assertEquals( 0d, TreeDistances.averageDistance( emptyTree, emptyTree, defaultCosts ), 0d );
		assertEquals( 20d / 6d, TreeDistances.averageDistance( tree1, tree2, defaultCosts ), 0d );
	}

	/**
	 * @see <a href="https://www.science.org/doi/suppl/10.1126/science.aar5663/suppl_file/aar5663_guignard_sm.pdf">Guignard et al. (2020) Fig. S23</a>
	 */
	@Test
	void testGuignardExample()
	{
		Tree< Double > treeGuignardT1 = SimpleTreeExamples.treeGuignardT1();
		Tree< Double > treeGuignardT2 = SimpleTreeExamples.treeGuignardT2();
		double numerator = 4d; // NB: the publication does not illustrate the optimal set of edit operations
		// double denominator = 9 * 1d + 1 * 2d + 1 * 9d + 7 * 10d; // NB: in the publication is a confirmed mistake, it needs to be 7 * 10d instead of 6 * 10d
		// A discussion with the authors revealed that the normalization should rather be done by the sum of nodes in both trees than by the sum of attributes
		int denominator = 18;
		assertEquals( 22d, ZhangUnorderedTreeEditDistance.distance( treeGuignardT1, treeGuignardT2, defaultCosts ), 0d );
		assertEquals( numerator / denominator, TreeDistances.normalizedDistance( treeGuignardT1, treeGuignardT2,
				TreeDistances.LOCAL_NORMALIZED_COST_FUNCTION
		), 0.001d );

	}

	@Test
	void testFlowNetworkBug()
	{
		SimpleTree< Double > tree2a2 = SimpleTreeExamples.tree2a2();
		SimpleTree< Double > tree2d11 = SimpleTreeExamples.tree2d11();
		double distance = TreeDistances.normalizedDistance( tree2a2, tree2d11,
				TreeDistances.LOCAL_NORMALIZED_COST_FUNCTION
		);
		assertEquals( 0.6586d, distance, 0.0001d );
	}

	/**
	 * There is an equivalent test in the GuignardLab/LineageTree repository:
	 * @see <a href="https://github.com/GuignardLab/LineageTree/blob/master/test/test_uted.py#L68">https://github.com/GuignardLab/LineageTree/blob/master/test/test_uted.py#L68</a>
	 * @see <a href="https://www.science.org/doi/10.1126/science.aar5663">Guignard et al. (2020) Fig. 3B</a>
	 */
	@Test
	void testGuignardExample2()
	{
		Tree< Double > treePm01a80008 = SimpleTreeExamples.treePm01a80008();
		Tree< Double > treePm01a80007 = SimpleTreeExamples.treePm01a80007();
		// NB: the publication says this should be 0.04d (cf. Fig 3B)
		// However, with the normalization described in the publication, the result would be 0.0033
		// A discussion with the authors revealed that the normalization should rather be done by the sum of nodes in both trees than by the sum of attributes
		assertEquals( 3.9974005474699665 / 32, TreeDistances.normalizedDistance( treePm01a80008, treePm01a80007,
				TreeDistances.LOCAL_NORMALIZED_COST_FUNCTION
		), 0.0001d );
	}
}
