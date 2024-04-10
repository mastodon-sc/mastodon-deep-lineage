/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature.spot;

import net.imglib2.util.LinAlgHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph5;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpotFeatureUtilsTest
{
	private ExampleGraph1 graph1;

	private ExampleGraph2 graph2;

	private ExampleGraph5 graph5;

	@BeforeEach
	void setUp()
	{
		graph1 = new ExampleGraph1();
		graph2 = new ExampleGraph2();
		graph5 = new ExampleGraph5();
	}

	@Test
	void testSpotMovement()
	{
		double[] expected = new double[] { 1, 2, 3 };
		double[] actual = SpotFeatureUtils.spotMovement( graph1.spot1 );
		assertArrayEquals( expected, actual, 0 );
	}

	@Test
	void testSpotMovementNoPredecessor()
	{
		assertNull( SpotFeatureUtils.spotMovement( graph1.spot0 ) );
	}

	@SuppressWarnings( "all" )
	@Test
	void testSpotMovementNull()
	{
		assertThrows( IllegalArgumentException.class, () -> SpotFeatureUtils.spotMovement( null ) );
	}

	@Test
	void testRelativeMovement()
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
	void testRelativeMovementException()
	{
		Model model = graph2.getModel();
		assertThrows( IllegalArgumentException.class, () -> SpotFeatureUtils.relativeMovement( null, 2, model ) );
		assertThrows( IllegalArgumentException.class, () -> SpotFeatureUtils.relativeMovement( graph2.spot13, 0, model ) );
	}

	@Test
	void testRelativeMovementNoNeighbors()
	{
		assertNull( SpotFeatureUtils.relativeMovement( graph2.spot1, 2, graph2.getModel() ) );
	}

	@Test
	void testGetNNearestNeighbors()
	{
		Predicate< Spot > excludeRootNeighbors = neighbor -> neighbor.incomingEdges().isEmpty();
		List< Spot > neighbors0 = SpotFeatureUtils.getNNearestNeighbors( graph2.getModel(), graph2.spot13, 1, excludeRootNeighbors );
		List< Spot > neighbors2 = SpotFeatureUtils.getNNearestNeighbors( graph2.getModel(), graph2.spot13, 2, excludeRootNeighbors );
		assertArrayEquals( new Spot[] { graph2.spot8 }, neighbors0.toArray() );
		assertArrayEquals( new Spot[] { graph2.spot8, graph2.spot5 }, neighbors2.toArray() );
	}

	@Test
	void testNeighborsAverageMovement()
	{
		assertNull(
				SpotFeatureUtils.neighborsAverageMovement( graph5.spot0, 2, graph5.getModel(), SpotFeatureUtils::spotMovement, null ) );
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
