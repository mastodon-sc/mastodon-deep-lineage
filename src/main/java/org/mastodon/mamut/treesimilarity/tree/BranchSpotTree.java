package org.mastodon.mamut.treesimilarity.tree;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.HashSet;
import java.util.Set;

public class BranchSpotTree implements Tree< Double >
{
	private final BranchSpot branchSpot;

	private final int endTimepoint;

	private final HashSet< Tree< Double > > children;

	public BranchSpotTree( final BranchSpot branchSpot, final int endTimepoint )
	{
		this.branchSpot = branchSpot;
		this.endTimepoint = endTimepoint;
		this.children = new HashSet<>();
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
		{
			BranchSpot child = branchLink.getTarget();
			if ( child.getFirstTimePoint() <= this.endTimepoint )
				this.children.add( new BranchSpotTree( child, this.endTimepoint ) );
		}
	}

	@Override
	public Set< Tree< Double > > getChildren()
	{
		return children;
	}

	@Override
	public Double getAttribute()
	{
		int lastTimePoint = Math.min( branchSpot.getTimepoint(), this.endTimepoint );
		double lifespan = ( double ) lastTimePoint - branchSpot.getFirstTimePoint();
		return lifespan;
	}

	public BranchSpot getBranchSpot()
	{
		return branchSpot;
	}
}
