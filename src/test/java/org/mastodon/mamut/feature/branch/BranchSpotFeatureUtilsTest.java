package org.mastodon.mamut.feature.branch;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.Spot;

import java.util.Iterator;

import static org.junit.Assert.*;

public class BranchSpotFeatureUtilsTest
{
	private ExampleGraph1 graph;

	@Before
	public void setUp()
	{
		graph = new ExampleGraph1();
	}

	@Test
	public void testGetSpotIterator()
	{
		Iterator< Spot > spotIterator = BranchSpotFeatureUtils.getSpotIterator( graph.getModel(), graph.branchSpotA );
		assertNotNull( spotIterator );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph.spot0, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph.spot1, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph.spot2, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph.spot3, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph.spot4, spotIterator.next() );
		assertFalse( spotIterator.hasNext() );
		graph.getModel().getBranchGraph().releaseIterator( spotIterator );
	}

	@Test
	public void testCumulatedDistance()
	{
		double expected = 4 * Math.sqrt( 1 + 4 + 9 );
		assertEquals( expected, BranchSpotFeatureUtils.cumulatedDistance( graph.getModel(), graph.branchSpotA ), 0d );
	}
}
