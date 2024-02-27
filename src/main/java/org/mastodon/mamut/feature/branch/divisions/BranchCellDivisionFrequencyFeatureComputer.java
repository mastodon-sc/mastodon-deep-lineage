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
package org.mastodon.mamut.feature.branch.divisions;

import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.mamut.feature.AbstractCancelableFeatureComputer;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * Computes {@link BranchCellDivisionFrequencyFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchCellDivisionFrequencyFeatureComputer extends AbstractCancelableFeatureComputer
{

	@Parameter
	protected ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	protected BranchCellDivisionFrequencyFeature output;

	private RefObjectMap< BranchSpot, Pair< Integer, Integer > > valueCache;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchCellDivisionFrequencyFeature( new DoublePropertyMap<>( branchGraph.vertices().getRefPool(), Double.NaN ) );
	}

	@Override
	public void run()
	{
		valueCache = new RefObjectHashMap<>( branchGraph.vertices().getRefPool(), 0 );
		LineageTreeUtils.callDepthFirst( branchGraph, this::computeCellDivisionFrequency, this::isCanceled );
	}

	private void computeCellDivisionFrequency( @Nonnull BranchSpot branchSpot )
	{
		boolean isLeaf = branchSpot.outgoingEdges().isEmpty();

		int duration = BranchSpotFeatureUtils.branchDuration( branchSpot );
		if ( isLeaf )
		{
			valueCache.put( branchSpot, new ValuePair<>( 0, duration ) );
			output.frequency.set( branchSpot, 0 );
		}
		else
		{
			BranchSpot ref = branchGraph.vertexRef();
			int totalSubsequentDivsions = 0;
			int totalDuration = 0;
			for ( BranchLink link : branchSpot.outgoingEdges() )
			{
				BranchSpot child = link.getTarget( ref );
				Pair< Integer, Integer > childValue = valueCache.get( child );
				totalSubsequentDivsions += childValue.getA();
				totalDuration += childValue.getB();
			}
			totalSubsequentDivsions += 1;
			totalDuration += duration;
			valueCache.put( branchSpot, new ValuePair<>( totalSubsequentDivsions, totalDuration ) );
			output.frequency.set( branchSpot, ( double ) totalSubsequentDivsions / totalDuration );
			branchGraph.releaseRef( ref );
		}
	}
}
