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
package org.mastodon.mamut.feature.branch.relativemovement;

import org.mastodon.mamut.feature.AbstractSerialFeatureComputer;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.feature.spot.relativemovement.SpotRelativeMovementFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;
import org.scijava.app.StatusService;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Computes {@link BranchRelativeMovementFeature}
 */
public class BranchRelativeMovementFeatureComputer extends AbstractSerialFeatureComputer< BranchSpot >
{

	private BranchRelativeMovementFeature feature;

	private final StatusService statusService;

	private final SpotRelativeMovementFeature spotFeature;

	public BranchRelativeMovementFeatureComputer( final Model model, final Context context, final SpotRelativeMovementFeature spotFeature )
	{
		this.model = model;
		this.statusService = context.getService( StatusService.class );
		this.spotFeature = spotFeature;
	}

	@Override
	public void createOutput()
	{
		if ( feature == null )
			feature = initFeature();
	}

	private BranchRelativeMovementFeature initFeature()
	{

		Predicate< BranchRelativeMovementFeature > predicate = relativeMovementFeature -> {
			if ( spotFeature.settings == null )
				return relativeMovementFeature != null;
			return relativeMovementFeature != null && relativeMovementFeature.settings.equals( spotFeature.settings );
		};
		// Try to find an existing feature with the given settings.
		BranchRelativeMovementFeature movementFeature =
				FeatureUtils.getFeature( model, predicate,
						BranchRelativeMovementFeature.BranchRelativeMovementFeatureSpec.class );
		if ( movementFeature != null )
			return movementFeature;

		final DoublePropertyMap< BranchSpot > x = new DoublePropertyMap<>( model.getBranchGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< BranchSpot > y = new DoublePropertyMap<>( model.getBranchGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< BranchSpot > z = new DoublePropertyMap<>( model.getBranchGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< BranchSpot > norm = new DoublePropertyMap<>( model.getBranchGraph().vertices().getRefPool(), Double.NaN );

		return new BranchRelativeMovementFeature( x, y, z, norm, model.getSpaceUnits(), this.spotFeature.settings );
	}

	@Override
	protected void compute( final BranchSpot branchSpot )
	{
		if ( branchSpot.incomingEdges().isEmpty() )
		{
			feature.xMap.set( branchSpot, Double.NaN );
			feature.yMap.set( branchSpot, Double.NaN );
			feature.zMap.set( branchSpot, Double.NaN );
			feature.normMap.set( branchSpot, Double.NaN );
			return;
		}
		double relativeMovement = BranchSpotFeatureUtils.relativeMovement( branchSpot, spotFeature, model );
		feature.normMap.set( branchSpot, relativeMovement );

		double[] normalizedRelativeMovementDirection =
				BranchSpotFeatureUtils.normalizedRelativeMovementDirection( branchSpot, spotFeature, model );
		if ( normalizedRelativeMovementDirection.length == 0 )
		{
			feature.xMap.set( branchSpot, Double.NaN );
			feature.yMap.set( branchSpot, Double.NaN );
			feature.zMap.set( branchSpot, Double.NaN );
			return;
		}
		feature.xMap.set( branchSpot, normalizedRelativeMovementDirection[ 0 ] );
		feature.yMap.set( branchSpot, normalizedRelativeMovementDirection[ 1 ] );
		feature.zMap.set( branchSpot, normalizedRelativeMovementDirection[ 2 ] );
	}

	@Override
	protected ValueIsSetEvaluator< BranchSpot > getEvaluator()
	{
		return feature;
	}

	@Override
	protected Collection< BranchSpot > getVertices()
	{
		return model.getBranchGraph().vertices();
	}

	@Override
	protected void reset()
	{
		feature.xMap.beforeClearPool();
		feature.yMap.beforeClearPool();
		feature.zMap.beforeClearPool();
		feature.normMap.beforeClearPool();
	}

	@Override
	protected void notifyProgress( final int finished, final int total )
	{
		statusService.showStatus( finished, total, "Computing SpotRelativeMovementFeature" );
	}

	public void computeFeature( final boolean forceComputeAll )
	{
		this.forceComputeAll = new AtomicBoolean( forceComputeAll );
		createOutput();
		run();
		model.getFeatureModel().declareFeature( feature );
	}
}
