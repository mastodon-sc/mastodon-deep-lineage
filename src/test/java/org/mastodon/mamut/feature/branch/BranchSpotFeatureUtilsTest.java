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

	@Test
	public void testGetSpotRef()
	{
		Spot spotRef = BranchSpotFeatureUtils.getSpotRef( graph.getModel() );
		graph.getModel().getBranchGraph().getLastLinkedVertex( graph.branchSpotA, spotRef );
		assertEquals( graph.spot4, spotRef );
		graph.getModel().getGraph().releaseRef( spotRef );
	}

	@Test
	public void testDirectDistance()
	{
		double expected = Math.sqrt( 16 + 64 + 144 );
		assertEquals( expected, BranchSpotFeatureUtils.directDistance( graph.getModel(), graph.branchSpotA ), 0d );
	}

	@Test
	public void testGetFirstSpotCoordinates()
	{
		double[] expected = new double[] { 1, 2, 3 };
		assertArrayEquals( expected, BranchSpotFeatureUtils.getFirstSpotCoordinates( graph.getModel(), graph.branchSpotA ), 0d );
	}

	@Test
	public void testGetLastSpotCoordinates()
	{
		double[] expected = new double[] { 5, 10, 15 };
		assertArrayEquals( expected, BranchSpotFeatureUtils.getLastSpotCoordinates( graph.getModel(), graph.branchSpotA ), 0d );
	}

	@Test
	public void normalizedDirection()
	{
		double[] normalizedDirection = BranchSpotFeatureUtils.normalizedDirection( graph.getModel(), graph.branchSpotA );
		double distance = Math.sqrt( 16 + 64 + 144 );
		double expectedX = 4 / distance;
		double expectedY = 8 / distance;
		double expectedZ = 12 / distance;
		assertEquals( expectedX, normalizedDirection[ 0 ], 0d );
		assertEquals( expectedY, normalizedDirection[ 1 ], 0d );
		assertEquals( expectedZ, normalizedDirection[ 2 ], 0d );
	}
}
