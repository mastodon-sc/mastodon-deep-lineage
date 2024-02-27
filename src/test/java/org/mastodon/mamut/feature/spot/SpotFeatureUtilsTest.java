package org.mastodon.mamut.feature.spot;

import net.imglib2.util.LinAlgHelpers;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph5;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class SpotFeatureUtilsTest
{
	private ExampleGraph1 graph1;

	private ExampleGraph2 graph2;

	private ExampleGraph5 graph5;

	@Before
	public void setUp()
	{
		graph1 = new ExampleGraph1();
		graph2 = new ExampleGraph2();
		graph5 = new ExampleGraph5();
	}

	@Test
	public void testSpotMovement()
	{
		double[] expected = new double[] { 1, 2, 3 };
		double[] actual = SpotFeatureUtils.spotMovement( graph1.spot1 );
		assertArrayEquals( expected, actual, 0 );
	}

	@Test
	public void testSpotMovementNoPredecessor()
	{
		assertNull( SpotFeatureUtils.spotMovement( graph1.spot0 ) );
	}

	@SuppressWarnings( "all" )
	@Test
	public void testSpotMovementNull()
	{
		assertThrows( IllegalArgumentException.class, () -> SpotFeatureUtils.spotMovement( null ) );
	}

	@Test
	public void testRelativeMovement()
	{
		double[] expected = new double[ 3 ];
		double[] movementSpot8 = SpotFeatureUtils.spotMovement( graph2.spot8 );
		double[] movementSpot5 = SpotFeatureUtils.spotMovement( graph2.spot5 );
		assertNotNull( movementSpot8 );
		LinAlgHelpers.add( movementSpot8, movementSpot5, movementSpot8 );
		LinAlgHelpers.scale( movementSpot8, 1 / 2d, movementSpot8 );
		double[] movementSpot13 = SpotFeatureUtils.spotMovement( graph2.spot13 );
		assertNotNull( movementSpot13 );
		LinAlgHelpers.subtract( movementSpot13, movementSpot8, expected );
		double[] actual = SpotFeatureUtils.relativeMovement( graph2.spot13, 2, graph2.getModel() );
		assertArrayEquals( expected, actual, 0d );
	}

	@SuppressWarnings( "all" )
	@Test
	public void testRelativeMovementException()
	{
		Model model = graph2.getModel();
		assertThrows( IllegalArgumentException.class, () -> SpotFeatureUtils.relativeMovement( null, 2, model ) );
		assertThrows( IllegalArgumentException.class, () -> SpotFeatureUtils.relativeMovement( graph2.spot13, 0, model ) );
	}

	@Test
	public void testRelativeMovementNoNeighbors()
	{
		assertNull( SpotFeatureUtils.relativeMovement( graph2.spot1, 2, graph2.getModel() ) );
	}

	@Test
	public void testGetNNearestNeighbors()
	{
		Predicate< Spot > excludeRootNeighbors = neighbor -> neighbor.incomingEdges().isEmpty();
		List< Spot > neighbors0 = SpotFeatureUtils.getNNearestNeighbors( graph2.getModel(), graph2.spot13, 1, excludeRootNeighbors );
		List< Spot > neighbors2 = SpotFeatureUtils.getNNearestNeighbors( graph2.getModel(), graph2.spot13, 2, excludeRootNeighbors );
		assertArrayEquals( new Spot[] { graph2.spot8 }, neighbors0.toArray() );
		assertArrayEquals( new Spot[] { graph2.spot8, graph2.spot5 }, neighbors2.toArray() );
	}

	@Test
	public void testNeighborsAverageMovement()
	{
		assertNull( SpotFeatureUtils.neighborsAverageMovement( graph5.spot0, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ) );
		assertArrayEquals( new double[] { 0, 2d, 0 },
				SpotFeatureUtils.neighborsAverageMovement( graph5.spot1, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ), 0d );
		assertArrayEquals( new double[] { 0, 2d, 0 },
				SpotFeatureUtils.neighborsAverageMovement( graph5.spot2, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ), 0d );
		assertArrayEquals( new double[] { 0, 2d, 0 },
				SpotFeatureUtils.neighborsAverageMovement( graph5.spot3, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ), 0d );
		assertArrayEquals( new double[] { 1d, 2d, 0 },
				SpotFeatureUtils.neighborsAverageMovement( graph5.spot4, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ), 0d );
		assertArrayEquals( new double[] { 0, 2d, 0 },
				SpotFeatureUtils.neighborsAverageMovement( graph5.spot5, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ), 0d );
	}
}
