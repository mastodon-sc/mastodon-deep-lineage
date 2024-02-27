package org.mastodon.mamut.feature.branch;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph3;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph5;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.util.Iterator;

import static org.junit.Assert.*;

public class BranchSpotFeatureUtilsTest
{
	private ExampleGraph1 graph1;

	private ExampleGraph2 graph2;

	private ExampleGraph3 graph3;

	private ExampleGraph5 graph5;

	@Before
	public void setUp()
	{
		graph1 = new ExampleGraph1();
		graph2 = new ExampleGraph2();
		graph3 = new ExampleGraph3();
		graph5 = new ExampleGraph5();
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
	public void testCountLeaves()
	{
		assertEquals( 1, BranchSpotFeatureUtils.countLeaves( graph1.getModel().getBranchGraph(), graph1.branchSpotA ) );
		assertEquals( 3, BranchSpotFeatureUtils.countLeaves( graph2.getModel().getBranchGraph(), graph2.branchSpotA ) );
		assertEquals( 2, BranchSpotFeatureUtils.countLeaves( graph2.getModel().getBranchGraph(), graph2.branchSpotB ) );
		assertEquals( 1, BranchSpotFeatureUtils.countLeaves( graph2.getModel().getBranchGraph(), graph2.branchSpotC ) );
		assertEquals( 1, BranchSpotFeatureUtils.countLeaves( graph2.getModel().getBranchGraph(), graph2.branchSpotD ) );
		assertEquals( 1, BranchSpotFeatureUtils.countLeaves( graph2.getModel().getBranchGraph(), graph2.branchSpotE ) );
	}

	@Test
	public void testTotalBranchDurations()
	{
		assertEquals( 4, BranchSpotFeatureUtils.totalBranchDurations( graph1.getModel().getBranchGraph(), graph1.branchSpotA ), 0d );
		assertEquals( 14, BranchSpotFeatureUtils.totalBranchDurations( graph2.getModel().getBranchGraph(), graph2.branchSpotA ), 0d );
		assertEquals( 8, BranchSpotFeatureUtils.totalBranchDurations( graph2.getModel().getBranchGraph(), graph2.branchSpotB ), 0d );
		assertEquals( 3, BranchSpotFeatureUtils.totalBranchDurations( graph2.getModel().getBranchGraph(), graph2.branchSpotC ), 0d );
		assertEquals( 3, BranchSpotFeatureUtils.totalBranchDurations( graph2.getModel().getBranchGraph(), graph2.branchSpotD ), 0d );
		assertEquals( 3, BranchSpotFeatureUtils.totalBranchDurations( graph2.getModel().getBranchGraph(), graph2.branchSpotE ), 0d );

	}

	@Test
	public void testBranchDuration()
	{
		assertEquals( 4, BranchSpotFeatureUtils.branchDuration( graph1.branchSpotA ) );
		assertEquals( 3, BranchSpotFeatureUtils.branchDuration( graph2.branchSpotA ) );
		assertEquals( 2, BranchSpotFeatureUtils.branchDuration( graph2.branchSpotB ) );
		assertEquals( 3, BranchSpotFeatureUtils.branchDuration( graph2.branchSpotC ) );
		assertEquals( 3, BranchSpotFeatureUtils.branchDuration( graph2.branchSpotD ) );
		assertEquals( 3, BranchSpotFeatureUtils.branchDuration( graph2.branchSpotE ) );
	}

	@Test
	public void testRelativeMovementGraph3()
	{
		double actual = BranchSpotFeatureUtils.relativeMovement( graph3.branchSpotA, 2, graph3.getModel() );
		assertEquals( 2d, actual, 0d );
	}

	@Test
	public void testNormalizedRelativeMovementDirectionGraph3()
	{
		double[] actual = BranchSpotFeatureUtils.normalizedRelativeMovementDirection( graph3.branchSpotA, 2, graph3.getModel() );
		assertArrayEquals( new double[] { 0, 1, 0 }, actual, 0d );
	}

	@Test
	public void testRelativeMovementGraph5()
	{
		double expected = ( 4 * 1d + Math.sqrt( 2d ) ) / 5d;
		double actual = BranchSpotFeatureUtils.relativeMovement( graph5.branchSpotA, 2, graph5.getModel() );
		assertEquals( expected, actual, 0d );
	}

	@Test
	public void testNormalizedRelativeMovementDirectionGraph5()
	{
		double[] expected = new double[] { -1d / Math.sqrt( 26d ), -5d / Math.sqrt( 26 ), 0 };
		double[] actual = BranchSpotFeatureUtils.normalizedRelativeMovementDirection( graph5.branchSpotA, 2, graph5.getModel() );
		assertArrayEquals( expected, actual, 0d );
	}
}
