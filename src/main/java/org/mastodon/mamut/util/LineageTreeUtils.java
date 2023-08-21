package org.mastodon.mamut.util;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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
}
