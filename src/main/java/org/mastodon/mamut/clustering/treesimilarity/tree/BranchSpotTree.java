/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.clustering.treesimilarity.tree;

import org.mastodon.mamut.clustering.config.HasName;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A tree data structure representing a branch spot and its children.
 */
public class BranchSpotTree implements Tree< Double >, HasName
{
	private final BranchSpot branchSpot;

	private final Collection< Tree< Double > > children;

	private final LabelSupplier labelSupplier;

	private final Double attribute;

	private final int startTimepoint;

	private final int endTimepoint;

	private final Model model;

	BranchSpotTree( final BranchSpot branchSpot, final int startTimepoint, final int endTimepoint )
	{
		this( branchSpot, startTimepoint, endTimepoint, null );
	}

	/**
	 * Create a new tree with the given branchSpot and endTimepoint.
	 *
	 * @param branchSpot the branchSpot of the tree.
	 * @param endTimepoint the end time point of the tree, i.e. the time point at which the tree is cut off.
	 */
	public BranchSpotTree( final BranchSpot branchSpot, final int startTimepoint, final int endTimepoint, final Model model )
	{
		if ( branchSpot == null )
			throw new IllegalArgumentException( "The given branchSpot is null." );
		if ( branchSpot.getFirstTimePoint() > endTimepoint )
			throw new IllegalArgumentException( "The first timepoint of the given branchSpot " + branchSpot.getFirstTimePoint()
					+ " is greater than the given endTimepoint (" + endTimepoint + "). This is not allowed." );
		if ( branchSpot.getTimepoint() < startTimepoint )
			throw new IllegalArgumentException( "The end timepoint of the given branchSpot (" + branchSpot.getTimepoint()
					+ ") is less than the startTimepoint of this BranchSpotTree (" + startTimepoint + "). This is not allowed." );
		this.branchSpot = branchSpot;
		this.children = new ArrayList<>();
		this.labelSupplier = new LabelSupplier();
		this.attribute = ( double ) BranchSpotFeatureUtils.branchDuration( branchSpot, startTimepoint, endTimepoint );
		this.startTimepoint = startTimepoint;
		this.endTimepoint = endTimepoint;
		this.model = model;
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
		{
			BranchSpot child = branchLink.getTarget();
			if ( branchSpot.equals( child ) )
				continue;
			if ( child.getFirstTimePoint() <= endTimepoint )
				this.children.add( new BranchSpotTree( child, startTimepoint, endTimepoint, model ) );
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
		return attribute;
	}

	public BranchSpot getBranchSpot()
	{
		return branchSpot;
	}

	public int getStartTimepoint()
	{
		return startTimepoint;
	}

	public int getEndTimepoint()
	{
		return endTimepoint;
	}

	public Model getModel()
	{
		return model;
	}

	/**
	 * Retrieves the root spot of the branch graph that corresponds to the
	 * specified starting timepoint. Navigates through the spots in the branch graph
	 * using a vertex branch iterator, and identifies the spot associated with the start
	 * timepoint. The iterator is released after iteration is complete.
	 *
	 * @return the root spot corresponding to the start timepoint, or {@code null} if no such
	 *         spot is found.
	 */
	public Spot getRootSpot()
	{
		Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( branchSpot );
		Spot spot = null;
		while ( spotIterator.hasNext() )
		{
			spot = spotIterator.next();
			if ( spot.getTimepoint() == startTimepoint )
				break;
		}
		model.getBranchGraph().releaseIterator( spotIterator );
		return spot;
	}

	public void updateLabeling( final boolean includeName, final boolean includeTag, final TagSetStructure.TagSet tagSet )
	{
		labelSupplier.setParams( includeName, includeTag, tagSet );
	}

	@Override
	public String toString()
	{
		return labelSupplier.get();
	}

	public String getTagLabel()
	{
		return labelSupplier.getTagLabel();
	}

	/**
	 * Gets the first the label for the branch spot associated with the BranchSpotTree
	 * @return the first label of the branch spot
	 */
	public String getName()
	{
		return branchSpot.getFirstLabel();
	}

	public class LabelSupplier implements Supplier< String >
	{
		private boolean includeName = true;

		private boolean includeTag = true;

		private TagSetStructure.TagSet tagSet;

		public void setParams( final boolean includeName, final boolean includeTag, final TagSetStructure.TagSet tagSet )
		{
			this.includeName = includeName;
			this.includeTag = includeTag;
			this.tagSet = tagSet;
		}

		private String getTagLabel()
		{
			Spot ref = model.getGraph().vertexRef();
			String tagLabel;
			tagLabel = TagSetUtils.getTagLabel( model, branchSpot, tagSet, ref );
			model.getGraph().releaseRef( ref );
			if ( tagLabel == null )
				tagLabel = "";
			return tagLabel;
		}

		@Override
		public String get()
		{
			String tagLabel = getTagLabel();
			if ( includeName && includeTag )
			{
				if ( tagLabel.isEmpty() )
					return branchSpot.getFirstLabel();
				else
					return branchSpot.getFirstLabel() + " " + tagLabel;
			}
			else if ( includeName )
				return branchSpot.getFirstLabel();
			else if ( includeTag )
				return tagLabel;
			else
				return "";
		}
	}
}
