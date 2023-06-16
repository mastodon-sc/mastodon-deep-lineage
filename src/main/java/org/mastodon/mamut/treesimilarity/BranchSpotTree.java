package org.mastodon.mamut.treesimilarity;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BranchSpotTree implements Tree< Integer >
{
	private final BranchSpot branchSpot;

	public BranchSpotTree( final BranchSpot branchSpot )
	{
		this.branchSpot = branchSpot;
	}

	@Override
	public Set< Tree< Integer > > getChildren()
	{
		HashSet< Tree< Integer > > children = new HashSet<>();
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
			children.add( new BranchSpotTree( branchLink.getTarget() ) );
		return children;
	}

	@Override
	public Integer getAttribute()
	{
		return branchSpot.getTimepoint() - branchSpot.getFirstTimePoint();
	}
}
