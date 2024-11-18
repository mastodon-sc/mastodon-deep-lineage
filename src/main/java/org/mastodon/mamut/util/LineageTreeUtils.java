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
package org.mastodon.mamut.util;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.Graph;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.BreadthFirstIterator;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.util.TreeUtils;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LineageTreeUtils {

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private LineageTreeUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Performs a depth-first iteration through the specified {@link Graph} starting from each root.
	 * <p>
	 * The iteration is performed in a single thread.
	 * <p>
	 * The given {@code reverseProcessor} is called for each vertex in the graph, when all its descendants in the search tree have been iterated through or when it is a leaf in the tree.
	 *
	 * @param graph the graph to iterate through.
	 * @param reverseProcessor optional action to performed on each vertex. Vertices are effectively processed in the reverse order that of their discovery.
	 * @param stopCondition optional condition that, when supplies true, stops the iteration before the next root is processed.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > void callDepthFirst(
			Graph< V, E > graph, @Nullable Consumer< V > reverseProcessor, @Nullable BooleanSupplier stopCondition )
	{
		callDepthFirst( graph, reverseProcessor, null, stopCondition );
	}

	/**
	 * Performs a depth-first iteration through the specified {@link Graph} starting from each root.
	 * <p>
	 * The iteration is performed in a single thread.
	 * Performs a depth-first iteration through the specified {@link Graph} starting from each root.
	 * <p>
	 * The iteration is performed in a single thread.
	 * The given {@code reverseProcessor} is called for each vertex in the graph, when all its descendants in the search tree have been iterated through or when it is a leaf in the tree.
	 * The given {@code forwardProcessor} is called for each vertex in the graph, when it is discovered.
	 *
	 * @param graph the graph to iterate through.
	 * @param reverseProcessor optional action to performed on each vertex. Vertices are effectively processed in the reverse order that of their discovery.
	 * @param forwardProcessor optional action performed on each vertex. Called when a vertex is discovered during the depth-first search.
	 * @param stopCondition optional condition that, when supplies true, stops the iteration before the next root is processed.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > void callDepthFirst(
			Graph< V, E > graph, @Nullable Consumer< V > reverseProcessor, @Nullable Consumer< V > forwardProcessor,
			@Nullable BooleanSupplier stopCondition )
	{
		DepthFirstSearch< V, E > search = new DepthFirstSearch<>( graph, GraphSearch.SearchDirection.DIRECTED );
		search.setTraversalListener( new SearchListener< V, E, DepthFirstSearch< V, E > >()
		{
			@Override
			public void processVertexLate( V vertex, DepthFirstSearch< V, E > search )
			{
				if ( reverseProcessor == null )
					return;
				reverseProcessor.accept( vertex );
			}

			@Override
			public void processVertexEarly( V vertex, DepthFirstSearch< V, E > search )
			{
				if ( forwardProcessor == null )
					return;
				forwardProcessor.accept( vertex );
			}

			@Override
			public void processEdge( E edge, V from, V to, DepthFirstSearch< V, E > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void crossComponent( V from, V to, DepthFirstSearch< V, E > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}
		} );
		final RefSet< V > roots = RootFinder.getRoots( graph );
		for ( V root : roots )
		{
			if ( stopCondition != null && stopCondition.getAsBoolean() )
				break;
			search.start( root );
		}
	}

	/**
	 * Gets the first time point that has at least the given number of spots ({@code numberOfSpots})
	 * by iterating through the {@link org.mastodon.spatial.SpatioTemporalIndex} of the given {@link Model}.
	 *
	 * @param model the {@link Model} to search in
	 * @param numberOfSpots the number of spots to search for
	 * @return the first time point with at least the given number of spots
	 * @throws NoSuchElementException if no time point with at least the given number of spots exists
	 */
	public static int getFirstTimepointWithNSpots( final Model model, final int numberOfSpots )
	{
		int minTimepoint = TreeUtils.getMinTimepoint( model );
		int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
			if ( model.getSpatioTemporalIndex().getSpatialIndex( timepoint ).size() >= numberOfSpots )
				return timepoint;
		throw new NoSuchElementException(
				"No time point with at least " + numberOfSpots + " spots in the range [minTimepoint=" + minTimepoint + ", maxTimepoint="
						+ maxTimepoint + "]. Maximum number of spots in a single time point in this model is: " + getMaxSpots( model )
						+ "." );
	}

	/**
	 * Gets the maximum number of spots in a single time point by iterating through the {@link org.mastodon.spatial.SpatioTemporalIndex} of the given {@link Model}.
	 * @param model the {@link Model} to search in
	 * @return the maximum number of spots in a single time point
	 */
	public static int getMaxSpots( final Model model )
	{
		int maxSpots = 0;
		int minTimepoint = TreeUtils.getMinTimepoint( model );
		int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
			maxSpots = Math.max( maxSpots, model.getSpatioTemporalIndex().getSpatialIndex( timepoint ).size() );
		return maxSpots;
	}

	// Replace with new method after has been resolved https://github.com/mastodon-sc/mastodon-tomancak/issues/13
	public static RefSet< Spot > getRoots( ModelGraph graph, int timepoint )
	{
		Predicate< Spot > isRoot = spot -> spot.getTimepoint() == timepoint
				|| ( spot.incomingEdges().isEmpty() && spot.getTimepoint() > timepoint );
		return filterSet( graph.vertices(), isRoot );
	}

	/**
	 * Returns a new {@link RefSet} containing all elements of the given
	 * {@link RefCollection} that satisfy the given {@link Predicate}.
	 */
	public static < T > RefSet< T > filterSet( RefCollection< T > values, Predicate< T > predicate )
	{
		RefSet< T > filtered = RefCollections.createRefSet( values );
		for ( T t : values )
			if ( predicate.test( t ) )
				filtered.add( t );
		return filtered;
	}

	/**
	 * Tests, whether the given vertex is a root in the given graph.
	 * <br><br>
	 * A root is a vertex with no incoming edges.
	 * <br><br>
	 * @param vertex the vertex to test.
	 * @param <V> the vertex type.
	 * @param <E> the edge type.
	 * @return true if the vertex is a root, false otherwise.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > boolean isRoot( final V vertex )
	{
		return vertex.incomingEdges().isEmpty();
	}

	/**
	 * Gets a new {@link RefSet} that contains all branch spots that share at least one of the given spots or links.
	 * @param model the model to search in.
	 * @param spots the spots used to search for branch spots.
	 * @param links the links used to search for branch spots.
	 * @return the resulting branch spots.
	 */
	public static RefSet< BranchSpot > getBranchSpots( final Model model, final RefSet< Spot > spots, final RefSet< Link > links )
	{
		ModelBranchGraph branchGraph = model.getBranchGraph();
		RefSet< BranchSpot > selectedBranchSpots = RefCollections.createRefSet( model.getBranchGraph().vertices() );
		if ( spots != null )
			spots.forEach( spot -> selectedBranchSpots.add( branchGraph.getBranchVertex( spot, branchGraph.vertexRef() ) ) );
		if ( links != null )
			links.forEach( link -> {
				BranchSpot branchSpot = branchGraph.getBranchVertex( link, branchGraph.vertexRef() );
				if ( branchSpot != null )
					selectedBranchSpots.add( branchSpot );
			} );
		return selectedBranchSpots;
	}

	/**
	 * Gets a new {@link RefSet} that contains all branch links that share at least one of the given links.
	 * @param model the model to search in.
	 * @param links the links used to search for branch links.
	 * @return the resulting branch links.
	 */
	public static RefSet< BranchLink > getBranchLinks( final Model model, final Set< Link > links )
	{
		ModelBranchGraph branchGraph = model.getBranchGraph();
		RefSet< BranchLink > branchLinks = RefCollections.createRefSet( model.getBranchGraph().edges() );
		if ( links != null )
			links.forEach( link -> {
				BranchLink branchLink = branchGraph.getBranchEdge( link, branchGraph.edgeRef() );
				if ( branchLink != null )
					branchLinks.add( branchLink );
			} );
		return branchLinks;
	}

	/**
	 * Gets all the vertex successors of the given root in the given graph, including the root.
	 * <br><br>
	 * The vertex successors of a vertex are all the vertices that are reachable from the root by following the edges of the graph.
	 * <br><br>
	 * @param root the root vertex.
	 * @param graph the graph to search in.
	 * @param <V> the vertex type.
	 * @param <E> the edge type.
	 * @return a new {@link RefSet} containing all the successors of the root.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > RefSet< V > getAllVertexSuccessors( final V root,
			final ReadOnlyGraph< V, E > graph )
	{
		BreadthFirstIterator< V, E > it = new BreadthFirstIterator<>( root, graph );
		RefSet< V > children = RefCollections.createRefSet( graph.vertices() );
		while ( it.hasNext() )
			children.add( it.next() );
		return children;
	}

	/**
	 * Gets all the edge successors of the given root in the given graph.
	 * <br><br>
	 * The edge successors of a vertex are all the vertices that are reachable from the root by following the edges of the graph.
	 * <br><br>
	 * @param root the root vertex.
	 * @param graph the graph to search in.
	 * @param <V> the vertex type.
	 * @param <E> the edge type.
	 * @return a new {@link RefSet} containing all the edge successors of the root.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > RefSet< E > getAllEdgeSuccessors( final V root,
			final ReadOnlyGraph< V, E > graph )
	{
		BreadthFirstIterator< V, E > it = new BreadthFirstIterator<>( root, graph );
		RefSet< E > children = RefCollections.createRefSet( graph.edges() );
		while ( it.hasNext() )
		{
			V vertex = it.next();
			Edges< E > outgoingEdges = vertex.outgoingEdges();
			outgoingEdges.forEach( children::add );
		}
		return children;
	}

	/**
	 * This method adds edges to the graph of the model between spots that have the same label and are in consecutive time points.
	 * <br><br>
	 * E.g. assume this set of spots:
	 * <pre>
	 * Spot(label=0,X=1,Y=1,tp=0)        Spot(label=1,X=0,Y=1,tp=0 )       Spot(label=2,X=2,Y=1,tp=0 )
	 *
	 * Spot(label=0,X=1,Y=2,tp=1)        Spot(label=1,X=0,Y=0,tp=1 )       Spot(label=2,X=2,Y=0,tp=1 )
	 *
	 * Spot(label=0,X=1,Y=3,tp=2 )       Spot(label=1,X=0,Y=-1,tp=2 )      Spot(label=3,X=2,Y=-1,tp=2 )
	 *
	 * Spot(label=0,X=1,Y=4,tp=3 )       Spot(label=0,X=0,Y=-2,tp=3 )
	 * </pre>
	 * This method will add edges between the following spots:
	 * <pre>
	 * Spot(label=0,X=1,Y=1,tp=0)        Spot(label=1,X=0,Y=1,tp=0 )       Spot(label=2,X=2,Y=1,tp=0 )
	 *              │                                 │                                 │
	 * Spot(label=0,X=1,Y=2,tp=1)        Spot(label=1,X=0,Y=0,tp=1 )       Spot(label=2,X=2,Y=0,tp=1 )
	 *              │                                 │
	 * Spot(label=0,X=1,Y=3,tp=2 )       Spot(label=1,X=0,Y=-1,tp=2 )      Spot(label=3,X=2,Y=-1,tp=2 )
	 *       ┌──────┴────────────────────────────┐
	 * Spot(label=0,X=1,Y=4,tp=3 )  Spot(label=0,X=0,Y=-2,tp=3 )
	 * </pre>
	 * @param model the model to link spots in.
	 */
	public static void linkSpotsWithSameLabel( final Model model, final StatusService statusService )
	{
		Link edgeRef = model.getGraph().edgeRef();
		int minTimepoint = TreeUtils.getMinTimepoint( model );
		int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		logger.debug( "Linking spots with the same label in consecutive timepoints from timepoint {} to timepoint {}.", minTimepoint,
				maxTimepoint );
		for ( int timepoint = minTimepoint; timepoint < maxTimepoint; timepoint++ )
		{
			if ( statusService != null )
				statusService.showProgress( timepoint + 1, maxTimepoint );
			AtomicInteger edgeCounter = new AtomicInteger( 0 );
			SpatialIndex< Spot > currentTimepoint = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			SpatialIndex< Spot > nextTimepoint = model.getSpatioTemporalIndex().getSpatialIndex( timepoint + 1 );
			currentTimepoint.forEach( spotA -> nextTimepoint.forEach( spotB -> {
				if ( spotA.getLabel().equals( spotB.getLabel() ) )
				{
					model.getGraph().addEdge( spotA, spotB, edgeRef ).init();
					edgeCounter.incrementAndGet();
				}
			} ) );
			logger.debug( "Added {} edge(s) between spots in time point {} and {}.", edgeCounter.get(), timepoint, timepoint + 1 );
		}
		model.getGraph().releaseRef( edgeRef );
		logger.debug( "Added {} edge(s) to the graph.", model.getGraph().edges().size() );
	}
}
