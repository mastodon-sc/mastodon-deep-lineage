package org.mastodon.mamut.feature.branch;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.scijava.plugin.Parameter;

import javax.annotation.Nonnull;

public abstract class IntPropertyFeatureComputer< T extends IntPropertyFeature > implements MamutFeatureComputer
{
	@Parameter
	protected ModelBranchGraph branchGraph;

	@Override
	public void run()
	{
		computeSubTree();
	}

	private void computeSubTree()
	{
		DepthFirstSearch< BranchSpot, BranchLink > search = new DepthFirstSearch<>( branchGraph, GraphSearch.SearchDirection.DIRECTED );
		search.setTraversalListener( new SearchListener< BranchSpot, BranchLink, DepthFirstSearch< BranchSpot, BranchLink > >()
		{
			@Override
			public void processVertexLate( BranchSpot vertex, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				computeIntProperty( vertex );
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
		roots.forEach( search::start );
	}

	protected abstract void computeIntProperty( @Nonnull BranchSpot vertex );
}
