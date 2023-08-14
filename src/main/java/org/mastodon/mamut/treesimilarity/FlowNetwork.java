package org.mastodon.mamut.treesimilarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

class FlowNetwork
{

	private final SimpleDirectedWeightedGraph< Object, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );

	private final Map< DefaultWeightedEdge, Integer > capacities = new HashMap<>();

	private MinimumCostFlowAlgorithm.MinimumCostFlow< DefaultWeightedEdge > flow;

	public void addVertices( Collection< ? > vertices )
	{
		vertices.forEach( graph::addVertex );
	}

	public void addEdge( Object source, Object target, int capacity, double weight )
	{
		DefaultWeightedEdge e1 = graph.addEdge( source, target );
		graph.setEdgeWeight( e1, weight );
		capacities.put( e1, capacity );
	}

	public void solveMaxFlowMinCost( Object source, Object sink )
	{
		flow = JGraphtTools.maxFlowMinCost( graph, capacities, source, sink );
	}

	public double getFlow( Object source, Object target )
	{
		return flow.getFlow( graph.getEdge( source, target ) );
	}
}
