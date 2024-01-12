package org.mastodon.mamut.feature.branch;

import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.Spot;

import java.util.Iterator;

import static org.junit.Assert.*;

public class BranchSpotFeatureUtilsTest
{

	@Test
	public void testGetSpotIterator()
	{
		ExampleGraph1 graph = new ExampleGraph1();
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
	}

	@Test
	public void testCumulatedDistance()
	{
		ExampleGraph1 graph = new ExampleGraph1();
		double expected = 4 * Math.sqrt( 1 + 4 + 9 );
		assertEquals( expected, BranchSpotFeatureUtils.cumulatedDistance( graph.getModel(), graph.branchSpotA ), 0d );
	}
}
