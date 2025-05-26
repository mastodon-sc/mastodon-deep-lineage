package org.mastodon.mamut.detection;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Cast;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.util.LabelImageUtils;
import org.mastodon.tracking.detection.DetectionUtil;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.detection.AbstractSpotDetectorOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

public abstract class AbstractDetector extends AbstractSpotDetectorOp
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void compute( final List< SourceAndConverter< ? > > sources, final ModelGraph graph )
	{
		ok = false;

		// Clear the status display.
		statusService.clearStatus();

		// Read and validate settings.
		final StringBuilder errorHolder = new StringBuilder();
		if ( !validateSettings( errorHolder ) )
		{
			errorMessage = errorHolder.toString();
			return;
		}

		// Extract settings.
		final int minTimepoint = ( int ) settings.get( DetectorKeys.KEY_MIN_TIMEPOINT );
		final int maxTimepoint = ( int ) settings.get( DetectorKeys.KEY_MAX_TIMEPOINT );
		final int setup = ( int ) settings.get( DetectorKeys.KEY_SETUP_ID );

		if ( setup < 0 || setup >= sources.size() )
		{
			errorMessage = "The parameter " + DetectorKeys.KEY_SETUP_ID + " is not in the range of available sources ("
					+ sources.size() + "): " + setup;
			return;
		}
		if ( maxTimepoint < minTimepoint )
		{
			errorMessage = "Min time-point should be smaller than or equal to max time-point, but was min = "
					+ minTimepoint + " and max = " + maxTimepoint;
			return;
		}
		// Now we are sure the settings are valid.

		// Perform detection.
		statusService.showStatus( "Detecting spots..." );
		try
		{
			for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
			{
				// We use the `statusService to show progress.
				statusService.showProgress( timepoint - minTimepoint + 1, maxTimepoint - minTimepoint + 1 );

				if ( isCanceled() )
					break; // Exit but don't fail.

				if ( !DetectionUtil.isPresent( sources, setup, timepoint ) )
					continue;

				/*
				 * Now here is the part specific to our dummy detector. First we get
				 * the source for the current channel (or setup) at the desired
				 * time-point. In BDV jargon, this is a source.
				 */
				final Source< ? > source = sources.get( setup ).getSpimSource();
				double[] voxelDimensions = source.getVoxelDimensions().dimensionsAsDoubleArray();

				/*
				 * This source possibly has several resolution levels. And for your
				 * own real detector, it might be very interesting to work on a
				 * lower resolution (higher level). Check the DogDetectorOp code, for
				 * instance. For us, we don't even care for pixels, we just want to
				 * have the image boundary from the highest resolution (level 0).
				 */
				final int level = 0;

				/*
				 * This is the 3D image of the current time-point, specified
				 * channel. It is always 3D. If the source is 2D, the 3rd dimension
				 * will have a size of 1.
				 */
				final RandomAccessibleInterval< ? > image = source.getSource( timepoint, level );

				final Img< ? > segmentation = performSegmentation( image, voxelDimensions );

				final AffineTransform3D transform = DetectionUtil.getTransform( sources, timepoint, setup, level );
				LabelImageUtils.createSpotsForFrame( graph, Cast.unchecked( segmentation ), timepoint, transform, 1d );
			}
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = getDetectorName() + " failed: " + e.getMessage();
			logger.error( "{} failed: {}", getDetectorName(), e.getMessage() );
			return;
		}

		/*
		 * We are done! Gracefully exit, stating we are ok.
		 */
		ok = true;
	}

	protected double getAnisotropy( double[] voxelSizes )
	{
		if ( voxelSizes == null || voxelSizes.length == 0 )
		{
			throw new IllegalArgumentException( "Array must not be empty" );
		}

		double highestValue = voxelSizes[ 0 ];
		double lowestValue = voxelSizes[ 0 ];

		for ( double value : voxelSizes )
		{
			if ( value > highestValue )
				highestValue = value;
			if ( value < lowestValue )
				lowestValue = value;
		}

		if ( lowestValue == 0 )
			throw new ArithmeticException( "Voxel size of zero detected. This is not allowed." );

		return highestValue / lowestValue;
	}

	protected boolean is3D( final RandomAccessibleInterval< ? > image )
	{
		long[] dimensions = image.dimensionsAsLongArray();
		if ( dimensions.length <= 2 )
			return false;
		else
		{
			int nonPlaneDimensionCount = 0;
			for ( final long dimension : dimensions )
			{
				if ( dimension > 1 )
					nonPlaneDimensionCount++;
			}
			return nonPlaneDimensionCount > 2;
		}
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > defaultSettings = new HashMap<>();
		defaultSettings.put( DetectorKeys.KEY_SETUP_ID, DetectorKeys.DEFAULT_SETUP_ID );
		defaultSettings.put( DetectorKeys.KEY_MIN_TIMEPOINT, DetectorKeys.DEFAULT_MIN_TIMEPOINT );
		defaultSettings.put( DetectorKeys.KEY_MAX_TIMEPOINT, DetectorKeys.DEFAULT_MAX_TIMEPOINT );
		addSpecificDefaultSettings( defaultSettings );
		return defaultSettings;
	}

	protected abstract boolean validateSettings( final StringBuilder errorHolder );

	protected abstract Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions );

	protected abstract void addSpecificDefaultSettings( final Map< String, Object > defaultSettings );

	protected abstract String getDetectorName();
}
