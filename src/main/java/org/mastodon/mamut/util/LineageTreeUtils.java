package org.mastodon.mamut.util;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.spatial.SpatioTemporalIndex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class LineageTreeUtils {

	private LineageTreeUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Performs a depth-first iteration through the specified {@link ModelBranchGraph} starting from each root.
	 * <p>
	 * The iteration is performed in a single thread. The given {@code action} is called for each vertex in the graph, when all its descendants in the search tree have been iterated through or when it is a leaf in the tree.
	 *
	 * @param branchGraph the graph to iterate through.
	 * @param action      the action to perform on each vertex.
	 * @param stopCondition optional condition that, when supplies true, stops the iteration before the next root is processed.
	 */
	public static void callDepthFirst( @Nonnull ModelBranchGraph branchGraph, @Nonnull Consumer< BranchSpot > action,
			@Nullable BooleanSupplier stopCondition )
	{
		DepthFirstSearch<BranchSpot, BranchLink> search = new DepthFirstSearch<>(branchGraph, GraphSearch.SearchDirection.DIRECTED);
		search.setTraversalListener(new SearchListener<BranchSpot, BranchLink, DepthFirstSearch<BranchSpot, BranchLink>>() {
			@Override
			public void processVertexLate( BranchSpot vertex, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				action.accept(vertex);
			}

			@Override
			public void processVertexEarly( BranchSpot vertex, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void processEdge( BranchLink edge, BranchSpot from, BranchSpot to, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void crossComponent( BranchSpot from, BranchSpot to, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}
		} );
		final RefSet< BranchSpot > roots = RootFinder.getRoots( branchGraph );
		for ( BranchSpot root : roots )
		{
			if ( stopCondition != null && stopCondition.getAsBoolean() )
				break;
			search.start( root );
		}
	}

	/**
	 * Performs a depth-first iteration through the specified {@link ModelGraph} starting from the given spot.
	 * <p>
	 * The iteration is performed in a single thread. The given {@code action} is called for each vertex (i.e. {@link Spot})
	 * in the graph, when all its descendants in the search tree have been iterated through or when it is a leaf in the tree.
	 *
	 * @param modelGraph the graph to iterate through.
	 * @param spot       the spot to start the iteration from.
	 * @param action      the action to perform on each spot.
	 */
	public static void callDepthFirst( ModelGraph modelGraph, Spot spot, Consumer< Spot > action )
	{
		DepthFirstSearch< Spot, Link > search = new DepthFirstSearch<>( modelGraph, GraphSearch.SearchDirection.DIRECTED );
		search.setTraversalListener( new SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > >()
		{
			@Override
			public void processVertexLate( Spot vertex, DepthFirstSearch< Spot, Link > search )
			{
				action.accept( vertex );
			}

			@Override
			public void processVertexEarly( Spot vertex, DepthFirstSearch< Spot, Link > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void processEdge( Link edge, Spot from, Spot to, DepthFirstSearch< Spot, Link > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void crossComponent( Spot from, Spot to, DepthFirstSearch< Spot, Link > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}
		} );
		search.start( spot );
	}

	/**
	 * Gets the first time point that has at least the given number of spots ({@code numberOfSpots})
	 * by iterating through the given spatio-temporal index ({@code spotSpatioTemporalIndex})
	 * from the given minimum time point ({@code minTimePoint}) to the given maximum time point ({@code maxTimePoint}).
	 *
	 * @param spotSpatioTemporalIndex the index to search in
	 * @param minTimePoint the minimum time point to search in (inclusive)
	 * @param maxTimePoint the maximum time point to search in (inclusive)
	 * @param numberOfSpots the number of spots to search for
	 * @return the first time point with the given number of spots (or more)
	 * @throws NoSuchElementException if no time point with the given number of spots (or more) exists
	 */
	public static int getTimePointWithNSpots( @Nonnull SpatioTemporalIndex< Spot > spotSpatioTemporalIndex,
			int minTimePoint, int maxTimePoint, int numberOfSpots )
	{
		for ( int timePoint = minTimePoint; timePoint <= maxTimePoint; timePoint++ )
			if ( spotSpatioTemporalIndex.getSpatialIndex( timePoint ).size() >= numberOfSpots )
				return timePoint;
		throw new NoSuchElementException(
				"No time point with at least " + numberOfSpots + " spots in the range [" + minTimePoint + ", " + maxTimePoint + "]." );
	}
}
