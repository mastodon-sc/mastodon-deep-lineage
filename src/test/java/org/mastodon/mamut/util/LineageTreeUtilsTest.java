package org.mastodon.mamut.util;

import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Model;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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

	@Test
	public void testGetFirstTimepointWithNSpots()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		Model model = exampleGraph2.getModel();

		assertEquals( 5, LineageTreeUtils.getFirstTimepointWithNSpots( model, 3 ) );
		assertEquals( 3, LineageTreeUtils.getFirstTimepointWithNSpots( model, 2 ) );
		assertThrows( NoSuchElementException.class, () -> LineageTreeUtils.getFirstTimepointWithNSpots( model, 5 ) );
	}
}
