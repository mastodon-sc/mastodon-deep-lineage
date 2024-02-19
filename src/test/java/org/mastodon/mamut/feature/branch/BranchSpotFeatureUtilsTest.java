package org.mastodon.mamut.feature.branch;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph3;
import org.mastodon.mamut.model.Spot;

import java.util.Iterator;

import static org.junit.Assert.*;

public class BranchSpotFeatureUtilsTest
{
	private ExampleGraph1 graph1;

	private ExampleGraph3 graph3;

	@Before
	public void setUp()
	{
		graph1 = new ExampleGraph1();
		graph3 = new ExampleGraph3();
	}

	@Test
	public void testGetSpotIterator()
	{
		Iterator< Spot > spotIterator = BranchSpotFeatureUtils.getSpotIterator( graph1.getModel(), graph1.branchSpotA );
		assertNotNull( spotIterator );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph1.spot0, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph1.spot1, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph1.spot2, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph1.spot3, spotIterator.next() );
		assertTrue( spotIterator.hasNext() );
		assertEquals( graph1.spot4, spotIterator.next() );
		assertFalse( spotIterator.hasNext() );
		graph1.getModel().getBranchGraph().releaseIterator( spotIterator );
	}

	@Test
	public void testCumulatedDistance()
	{
		double expected = 4 * Math.sqrt( 1 + 4 + 9 );
		assertEquals( expected, BranchSpotFeatureUtils.cumulatedDistance( graph1.getModel(), graph1.branchSpotA ), 0d );
	}

	@Test
	public void testGetSpotRef()
	{
		Spot spotRef = BranchSpotFeatureUtils.getSpotRef( graph1.getModel() );
		graph1.getModel().getBranchGraph().getLastLinkedVertex( graph1.branchSpotA, spotRef );
		assertEquals( graph1.spot4, spotRef );
		graph1.getModel().getGraph().releaseRef( spotRef );
	}

	@Test
	public void testDirectDistance()
	{
		double expected = Math.sqrt( 16 + 64 + 144 );
		assertEquals( expected, BranchSpotFeatureUtils.directDistance( graph1.getModel(), graph1.branchSpotA ), 0d );
	}

	@Test
	public void testGetFirstSpotCoordinates()
	{
		double[] expected = new double[] { 1, 2, 3 };
		assertArrayEquals( expected, BranchSpotFeatureUtils.getFirstSpotCoordinates( graph1.getModel(), graph1.branchSpotA ), 0d );
	}

	@Test
	public void testGetLastSpotCoordinates()
	{
		double[] expected = new double[] { 5, 10, 15 };
		assertArrayEquals( expected, BranchSpotFeatureUtils.getLastSpotCoordinates( graph1.getModel(), graph1.branchSpotA ), 0d );
	}

	@Test
	public void testNormalizedDirection()
	{
		double[] normalizedDirection = BranchSpotFeatureUtils.normalizedDirection( graph1.getModel(), graph1.branchSpotA );
		double distance = Math.sqrt( 16 + 64 + 144 );
		double expectedX = 4 / distance;
		double expectedY = 8 / distance;
		double expectedZ = 12 / distance;
		assertEquals( expectedX, normalizedDirection[ 0 ], 0d );
		assertEquals( expectedY, normalizedDirection[ 1 ], 0d );
		assertEquals( expectedZ, normalizedDirection[ 2 ], 0d );
	}

	@Test
	public void testRelativeMovement()
	{
		double actual = BranchSpotFeatureUtils.relativeMovement( graph3.branchSpotA, 2, graph3.getModel() );
		assertEquals( 2d, actual, 0d );
	}

	@Test
	public void testNormalizedRelativeMovementDirection()
	{
		double[] actual = BranchSpotFeatureUtils.normalizedRelativeMovementDirection( graph3.branchSpotA, 2, graph3.getModel() );
		assertArrayEquals( new double[] { 0, 1, 0 }, actual, 0d );
	}
}
