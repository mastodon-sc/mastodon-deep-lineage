/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.treesimilarity.util;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JGraphtTools
{

	private JGraphtTools()
	{
		// prevent from instantiation
	}

	/**
	 * Computes a maximum (source, sink)-flow of minimum cost and returns it.
	 * Assuming {@code graph} is a digraph with edge costs and capacities. There is a source node s and a sink node t. This function finds a maximum flow from s to t whose total cost is minimized.
	 *
	 * @param graph      a directed graph with edge costs (i.e. edge weights)
	 * @param capacities a map from edges to their capacities
	 * @param source     the source node
	 * @param sink       the sink node
	 * @return the maximum flow of minimum cost
	 */
	public static < V > MinimumCostFlowAlgorithm.MinimumCostFlow< DefaultWeightedEdge > maxFlowMinCost( final SimpleDirectedWeightedGraph< V, DefaultWeightedEdge > graph,
			final Map< DefaultWeightedEdge, Integer > capacities, final V source, final V sink )
	{
		// Intermediately save the edge weights, since they need to be overwritten with capacities for the maximum flow algorithm
		Map< DefaultWeightedEdge, Double > weights = new HashMap<>();
		for ( DefaultWeightedEdge edge : graph.edgeSet() )
			weights.put( edge, graph.getEdgeWeight( edge ) );

		// Set the capacities as edge weights
		// Edges, for which no capacity is set in the given capacities map, are assumed to have Integer.MAX_VALUE capacity
		for ( DefaultWeightedEdge edge : graph.edgeSet() )
			graph.setEdgeWeight( edge, capacities.getOrDefault( edge, Integer.MAX_VALUE ) );

		// Compute the maximum flow value
		MaximumFlowAlgorithm< V, DefaultWeightedEdge > maximumFlowAlgorithm = new PushRelabelMFImpl<>( graph );
		double maximumFlowValue = maximumFlowAlgorithm.getMaximumFlowValue( source, sink );

		// Now set the actual edge weights again
		for ( DefaultWeightedEdge edge : graph.edgeSet() )
			graph.setEdgeWeight( edge, weights.get( edge ) );

		// Create supplies for the minimum cost flow problem
		Function< V, Integer > supplies = v -> {
			if ( v.equals( source ) )
				return ( int ) maximumFlowValue;
			else if ( v.equals( sink ) )
				return -( int ) maximumFlowValue;
			else
				return 0;
		};

		MinimumCostFlowProblem< V, DefaultWeightedEdge > problem =
				new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>( graph, supplies, capacities::get );
		CapacityScalingMinimumCostFlow< V, DefaultWeightedEdge > minimumCostFlowAlgorithm =
				new CapacityScalingMinimumCostFlow<>();
		return minimumCostFlowAlgorithm.getMinimumCostFlow( problem );
	}
}
