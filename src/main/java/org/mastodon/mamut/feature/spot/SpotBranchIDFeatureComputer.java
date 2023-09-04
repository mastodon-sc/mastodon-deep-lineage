/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature.spot;

import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = MamutFeatureComputer.class)
@SuppressWarnings({ "UnusedDeclaration" })
public class SpotBranchIDFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{

	@Parameter
	@SuppressWarnings({ "UnusedDeclaration" })
	private ModelGraph graph;

	@Parameter
	@SuppressWarnings({ "UnusedDeclaration" })
	private ModelBranchGraph branchGraph;

	@Parameter(type = ItemIO.OUTPUT)
	private SpotBranchIDFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotBranchIDFeature( new IntPropertyMap<>( graph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		output.map.beforeClearPool();

		if ( graph.vertices().isEmpty() )
			return;
		if ( branchGraph.vertices().isEmpty() )
			return;

		BranchSpot ref = branchGraph.vertexRef();
		for ( Spot spot : graph.vertices() )
		{
			if ( isCanceled() )
				return;
			final BranchSpot branchSpot = branchGraph.getBranchVertex( spot, ref );
			if ( branchSpot != null )
				output.map.set( spot, branchSpot.getInternalPoolIndex() );
		}
		branchGraph.releaseRef( ref );
	}
}
