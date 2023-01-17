package org.mastodon.mamut.feature;

import org.mastodon.feature.DefaultFeatureComputerService;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Computes {@link SpotEllipsoidFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class SpotEllipsoidAspectRatiosFeatureComputer
		implements MamutFeatureComputer, Cancelable
{
	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private AtomicBoolean forceComputeAll;

	@Parameter
	private DefaultFeatureComputerService.FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotEllipsoidAspectRatiosFeature output;

	@Parameter( type = ItemIO.INPUT )
	private SpotEllipsoidFeature input;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature(
					SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC );
			if ( null != feature )
			{
				output = ( SpotEllipsoidAspectRatiosFeature ) feature;
				return;
			}

			final DoublePropertyMap< Spot > aspectRatioAToB =
					new DoublePropertyMap<>(
							model.getGraph().vertices().getRefPool(),
							Double.NaN );
			final DoublePropertyMap< Spot > aspectRatioAToC =
					new DoublePropertyMap<>(
							model.getGraph().vertices().getRefPool(),
							Double.NaN );
			final DoublePropertyMap< Spot > aspectRatioBToC =
					new DoublePropertyMap<>(
							model.getGraph().vertices().getRefPool(),
							Double.NaN );

			// Create a new output.
			output = new SpotEllipsoidAspectRatiosFeature( aspectRatioAToB,
					aspectRatioAToC, aspectRatioBToC );
		}
		if ( null == input )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature(
					SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC );
			if ( null != feature )
			{
				input = ( SpotEllipsoidFeature ) feature;
			}
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
			output.aspectRatioAToB.beforeClearPool();
			output.aspectRatioAToC.beforeClearPool();
			output.aspectRatioBToC.beforeClearPool();
		}

		final int numTimepoints = bdvData.getNumTimepoints();

		int done = 0;

		for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
		{
			if ( isCanceled() )
				return;

			final SpatialIndex< Spot > toProcess =
					model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			for ( final Spot spot : toProcess )
			{
				if ( isCanceled() )
					return;

				if ( !recomputeAll && output.aspectRatioAToB.isSet( spot ) )
					continue;
				{
					final double a = input.semiAxisA.get( spot );
					final double b = input.semiAxisB.get( spot );
					final double c = input.semiAxisC.get( spot );
					output.aspectRatioAToB.set( spot, a / b );
					output.aspectRatioAToC.set( spot, a / c );
					output.aspectRatioBToC.set( spot, b / c );
				}
			}

			status.notifyProgress( ( double ) done++ / numTimepoints );
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
