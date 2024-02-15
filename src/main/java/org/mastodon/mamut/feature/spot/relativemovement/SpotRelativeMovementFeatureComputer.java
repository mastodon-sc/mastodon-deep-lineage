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
package org.mastodon.mamut.feature.spot.relativemovement;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.feature.AbstractSerialFeatureComputer;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.spot.SpotFeatureUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;
import org.scijava.app.StatusService;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Computes {@link SpotRelativeMovementFeature}
 */
public class SpotRelativeMovementFeatureComputer extends AbstractSerialFeatureComputer< Spot >
{

	private SpotRelativeMovementFeature feature;

	private SpotRelativeMovementFeatureSettings settings;

	private final StatusService statusService;

	public SpotRelativeMovementFeatureComputer( final Model model, final Context context )
	{
		this.model = model;
		this.statusService = context.getService( StatusService.class );
	}

	@Override
	public void createOutput()
	{
		if ( feature == null )
			feature = initFeature();
	}

	private SpotRelativeMovementFeature initFeature()
	{
		// Try to get output from the FeatureModel, if we deserialized a model.
		final Feature< ? > existingFeature = model.getFeatureModel().getFeature( SpotRelativeMovementFeature.SPEC );
		if ( existingFeature != null )
			return ( SpotRelativeMovementFeature ) existingFeature;

		final DoublePropertyMap< Spot > x = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< Spot > y = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< Spot > z = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< Spot > norm = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );

		return new SpotRelativeMovementFeature( x, y, z, norm, model.getSpaceUnits(), new SpotRelativeMovementFeatureSettings() );
	}

	@Override
	protected void compute( final Spot spot )
	{
		if ( spot.incomingEdges().isEmpty() )
		{
			feature.x.set( spot, Double.NaN );
			feature.y.set( spot, Double.NaN );
			feature.z.set( spot, Double.NaN );
			feature.norm.set( spot, Double.NaN );
			return;
		}
		double[] relativeMovement = SpotFeatureUtils.relativeMovement( spot, settings.numberOfNeighbors, model );
		if ( relativeMovement.length == 0 )
		{
			feature.x.set( spot, Double.NaN );
			feature.y.set( spot, Double.NaN );
			feature.z.set( spot, Double.NaN );
			feature.norm.set( spot, Double.NaN );
			return;
		}
		feature.x.set( spot, relativeMovement[ 0 ] );
		feature.y.set( spot, relativeMovement[ 1 ] );
		feature.z.set( spot, relativeMovement[ 2 ] );
		feature.norm.set( spot, LinAlgHelpers.length( relativeMovement ) );
	}

	@Override
	protected ValueIsSetEvaluator< Spot > getEvaluator()
	{
		return feature;
	}

	@Override
	protected Collection< Spot > getVertices()
	{
		return model.getGraph().vertices();
	}

	@Override
	protected void reset()
	{
		feature.x.beforeClearPool();
		feature.y.beforeClearPool();
		feature.z.beforeClearPool();
		feature.norm.beforeClearPool();
	}

	@Override
	protected void notifyProgress( final int finished, final int total )
	{
		statusService.showStatus( finished, total, "Computing SpotRelativeMovementFeature" );
	}

	public void computeFeature( final boolean forceComputeAll, final SpotRelativeMovementFeatureSettings settings )
	{
		this.forceComputeAll = new AtomicBoolean( forceComputeAll );
		this.settings = settings;
		createOutput();
		run();
		model.getFeatureModel().declareFeature( feature );
	}
}