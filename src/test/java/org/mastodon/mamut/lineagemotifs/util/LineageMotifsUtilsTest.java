package org.mastodon.mamut.lineagemotifs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Model;

class LineageMotifsUtilsTest
{

	private ExampleGraph1 graph1;

	private ExampleGraph2 graph2;

	@BeforeEach
	void setUp()
	{
		graph1 = new ExampleGraph1();
		graph2 = new ExampleGraph2();
	}

	@Test
	void testGetNumberOfDivisions_NoDivisions()
	{
		Model model = graph1.getModel();
		int division = LineageMotifsUtils.getNumberOfDivisions( new BranchSpotTree( graph1.branchSpotA, 0, 3, model ), model );
		assertEquals( 0, division );
	}

	@Test
	void testGetNumberOfDivisions_MultipleDivisions()
	{
		Model model = graph2.getModel();
		int division = LineageMotifsUtils.getNumberOfDivisions( new BranchSpotTree( graph2.branchSpotA, 0, 7, model ), model );
		assertEquals( 2, division );
	}
}
