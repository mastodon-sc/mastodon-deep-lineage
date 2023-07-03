package org.mastodon.mamut.treesimilarity.tree;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.ArrayList;
import java.util.Collection;

public class BranchSpotTree implements Tree< Double >
{
	private final BranchSpot branchSpot;

	private final int endTimepoint;

	private final Collection< Tree< Double > > children;

	public BranchSpotTree( final BranchSpot branchSpot, final int endTimepoint ) throws IllegalArgumentException
	{
		if ( branchSpot == null )
			throw new IllegalArgumentException( "The given branchSpot is null." );
		if ( branchSpot.getFirstTimePoint() > endTimepoint )
			throw new IllegalArgumentException( "The first timepoint of the given branchSpot " + branchSpot.getFirstTimePoint()
					+ " is greater than the endTimepoint (" + endTimepoint + ")." );
		this.branchSpot = branchSpot;
		this.endTimepoint = endTimepoint;
		this.children = new ArrayList<>();
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
		{
			BranchSpot child = branchLink.getTarget();
			if ( child.getFirstTimePoint() <= this.endTimepoint )
				this.children.add( new BranchSpotTree( child, this.endTimepoint ) );
		}
	}

	@Override
	public Collection< Tree< Double > > getChildren()
	{
		return children;
	}

	@Override
	public Double getAttribute()
	{
		int lastTimePoint = Math.min( branchSpot.getTimepoint(), this.endTimepoint );
		return ( double ) lastTimePoint - branchSpot.getFirstTimePoint();
	}

	public BranchSpot getBranchSpot()
	{
		return branchSpot;
	}

	@Override
	public String toString()
	{
		return branchSpot.getFirstLabel();
	}
}
