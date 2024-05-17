/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.classification.treesimilarity.tree;

import org.mastodon.mamut.classification.util.ClassificationUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.model.tag.TagSetStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * A tree data structure representing a branch spot and its children.
 */
public class BranchSpotTree implements Tree< Double >
{
	private final BranchSpot branchSpot;

	private final Collection< Tree< Double > > children;

	private final LabelSupplier labelSupplier;

	private final Double attribute;

	BranchSpotTree( final BranchSpot branchSpot, final int endTimepoint )
	{
		this( branchSpot, endTimepoint, null );
	}

	/**
	 * Create a new tree with the given branchSpot and endTimepoint.
	 *
	 * @param branchSpot the branchSpot of the tree.
	 * @param endTimepoint the end time point of the tree, i.e. the time point at which the tree is cut off.
	 */
	public BranchSpotTree( final BranchSpot branchSpot, final int endTimepoint, final Model model )
	{
		if ( branchSpot == null )
			throw new IllegalArgumentException( "The given branchSpot is null." );
		if ( branchSpot.getFirstTimePoint() > endTimepoint )
			throw new IllegalArgumentException( "The first timepoint of the given branchSpot " + branchSpot.getFirstTimePoint()
					+ " is greater than the endTimepoint (" + endTimepoint + ")." );
		this.branchSpot = branchSpot;
		this.children = new ArrayList<>();
		this.labelSupplier = new LabelSupplier( model );
		this.attribute = ( double ) BranchSpotFeatureUtils.branchDuration( branchSpot, endTimepoint );
		for ( BranchLink branchLink : branchSpot.outgoingEdges() )
		{
			BranchSpot child = branchLink.getTarget();
			if ( branchSpot.equals( child ) )
				continue;
			if ( child.getFirstTimePoint() <= endTimepoint )
				this.children.add( new BranchSpotTree( child, endTimepoint, model ) );
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

	public void updateLabeling( final boolean includeName, final boolean includeTag, final TagSetStructure.TagSet tagSet )
	{
		labelSupplier.setParams( includeName, includeTag, tagSet );
	}

	@Override
	public String toString()
	{
		return labelSupplier.get();
	}

	public class LabelSupplier implements Supplier< String >
	{
		private boolean includeName = true;

		private boolean includeTag = true;

		private TagSetStructure.TagSet tagSet;

		private final Model model;

		public LabelSupplier( final Model model )
		{
			this.model = model;
		}

		public void setParams( final boolean includeName, final boolean includeTag, final TagSetStructure.TagSet tagSet )
		{
			this.includeName = includeName;
			this.includeTag = includeTag;
			this.tagSet = tagSet;
		}

		@Override
		public String get()
		{
			Spot ref = model.getGraph().vertexRef();
			String tagLabel = ClassificationUtils.getTagLabel( model, branchSpot, tagSet, ref );
			model.getGraph().releaseRef( ref );
			if ( tagLabel == null )
				tagLabel = "";
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
