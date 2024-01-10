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
package org.mastodon.mamut.util;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.PoolCollectionWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LineageTreeUtils {

	private LineageTreeUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Performs a depth-first iteration through the specified {@link Graph} starting from each root.
	 * <p>
	 * The iteration is performed in a single thread. The given {@code action} is called for each vertex in the graph, when all its descendants in the search tree have been iterated through or when it is a leaf in the tree.
	 *
	 * @param graph the graph to iterate through.
	 * @param action      the action to perform on each vertex.
	 * @param stopCondition optional condition that, when supplies true, stops the iteration before the next root is processed.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > void callDepthFirst(
			@Nonnull Graph< V, E > graph, @Nonnull Consumer< V > action,
			@Nullable BooleanSupplier stopCondition )
	{
		DepthFirstSearch< V, E > search = new DepthFirstSearch<>( graph, GraphSearch.SearchDirection.DIRECTED );
		search.setTraversalListener( new SearchListener< V, E, DepthFirstSearch< V, E > >()
		{
			@Override
			public void processVertexLate( V vertex, DepthFirstSearch< V, E > search )
			{
				action.accept(vertex);
			}

			@Override
			public void processVertexEarly( V vertex, DepthFirstSearch< V, E > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
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

	// TODO: remove after https://github.com/mastodon-sc/mastodon/pull/274 is merged
	/**
	 * Gets the minimum timepoint in the given {@link Model} at which at least one {@link Spot} exists in the Model.
	 * @param model the {@link Model}
	 * @return the timepoint
	 */
	public static int getMinTimepoint( final Model model )
	{
		PoolCollectionWrapper< Spot > spots = model.getGraph().vertices();
		int minTimepoint = Integer.MAX_VALUE;
		for ( Spot spot : spots )
			if ( spot.getTimepoint() < minTimepoint )
				minTimepoint = spot.getTimepoint();

		return minTimepoint;
	}

	// TODO: remove after https://github.com/mastodon-sc/mastodon/pull/274 is merged
	/**
	 * Gets the maximum timepoint in the given {@link Model} at which at least one {@link Spot} exists in the Model.
	 * @param model the {@link Model}
	 * @return the timepoint
	 */
	public static int getMaxTimepoint( final Model model )
	{
		PoolCollectionWrapper< Spot > spots = model.getGraph().vertices();
		int maxTimepoint = Integer.MIN_VALUE;
		for ( Spot spot : spots )
			if ( spot.getTimepoint() > maxTimepoint )
				maxTimepoint = spot.getTimepoint();

		return maxTimepoint;
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
		int minTimepoint = getMinTimepoint( model );
		int maxTimepoint = getMaxTimepoint( model );
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
			if ( model.getSpatioTemporalIndex().getSpatialIndex( timepoint ).size() >= numberOfSpots )
				return timepoint;
		throw new NoSuchElementException(
				"No time point with at least " + numberOfSpots + " spots in the range [minTimepoint=" + minTimepoint + ", maxTimepoint="
						+ maxTimepoint + "]." );
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
}
