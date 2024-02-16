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
package org.mastodon.mamut.feature.branch.movement;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.feature.AbstractSerialFeatureComputer;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Collection;

/**
 * Computes {@link BranchAverageMovementFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchAverageMovementFeatureComputer extends AbstractSerialFeatureComputer< BranchSpot >
{

	@Parameter( type = ItemIO.OUTPUT )
	protected BranchAverageMovementFeature output;

	@Override
	protected void compute( final BranchSpot branchSpot )
	{
		output.averageMovement.set( branchSpot, averageMovement( branchSpot ) );
	}

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchAverageMovementFeature(
					new DoublePropertyMap<>( model.getBranchGraph().vertices().getRefPool(), Double.NaN ), model.getSpaceUnits() );
	}

	private double averageMovement( final BranchSpot branchSpot )
	{
		int duration = BranchSpotFeatureUtils.branchDuration( branchSpot ) - 1;
		return BranchSpotFeatureUtils.cumulatedDistance( model, branchSpot ) / duration;
	}

	@Override
	protected void reset()
	{
		output.averageMovement.beforeClearPool();
	}

	@Override
	protected ValueIsSetEvaluator< BranchSpot > getEvaluator()
	{
		return output;
	}

	@Override
	protected Collection< BranchSpot > getVertices()
	{
		return model.getBranchGraph().vertices();
	}
}
