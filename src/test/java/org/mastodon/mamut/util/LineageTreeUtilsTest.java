package org.mastodon.mamut.util;

import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;

import static org.junit.Assert.assertEquals;

public class LineageTreeUtilsTest
{
	@Test
	public void testGetMinTimepoint()
	{
		assertEquals( 0, LineageTreeUtils.getMinTimepoint( new ExampleGraph1().getModel() ) );
		assertEquals( 0, LineageTreeUtils.getMinTimepoint( new ExampleGraph2().getModel() ) );

	}

	@Test
	public void testGetMaxTimepoint()
	{
		assertEquals( 3, LineageTreeUtils.getMaxTimepoint( new ExampleGraph1().getModel() ) );
		assertEquals( 7, LineageTreeUtils.getMaxTimepoint( new ExampleGraph2().getModel() ) );
	}
}
