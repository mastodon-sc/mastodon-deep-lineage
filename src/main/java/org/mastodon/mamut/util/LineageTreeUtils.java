package org.mastodon.mamut.util;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class LineageTreeUtils {

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
			public void processVertexLate(BranchSpot vertex, DepthFirstSearch<BranchSpot, BranchLink> search) {
				action.accept(vertex);
			}

			@Override
			public void processVertexEarly( BranchSpot vertex, DepthFirstSearch< BranchSpot, BranchLink > search )
			{}

			@Override
			public void processEdge( BranchLink edge, BranchSpot from, BranchSpot to, DepthFirstSearch< BranchSpot, BranchLink > search )
			{}

			@Override
			public void crossComponent( BranchSpot from, BranchSpot to, DepthFirstSearch< BranchSpot, BranchLink > search )
			{}
		} );
		final RefSet< BranchSpot > roots = RootFinder.getRoots( branchGraph );
		for ( BranchSpot root : roots )
		{
			if ( stopCondition != null && stopCondition.getAsBoolean() )
				break;
			search.start( root );
		}
	}
}
