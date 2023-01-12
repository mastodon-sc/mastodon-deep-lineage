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
package org.mastodon.mamut.feature;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Computes {@link SpotEllipsoidFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class SpotEllipsoidFeatureComputer implements MamutFeatureComputer, Cancelable
{

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private AtomicBoolean forceComputeAll;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotEllipsoidFeature output;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get output from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC );
			if ( null != feature )
			{
				output = ( SpotEllipsoidFeature ) feature;
				return;
			}

			final DoublePropertyMap< Spot > semiAxisA = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
			final DoublePropertyMap< Spot > semiAxisB = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
			final DoublePropertyMap< Spot > semiAxisC = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
			final DoublePropertyMap< Spot > volume = new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN );
			// Create a new output.
			output = new SpotEllipsoidFeature( semiAxisA, semiAxisB, semiAxisC, volume );
		}
	}

	@Override
	public void run()
	{
		cancelReason = null;
		final boolean recomputeAll = forceComputeAll.get();

		if ( recomputeAll )
		{
			// Clear all.
			output.semiAxisA.beforeClearPool();
			output.semiAxisB.beforeClearPool();
			output.semiAxisC.beforeClearPool();
			output.volume.beforeClearPool();
		}

		final int numTimepoints = bdvData.getNumTimepoints();

		int done = 0;

		final double[][] covarianceMatrix = new double[ 3 ][ 3 ];

		final JamaEigenvalueDecomposition eigenvalueDecomposition = new JamaEigenvalueDecomposition( 3 );

		for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
		{

			status.notifyProgress( ( double ) done++ / numTimepoints );

			final SpatialIndex< Spot > toProcess = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			for ( final Spot spot : toProcess )
			{
				if ( isCanceled() )
					break;

				// Skip if we are not forced to recompute all and if a value is already computed.
				if ( !recomputeAll && output.semiAxisA.isSet( spot ) )
					continue;

				spot.getCovariance( covarianceMatrix );
				eigenvalueDecomposition.decomposeSymmetric( covarianceMatrix );
				final double[] eigenValues = eigenvalueDecomposition.getRealEigenvalues();
				double volume = 4d / 3d * Math.PI;
				for ( int k = 0; k < eigenValues.length; k++ )
				{
					final double semiAxis = Math.sqrt( eigenValues[ k ] );
					volume *= semiAxis;
					if ( k == 0 )
						output.semiAxisA.set( spot, semiAxis );
					else if ( k == 1 )
						output.semiAxisB.set( spot, semiAxis );
					else if ( k == 2 )
						output.semiAxisC.set( spot, semiAxis );
				}
				output.volume.set( spot, volume );
			}
		}
	}

	@Override
	public boolean isCanceled()
	{
		return null != cancelReason;
	}

	@Override
	public void cancel( final String reason )
	{
		cancelReason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}
}
