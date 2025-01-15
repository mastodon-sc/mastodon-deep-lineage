/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature.branch.timepoints;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Computes the {@link org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchTimepointsFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private Model model;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchTimepointsFeature output;

	@Override
	public void createOutput()
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		if ( null == output )
			output = new BranchTimepointsFeature(
					new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ),
					new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ),
					model.getSpaceUnits() );
	}

	@Override
	public void run()
	{
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		for ( final BranchSpot branchSpot : branchGraph.vertices() )
			runForBranchSpot( branchSpot );

	}

	private void runForBranchSpot( BranchSpot branchSpot )
	{
		output.startTimepointsMap.set( branchSpot, branchSpot.getFirstTimePoint() );
		output.endTimepointsMap.set( branchSpot, branchSpot.getTimepoint() );
	}
}
