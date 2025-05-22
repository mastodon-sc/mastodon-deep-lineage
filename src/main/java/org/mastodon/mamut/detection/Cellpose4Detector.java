/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2023 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_SETUP_ID;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_SETUP_ID;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.Cellpose4DetectorDescriptor.KEY_CELL_PROBABILITY_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.Cellpose4DetectorDescriptor.KEY_FLOW_THRESHOLD;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Cast;

import org.mastodon.mamut.util.LabelImageUtils;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.tracking.detection.DetectionUtil;
import org.mastodon.tracking.mamut.detection.AbstractSpotDetectorOp;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "Cellpose4 detector", description = "<html>"
		+ "This detector uses Cellpose for segmentation. Cellpose has been published in:<br>"
		+ "<i>Cellpose: a generalist algorithm for cellular segmentation </i> - "
		+ "<i>Stringer et al.</i>, 2021, Nature Methods.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "A cell probability threshold can be set to allow more/less sensitive detection.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~9GB hard disk space.</strong><br>"
		+ "</html>" )
public class Cellpose4Detector extends AbstractSpotDetectorOp
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void compute( final List< SourceAndConverter< ? > > sources, final ModelGraph graph )
	{
		ok = false;

		// And we clear the status display.
		statusService.clearStatus();

		// A. Read the settings map, and check validity.
		final StringBuilder errorHolder = new StringBuilder();
		boolean good;
		good = checkParameter( settings, KEY_SETUP_ID, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_MIN_TIMEPOINT, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_MAX_TIMEPOINT, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_CELL_PROBABILITY_THRESHOLD, Double.class, errorHolder );
		good = good & checkParameter( settings, KEY_FLOW_THRESHOLD, Double.class, errorHolder );
		if ( !good )
		{
			errorMessage = errorHolder.toString();
			return;
		}

		// Now we are sure that they are here, and of the right class.
		final int minTimepoint = ( int ) settings.get( KEY_MIN_TIMEPOINT );
		final int maxTimepoint = ( int ) settings.get( KEY_MAX_TIMEPOINT );
		final int setup = ( int ) settings.get( KEY_SETUP_ID );
		final double cellProbabilityThreshold = ( double ) settings.get( KEY_CELL_PROBABILITY_THRESHOLD );
		final double flowThreshold = ( double ) settings.get( KEY_FLOW_THRESHOLD );

		if ( setup < 0 || setup >= sources.size() )
		{
			errorMessage = "The parameter " + KEY_SETUP_ID + " is not in the range of available sources ("
					+ sources.size() + "): " + setup;
			return;
		}
		if ( maxTimepoint < minTimepoint )
		{
			errorHolder.append( "Min time-point should smaller than or equal to max time-point, be was min = " ).append( minTimepoint )
					.append( " and max = " ).append( maxTimepoint ).append( "\n" );
			return;
		}
		// Now we are sure they are valid.

		// The `statusService` can be used to show short messages.
		statusService.showStatus( "Detecting spots using Cellpose." );

		try (Cellpose4 cellpose = new Cellpose4())
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

				/*
				 * This source has possibly several resolution levels. And for your
				 * own real detector, it might be very interesting to work on a
				 * lower resolution (higher level). Check the DogDetectorOp code for
				 * instance. For us, we don't even care for pixels, we just want to
				 * have the image boundary from the highest resolution (level 0).
				 */
				final int level = 0;
				final RandomAccessibleInterval< ? > image = source.getSource( timepoint, level );

				/*
				 * This is the 3D image of the current time-point, specified
				 * channel. It is always 3D. If the source is 2D, the 3rd dimension
				 * will have a size of 1.
				 */
				cellpose.set3D( is3D( image ) );
				cellpose.setCellprobThreshold( cellProbabilityThreshold );
				cellpose.setFlowThreshold( flowThreshold );
				Img< ? > segmentation = cellpose.segmentImage( Cast.unchecked( image ) );

				final AffineTransform3D transform = DetectionUtil.getTransform( sources, timepoint, setup, level );
				LabelImageUtils.createSpotsForFrame( graph, Cast.unchecked( segmentation ), timepoint, transform, 1d );
			}
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = "Cellpose failed: " + e.getMessage();
			logger.error( "Cellpose failed: {}", e.getMessage() );
			return;
		}

		/*
		 * We are done! Gracefully exit, stating we are ok.
		 */
		ok = true;

	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		/*
		 * We declare all the parameters required by this detector by returning
		 * a new map containing default values *of the right class*.
		 */
		final Map< String, Object > defaultSettings = new HashMap<>();
		defaultSettings.put( KEY_SETUP_ID, DEFAULT_SETUP_ID );
		defaultSettings.put( KEY_MIN_TIMEPOINT, DEFAULT_MIN_TIMEPOINT );
		defaultSettings.put( KEY_MAX_TIMEPOINT, DEFAULT_MAX_TIMEPOINT );
		defaultSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, Cellpose4.DEFAULT_CELLPROB_THRESHOLD );
		defaultSettings.put( KEY_FLOW_THRESHOLD, Cellpose4.DEFAULT_FLOW_THRESHOLD );
		return defaultSettings;
	}

	private boolean is3D( final RandomAccessibleInterval< ? > image )
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
}
