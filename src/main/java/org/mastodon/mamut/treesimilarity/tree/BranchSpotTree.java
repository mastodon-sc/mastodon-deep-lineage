package org.mastodon.mamut.treesimilarity.tree;

import org.mastodon.graph.algorithm.traversal.BreadthFirstIterator;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

import java.util.HashSet;
import java.util.Set;

public class BranchSpotTree implements Tree< Integer >
{
	private final BranchSpot branchSpot;

	private final ModelBranchGraph modelBranchGraph;

	private final int endTimepoint;

	public BranchSpotTree( final BranchSpot branchSpot, final ModelBranchGraph modelBranchGraph, final int endTimepoint )
	{
		this.branchSpot = branchSpot;
		this.modelBranchGraph = modelBranchGraph;
		this.endTimepoint = endTimepoint;
	}

	@Override
	public Set< Tree< Integer > > getChildren()
	{

		HashSet< Tree< Integer > > children = new HashSet<>();

		// Create a breadth first iterator to traverse the model branch graph
		BreadthFirstIterator< BranchSpot, BranchLink > breadthFirstIterator =
				new BreadthFirstIterator<>( branchSpot, modelBranchGraph );

		// Traverse downwards (breadth first) from the selected branchSpot in the model branch graph and add the branch spots
		breadthFirstIterator.forEachRemaining( subBranchSpot -> {
			if ( subBranchSpot.equals( branchSpot ) )
				return;
			if ( !subBranchSpot.incomingEdges().get( 0 ).getSource().equals( this.branchSpot ) )
				return;
			BranchSpot childCopy = modelBranchGraph.vertexRef();
			childCopy.refTo( subBranchSpot );
			if ( childCopy.getFirstTimePoint() <= this.endTimepoint )
				children.add( new BranchSpotTree( childCopy, this.modelBranchGraph, this.endTimepoint ) );
		} );
		/*
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
		{
			BranchSpot child = branchLink.getTarget();
			if ( child.getFirstTimePoint() <= this.endTimepoint )
				children.add( new BranchSpotTree( child, this.modelBranchGraph, this.endTimepoint ) );
		}
		*/
		return children;
	}

	@Override
	public Integer getAttribute()
	{
		int lifespan;
		if ( branchSpot.getTimepoint() > this.endTimepoint )
			lifespan = this.endTimepoint - branchSpot.getFirstTimePoint();
		else
			lifespan = branchSpot.getTimepoint() - branchSpot.getFirstTimePoint();
		return lifespan;
	}
}
