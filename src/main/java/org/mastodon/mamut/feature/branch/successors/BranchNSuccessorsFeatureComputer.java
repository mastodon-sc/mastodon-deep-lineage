/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.feature.branch.successors;

import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * Computes {@link BranchNSuccessorsFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchNSuccessorsFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{
	@Parameter
	protected ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchNSuccessorsFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchNSuccessorsFeature( new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		LineageTreeUtils.callDepthFirst( branchGraph, this::computeSuccessors, this::isCanceled );
	}

	private void computeSuccessors( @Nonnull BranchSpot vertex )
	{
		boolean isLeaf = vertex.outgoingEdges().isEmpty();
		if ( isLeaf )
			output.map.set( vertex, 0 );
		else
		{
			BranchSpot ref = branchGraph.vertexRef();
			int n = 0;
			for ( BranchLink link : vertex.outgoingEdges() )
			{
				BranchSpot child = link.getTarget( ref );
				n += 1 + output.map.get( child );
			}
			output.map.set( vertex, n );
			branchGraph.releaseRef( ref );
		}
	}
}
