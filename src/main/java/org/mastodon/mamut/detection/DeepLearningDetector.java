/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.detection;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Cast;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.util.LabelImageUtils;
import org.mastodon.tracking.detection.DetectionUtil;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.detection.AbstractSpotDetectorOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

/**
 * An abstract class representing a deep learning-based detector.
 * This class provides the framework for various segmentation
 * models and requires subclass implementation for model-specific functionality.<br>
 * Subclasses are expected to define specific segmentation, settings, and validation
 * logic.<br>
 * The main workflow includes validating settings, providing a (time-point and channel) specific source,
 * and executing the detection process via image segmentation.<br>
 * The segmentation results are then used to create spots in the model graph.<br>
 */
public abstract class DeepLearningDetector extends AbstractSpotDetectorOp
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
			logger.error( "Invalid settings for {}: {}", getDetectorName(), errorMessage );
			return;
		}

		// Extract settings.
		final int minTimepoint = ( int ) settings.get( DetectorKeys.KEY_MIN_TIMEPOINT );
		final int maxTimepoint = ( int ) settings.get( DetectorKeys.KEY_MAX_TIMEPOINT );
		final int setup = ( int ) settings.get( DetectorKeys.KEY_SETUP_ID );

		logger.info( "Settings contain, minTimepoint: {}, maxTimepoint: {} and setup {}", minTimepoint, maxTimepoint, setup );

		if ( setup < 0 || setup >= sources.size() )
		{
			errorMessage = "The parameter " + DetectorKeys.KEY_SETUP_ID + " is not in the range of available sources ("
					+ sources.size() + "): " + setup;
			logger.error( "Invalid setup ID: {}", errorMessage );
			return;
		}
		if ( maxTimepoint < minTimepoint )
		{
			errorMessage = "Min time-point should be smaller than or equal to max time-point, but was min = "
					+ minTimepoint + " and max = " + maxTimepoint;
			logger.error( "Invalid time-point range: {}", errorMessage );
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

				if ( DetectionUtil.isPresent( sources, setup, timepoint ) )
				{
					// First, get the source for the current channel (or setup) at the desired time-point. In BDV jargon, this is a source.
					final Source< ? > source = sources.get( setup ).getSpimSource();

					final int level = 0; // level 0 is always the highest resolution.

					// This is the 3D image of the current time-point and specified channel. It is always 3D. If the source is 2D, the 3rd dimension has a size of 1.
					RandomAccessibleInterval< ? > image = source.getSource( timepoint, level );

					// Crop the image to the region of interest (ROI) if specified in the settings.
					final Interval roi = ( Interval ) settings.get( DetectorKeys.KEY_ROI );
					if ( roi != null )
						image = Views.interval( image, roi );

					final Img< ? > segmentation = performSegmentation( image, source.getVoxelDimensions().dimensionsAsDoubleArray() );

					if ( segmentation != null )
					{
						IntervalView< ? > roiSegmentation = Views.zeroMin( segmentation );
						if ( roi != null )
						{
							long[] min = new long[ roi.numDimensions() ];
							roi.min( min );
							roiSegmentation = Views.translate( segmentation, min );
						}

						final AffineTransform3D transform = DetectionUtil.getTransform( sources, timepoint, setup, level );
						LabelImageUtils.createSpotsForFrame( graph, Cast.unchecked( roiSegmentation ), timepoint, transform, 1d );
					}
				}
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
		defaultSettings.put( DetectorKeys.KEY_ROI, null ); // No ROI by default.
		addSpecificDefaultSettings( defaultSettings );
		return defaultSettings;
	}

	protected abstract boolean validateSettings( final StringBuilder errorHolder );

	protected abstract Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions );

	protected abstract void addSpecificDefaultSettings( final Map< String, Object > defaultSettings );

	protected abstract String getDetectorName();
}
