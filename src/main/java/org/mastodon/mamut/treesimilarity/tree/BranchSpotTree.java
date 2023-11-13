/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 N/A
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

	public BranchSpotTree( final BranchSpot branchSpot, final int endTimepoint )
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
			if ( branchSpot.equals( child ) )
				continue;
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
