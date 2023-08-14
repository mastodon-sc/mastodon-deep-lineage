package org.mastodon.mamut.treesimilarity.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * A utility class that encapsulates a graph ({@link SimpleDirectedWeightedGraph}), a corresponding map of edge capacities and a minimum cost flow solution for this graph.<p>
 *
 * @author Matthias Arzt
 * @author Stefan Hahmann
 */
public class FlowNetwork
{

	private final SimpleDirectedWeightedGraph< Object, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );

	private final Map< DefaultWeightedEdge, Integer > capacities = new HashMap<>();

	private MinimumCostFlowAlgorithm.MinimumCostFlow< DefaultWeightedEdge > flow;

	/**
	 * Adds a collection of vertices to the graph.
	 * @param vertices vertices to add
	 */
	public void addVertices( Collection< ? > vertices )
	{
		vertices.forEach( graph::addVertex );
	}

	/**
	 * Adds an edge to the graph with the given capacity and weight.
	 * @param source source vertex
	 * @param target target vertex
	 * @param capacity capacity of the edge
	 * @param weight weight of the edge
	 */
	public void addEdge( Object source, Object target, int capacity, double weight )
	{
		DefaultWeightedEdge e1 = graph.addEdge( source, target );
		graph.setEdgeWeight( e1, weight );
		capacities.put( e1, capacity );
	}

	/**
	 * Solves the maximum flow minimum cost problem on the graph for the given source and sink.
	 * @param source source vertex
	 * @param sink sink vertex
	 */
	public void solveMaxFlowMinCost( Object source, Object sink )
	{
		flow = JGraphtTools.maxFlowMinCost( graph, capacities, source, sink );
	}

	/**
	 * Returns the flow on the edge from source to target.<p>
	 * NB: The flow is only defined after {@link #solveMaxFlowMinCost(Object, Object)} has been called at least once.
	 * @param source source vertex
	 * @param target target vertex
	 * @return the flow on the edge from source to target
	 */
	public double getFlow( Object source, Object target )
	{
		if ( flow == null )
			throw new IllegalStateException( "Flow is not defined. Call solveMaxFlowMinCost() first." );
		return flow.getFlow( graph.getEdge( source, target ) );
	}
}
