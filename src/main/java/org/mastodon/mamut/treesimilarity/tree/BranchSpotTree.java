package org.mastodon.mamut.treesimilarity.tree;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.HashSet;
import java.util.Set;

public class BranchSpotTree implements Tree< Integer >
{
	private final BranchSpot branchSpot;

	private final int endTimepoint;

	public BranchSpotTree( final BranchSpot branchSpot, final int endTimepoint )
	{
		this.branchSpot = branchSpot;
		this.endTimepoint = endTimepoint;
	}

	@Override
	public Set< Tree< Integer > > getChildren()
	{
		HashSet< Tree< Integer > > children = new HashSet<>();
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
		{
			BranchSpot child = branchLink.getTarget();
			if ( child.getTimepoint() < this.endTimepoint )
				children.add( new BranchSpotTree( branchLink.getTarget(), this.endTimepoint ) );
		}
		return children;
	}

	@Override
	public Integer getAttribute()
	{
		int lifespan = branchSpot.getTimepoint() - branchSpot.getFirstTimePoint();
		return lifespan;
	}
}
