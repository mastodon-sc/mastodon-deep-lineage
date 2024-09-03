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
package org.mastodon.mamut.clustering.treesimilarity.util;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JGraphtToolsTest
{
	@Test
	void testMinCostMaxFlow()
	{
		SimpleDirectedWeightedGraph< Integer, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );

		graph.addVertex( 1 );
		graph.addVertex( 2 );
		graph.addVertex( 3 );
		graph.addVertex( 4 );
		graph.addVertex( 5 );
		graph.addVertex( 6 );
		graph.addVertex( 7 );

		DefaultWeightedEdge e1 = graph.addEdge( 1, 2 );
		DefaultWeightedEdge e2 = graph.addEdge( 1, 3 );
		DefaultWeightedEdge e3 = graph.addEdge( 2, 3 );
		DefaultWeightedEdge e4 = graph.addEdge( 2, 6 );
		DefaultWeightedEdge e5 = graph.addEdge( 3, 4 );
		DefaultWeightedEdge e6 = graph.addEdge( 3, 5 );
		DefaultWeightedEdge e7 = graph.addEdge( 4, 2 );
		DefaultWeightedEdge e8 = graph.addEdge( 4, 5 );
		DefaultWeightedEdge e9 = graph.addEdge( 5, 7 );
		DefaultWeightedEdge e10 = graph.addEdge( 6, 5 );
		DefaultWeightedEdge e11 = graph.addEdge( 6, 7 );
		DefaultWeightedEdge e12 = graph.addEdge( 7, 4 );

		// overwrite capacities for the minimum cost flow problem with the actual cost values
		graph.setEdgeWeight( e1, 4 );
		graph.setEdgeWeight( e2, 6 );
		graph.setEdgeWeight( e3, -3 );
		graph.setEdgeWeight( e4, 1 );
		graph.setEdgeWeight( e5, 9 );
		graph.setEdgeWeight( e6, 5 );
		graph.setEdgeWeight( e7, 13 );
		graph.setEdgeWeight( e8, 0 );
		graph.setEdgeWeight( e9, 2 );
		graph.setEdgeWeight( e10, 1 );
		graph.setEdgeWeight( e11, 8 );
		graph.setEdgeWeight( e12, 6 );

		// capacities for the minimum cost flow problem
		Map< DefaultWeightedEdge, Integer > capacities = new HashMap<>();
		capacities.put( e1, 12 );
		capacities.put( e2, 20 );
		capacities.put( e3, 6 );
		capacities.put( e4, 14 );
		capacities.put( e5, Integer.MAX_VALUE );
		capacities.put( e6, 10 );
		capacities.put( e7, 19 );
		capacities.put( e8, 4 );
		capacities.put( e9, 28 );
		capacities.put( e10, 11 );
		capacities.put( e11, Integer.MAX_VALUE );
		capacities.put( e12, 6 );

		double cost = JGraphtTools.maxFlowMinCost( graph, capacities, 1, 7 ).getCost();

		assertEquals( 373, ( int ) cost );
	}
}
