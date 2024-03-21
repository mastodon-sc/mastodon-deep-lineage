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
package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.mamut.feature.branch.movement.relative.BranchRelativeMovementFeatureComputer;
import org.mastodon.mamut.feature.spot.movement.relative.SpotRelativeMovementFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * A controller to compute relative movement of spots and branches in relation to nearest neighbors.
 */
public class RelativeMovementController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	/**
	 * Creates a new relative movement controller.
	 * @param model the model to operate on.
	 */
	public RelativeMovementController( final Model model )
	{
		this.model = model;
	}

	/**
	 * Computes relative movement of spots and branches in relation to nearest neighbors.
	 * <p>
	 * Adds new features on spot and branch level to the model, except features with the same settings already exist.
	 *
	 * @param settings the settings to use for computing the relative movement.
	 * @param context the scijava context to operate in.
	 */
	public void computeRelativeMovement( final RelativeMovementFeatureSettings settings,
			final Context context )
	{
		logger.debug( "Computing relative movement on spot level using these settings: {}", settings );
		SpotRelativeMovementFeatureComputer spotFeatureComputer = new SpotRelativeMovementFeatureComputer( model, context );
		spotFeatureComputer.computeFeature( settings );
		logger.debug( "Computing relative movement on branch spot level using these settings: {}", settings );
		BranchRelativeMovementFeatureComputer branchFeatureComputer =
				new BranchRelativeMovementFeatureComputer( model, context, spotFeatureComputer.getFeature() );
		branchFeatureComputer.computeFeature();
		logger.debug( "Successfully computed relative movement." );
	}
}
