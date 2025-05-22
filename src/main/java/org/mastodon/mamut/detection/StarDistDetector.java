package org.mastodon.mamut.detection;

import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.DEFAULT_SETUP_ID;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_SETUP_ID;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor.KEY_MODEL_TYPE;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;

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
import org.mastodon.tracking.mamut.detection.AbstractSpotDetectorOp;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;

@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "StarDist detector", description = "<html>"
		+ "This detector uses StarDist for segmentation. StarDist has been published in:<br>"
		+ "<i>Cell Detection with Star-convex Polygons</i> - "
		+ "<i>Uwe Schmidt, Martin Weigert, Coleman Broaddus, and Gene Myers</i>, International Conference on Medical Image Computing and Computer-Assisted Intervention (MICCAI), Granada, Spain, 2018.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "A cell probability threshold can be set to allow more/less sensitive detection.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~5.5GB hard disk space.</strong><br>"
		+ "</html>" )
public class StarDistDetector extends AbstractSpotDetectorOp
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void compute( final List< SourceAndConverter< ? > > sources, final ModelGraph graph )
	{
		ok = false;

		// And we clear the status display.
		statusService.clearStatus();

		// A. Read the settings map, and check the validity.
		final StringBuilder errorHolder = new StringBuilder();
		boolean good;
		good = checkParameter( settings, KEY_MODEL_TYPE, StarDist.ModelType.class, errorHolder );
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
		final StarDist.ModelType modelType = ( StarDist.ModelType ) settings.get( KEY_MODEL_TYPE );

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

		try (StarDist starDist = new StarDist( modelType ))
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
				boolean isData3D = is3D( image );
				Boolean isModelType2D = starDist.getModelType().is2D();

				if ( isModelType2D != null )
				{
					if ( isData3D && isModelType2D )
						throw new RuntimeException( "StarDist model type is 2D, but the image is 3D. Please select a 3D model type." );
					if ( !isData3D && !isModelType2D )
						throw new RuntimeException( "StarDist model type is 3D, but the image is 2D. Please select a 2D model type." );
				}

				/*
				 * This is the 3D image of the current time-point, specified
				 * channel. It is always 3D. If the source is 2D, the 3rd dimension
				 * will have a size of 1.
				 */
				Img< ? > segmentation = starDist.segmentImage( Cast.unchecked( image ) );

				final AffineTransform3D transform = DetectionUtil.getTransform( sources, timepoint, setup, level );
				LabelImageUtils.createSpotsForFrame( graph, Cast.unchecked( segmentation ), timepoint, transform, 1d );
			}
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = "StarDist failed: " + e.getMessage();
			logger.error( "StarDist failed: {}", e.getMessage() );
			return;
		}

		/*
		 * We are done! Graceful exit, stating we are ok.
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
		defaultSettings.put( KEY_MODEL_TYPE, StarDist.ModelType.DEMO );
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
