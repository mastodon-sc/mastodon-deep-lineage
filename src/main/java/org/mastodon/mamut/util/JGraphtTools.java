package org.mastodon.mamut.util;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.flow.mincost.CapacityScalingMinimumCostFlow;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.HashMap;
import java.util.Map;

public class JGraphtTools
{

	private JGraphtTools()
	{
		// prevent from instantiation
	}

	/**
	 * Computes a maximum (source, sink)-flow of minimum cost and returns its cost.
	 * G is a digraph with edge costs and capacities. There is a source node s and a sink node t. This function finds a maximum flow from s to t whose total cost is minimized.
	 * @param graph a directed graph with edge costs (i.e. edge weights)
	 * @param capacities a map from edges to their capacities
	 * @param source the source node
	 * @param sink the sink node
	 * @return the minimum cost of the maximum flow
	 */
	public static < V > double maxFlowMinCost( SimpleDirectedWeightedGraph< V, DefaultWeightedEdge > graph,
			Map< DefaultWeightedEdge, Integer > capacities, V source, V sink )
	{
		// Intermediately save the edge weights, since they need to be overwritten with capacities for the maximum flow algorithm
		Map< DefaultWeightedEdge, Double > weights = new HashMap<>();
		for ( DefaultWeightedEdge edge : graph.edgeSet() )
		{
			weights.put( edge, graph.getEdgeWeight( edge ) );
		}

		// Set the capacities as edge weights
		// Edges, for which no capacity is set in the given capacities map, are assumed to have Integer.MAX_VALUE capacity
		for ( DefaultWeightedEdge defaultWeightedEdge : graph.edgeSet() )
		{
			graph.setEdgeWeight( defaultWeightedEdge,
					capacities.get( defaultWeightedEdge ) == null ? Integer.MAX_VALUE : capacities.get( defaultWeightedEdge ) );
		}

		// Compute the maximum flow value
		MaximumFlowAlgorithm< V, DefaultWeightedEdge > maximumFlowAlgorithm = new PushRelabelMFImpl<>( graph );
		double maximumFlowValue = maximumFlowAlgorithm.getMaximumFlowValue( source, sink );

		// Now set the actual edge weights again
		for ( DefaultWeightedEdge edge : graph.edgeSet() )
		{
			graph.setEdgeWeight( edge, weights.get( edge ) );
		}

		// Create demands for the minimum cost flow problem
		Map< V, Integer > demands = new HashMap<>();
		for ( V v : graph.vertexSet() )
		{
			if ( v.equals( source ) )
				demands.put( v, ( int ) maximumFlowValue );
			else if ( v.equals( sink ) )
				demands.put( v, -( int ) maximumFlowValue );
			else
				demands.put( v, 0 );
		}

		MinimumCostFlowProblem< V, DefaultWeightedEdge > problem =
				new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>( graph, demands::get, capacities::get );
		CapacityScalingMinimumCostFlow< V, DefaultWeightedEdge > minimumCostFlowAlgorithm =
				new CapacityScalingMinimumCostFlow<>();
		return minimumCostFlowAlgorithm.getMinimumCostFlow( problem ).getCost();
	}
}
