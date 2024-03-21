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
package org.mastodon.mamut.feature.spot.movement.relative;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.mamut.feature.AbstractSerialFeatureComputer;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementFeatureSettings;
import org.mastodon.mamut.feature.spot.SpotFeatureUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;
import org.scijava.app.StatusService;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Computes {@link SpotRelativeMovementFeature}
 */
public class SpotRelativeMovementFeatureComputer extends AbstractSerialFeatureComputer< Spot >
{

	private SpotRelativeMovementFeature feature;

	private RelativeMovementFeatureSettings settings;

	private final StatusService statusService;

	private final DoublePropertyMap< Spot > movementCacheX;

	private final DoublePropertyMap< Spot > movementCacheY;

	private final DoublePropertyMap< Spot > movementCacheZ;

	public SpotRelativeMovementFeatureComputer( final Model model, final Context context )
	{
		this.model = model;
		this.statusService = context.getService( StatusService.class );
		this.movementCacheX = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		this.movementCacheY = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		this.movementCacheZ = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
	}

	@Override
	public void createOutput()
	{
		if ( feature == null )
			feature = initFeature();
	}

	private SpotRelativeMovementFeature initFeature()
	{

		Predicate< SpotRelativeMovementFeature > predicate = relativeMovementFeature -> {
			if ( settings == null )
				return relativeMovementFeature != null;
			return relativeMovementFeature != null && relativeMovementFeature.settings.equals( settings );
		};
		// Try to find an existing feature with the given settings.
		SpotRelativeMovementFeature movementFeature =
				FeatureUtils.getFeature( model, predicate, SpotRelativeMovementFeature.SpotRelativeMovementFeatureSpec.class );
		if ( movementFeature != null )
			return movementFeature;

		final DoublePropertyMap< Spot > x = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< Spot > y = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< Spot > z = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
		final DoublePropertyMap< Spot > norm = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );

		return new SpotRelativeMovementFeature( x, y, z, norm, model.getSpaceUnits(), this.settings );
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
		Function< Spot, double[] > movementProvider = s -> {
			if ( movementCacheX.get( s ) == null || movementCacheY.get( s ) == null || movementCacheZ.get( s ) == null )
				return null;
			return new double[] { movementCacheX.get( s ), movementCacheY.get( s ), movementCacheZ.get( s ) };
		};
		double[] relativeMovement = SpotFeatureUtils.relativeMovement( spot, settings.numberOfNeighbors, model, movementProvider );
		if ( relativeMovement == null )
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

	/**
	 * Computes the feature for the given settings.
	 * @param settings the settings
	 */
	public void computeFeature( final RelativeMovementFeatureSettings settings )
	{
		this.forceComputeAll = new AtomicBoolean( true );
		this.settings = settings;
		createOutput();
		updateMovementCache();
		run();
		model.getFeatureModel().declareFeature( feature );
	}

	private void updateMovementCache()
	{
		movementCacheX.beforeClearPool();
		for ( Spot spot : model.getGraph().vertices() )
		{
			double[] movement = SpotFeatureUtils.spotMovement( spot );
			if ( movement == null )
				continue;
			movementCacheX.set( spot, movement[ 0 ] );
			movementCacheY.set( spot, movement[ 1 ] );
			movementCacheZ.set( spot, movement[ 2 ] );
		}
	}

	public SpotRelativeMovementFeature getFeature()
	{
		return feature;
	}
}
