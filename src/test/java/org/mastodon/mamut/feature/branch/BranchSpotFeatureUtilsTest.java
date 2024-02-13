package org.mastodon.mamut.feature.branch;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.AbstractExampleGraph;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

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
	public void testNormalizedDirection()
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

	@Test
	public void testRelativeMovement()
	{
		ExampleGraph3 graph3 = new ExampleGraph3();
		double actual = BranchSpotFeatureUtils.relativeMovement( graph3.branchSpotA, 2, graph3.getModel() );
		assertEquals( 2d, actual, 0d );
	}

	@Test
	public void testNormalizedRelativeMovementDirection()
	{
		ExampleGraph3 graph3 = new ExampleGraph3();
		double[] actual = BranchSpotFeatureUtils.normalizedRelativeMovementDirection( graph3.branchSpotA, 2, graph3.getModel() );
		assertArrayEquals( new double[] { 0, 1, 0 }, actual, 0d );
	}

	/**
	 * Represents a {@link AbstractExampleGraph} with the following {@link ModelGraph} and {@link ModelBranchGraph}:
	 *
	 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
	 * <pre>
	 * Spot( 0, X=1, Y=1, tp=0 )		Spot( 3, X=0, Y=1, tp=0 )		Spot( 0, X=2, Y=1, tp=0 )
	 *              │								 │								 │
	 * Spot( 1, X=1, Y=2, tp=1 )		Spot( 4, X=0, Y=0, tp=1 )	    Spot( 1, X=2, Y=0, tp=1 )
	 *              │								 │								 │
	 * Spot( 2, X=1, Y=3, tp=2 )        Spot( 5, X=0, Y=-1, tp=2 )		Spot( 2, X=2, Y=-1, tp=2 )
	 * </pre>
	 * <h1>Branch-Graph (i.e. Graph of BranchSpots)</h1>
	 * <pre>
	 * branchSpotA						branchSpotB						branchSpotC
	 * </pre>
	 */
	private static class ExampleGraph3 extends AbstractExampleGraph
	{

		private final BranchSpot branchSpotA;

		private ExampleGraph3()
		{
			Spot spot0 = addNode( "0", 0, new double[] { 1d, 1d, 0d } );
			Spot spot1 = addNode( "1", 1, new double[] { 1d, 2d, 0d } );
			Spot spot2 = addNode( "2", 2, new double[] { 1d, 3d, 0d } );
			Spot spot3 = addNode( "3", 0, new double[] { 0d, 1d, 0d } );
			Spot spot4 = addNode( "4", 1, new double[] { 0d, 0d, 0d } );
			Spot spot5 = addNode( "5", 2, new double[] { 0d, -1d, 0d } );
			Spot spot6 = addNode( "6", 0, new double[] { 2d, 1d, 0d } );
			Spot spot7 = addNode( "7", 1, new double[] { 2d, 0d, 0d } );
			Spot spot8 = addNode( "8", 2, new double[] { 2d, -1d, 0d } );

			addEdge( spot0, spot1 );
			addEdge( spot1, spot2 );
			addEdge( spot3, spot4 );
			addEdge( spot4, spot5 );
			addEdge( spot6, spot7 );
			addEdge( spot7, spot8 );

			branchSpotA = getBranchSpot( spot0 );
			BranchSpot branchSpotB = getBranchSpot( spot3 );
			BranchSpot branchSpotC = getBranchSpot( spot6 );
		}
	}
}
