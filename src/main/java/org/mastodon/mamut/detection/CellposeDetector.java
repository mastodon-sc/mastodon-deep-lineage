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
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_RADIUS;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_SETUP_ID;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_RADIUS;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_SETUP_ID;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.CellposeDetectorDescriptor.KEY_MODEL_TYPE;

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

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "Cellpose detector", description = "<html>"
		+ "This detector uses Cellpose for segmentation. "
		+ "Spots are subsequently derived from the segmentation by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "Different Cellpose models can be used for different types of images."
		+ "</html>" )
public class CellposeDetector extends AbstractSpotDetectorOp
{

	@Override
	public void compute( final List< SourceAndConverter< ? > > sources, final ModelGraph graph )
	{
		System.out.println( "Cellpose detector starting..." );
		/*
		 * The abstract class `AbstractSpotDetectorOp` we inherit provides
		 * several useful fields that are used to store settings, communicate
		 * success or failure with an error message, or sends messages to the
		 * user interface.
		 * 
		 * The first one is the `ok` flag, that states whether the computation
		 * finished successfully. If not, a meaningful error message should be
		 * provided in the `errorMessage` field. The user interface will use
		 * them.
		 * 
		 * We start by settings the `ok` flag to false. If we break before the
		 * end, this will signal something wrong happened.
		 */
		ok = false;

		/*
		 * And we clear the status display.
		 */
		statusService.clearStatus();

		/*
		 * A. Read the settings map, and check validity.
		 * 
		 * Let's be a bit thorough with this part.
		 * 
		 * The `settings` variable (stored in the mother abstract class) is a
		 * `Map<String, Object>` that will be passed with all the settings the
		 * user will specify, either programmatically or in the wizard. For our
		 * dummy detector example, we have 5 parameters: 1. the number of spots
		 * we will create, 2. their radius, 3. with respect to what source of
		 * channel, 4. and 5. the min and max time-points we will process.
		 * 
		 * To check that they are present in the map and of the right class, we
		 * use a utility function defined in `LinkingUtils` that accepts the
		 * settings map, the key of the parameter to test, its desired class,
		 * and a holder to store error messages. It goes like this:
		 */
		final StringBuilder errorHolder = new StringBuilder();
		boolean good;
		good = checkParameter( settings, KEY_MODEL_TYPE, Cellpose.MODEL_TYPE.class, errorHolder );
		good = good & checkParameter( settings, KEY_SETUP_ID, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_MIN_TIMEPOINT, Integer.class, errorHolder );
		good = good & checkParameter( settings, KEY_MAX_TIMEPOINT, Integer.class, errorHolder );
		if ( !good )
		{
			errorMessage = errorHolder.toString();
			return;
		}
		// Now we are sure that they are here, and of the right class.

		final int minTimepoint = ( int ) settings.get( KEY_MIN_TIMEPOINT );
		final int maxTimepoint = ( int ) settings.get( KEY_MAX_TIMEPOINT );
		final int setup = ( int ) settings.get( KEY_SETUP_ID );
		final Cellpose.MODEL_TYPE modelType = ( Cellpose.MODEL_TYPE ) settings.get( KEY_MODEL_TYPE );

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

		try (Cellpose cellpose = new Cellpose( modelType ))
		{
			for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++ )
			{
				// We use the `statusService to show progress.
				statusService.showProgress( timepoint - minTimepoint + 1, maxTimepoint - minTimepoint + 1 );

				/*
				 * The detection process can be canceled. For instance, if the user
				 * clicks on the 'cancel' button, this class will be notified via
				 * the `isCanceled()` method.
				 *
				 * You can check if the process has been canceled as you wish (you
				 * can even ignore it), but we recommend checking every time-point.
				 */
				if ( isCanceled() )
					break; // Exit but don't fail.

				/*
				 * Important: With the image data structure we use, some time-points
				 * may be devoid of a certain source. We need to test for this, and
				 * should it be the case, to skip the time-point.
				 *
				 * Again, there is a utility function to do this:
				 */
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

				Img< ? > segmentation = cellpose.segmentImage( Cast.unchecked( image ) );

				final AffineTransform3D transform = DetectionUtil.getTransform( sources, timepoint, setup, level );
				LabelImageUtils.createSpotsForFrame( graph, Cast.unchecked( segmentation ), timepoint, transform, 1d );
			}
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = "Cellpose failed: " + e.getMessage();
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
		defaultSettings.put( KEY_RADIUS, DEFAULT_RADIUS );
		return defaultSettings;
	}
}
