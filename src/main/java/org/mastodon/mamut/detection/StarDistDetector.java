package org.mastodon.mamut.detection;


import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor.KEY_MODEL_TYPE;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.util.Cast;

import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "StarDist detector", description = "<html>"
		+ "This detector uses StarDist for segmentation. StarDist has been published in:<br>"
		+ "<i>Cell Detection with Star-convex Polygons</i> - "
		+ "<i>Uwe Schmidt, Martin Weigert, Coleman Broaddus, and Gene Myers</i>, International Conference on Medical Image Computing and Computer-Assisted Intervention (MICCAI), Granada, Spain, 2018.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "A cell probability threshold can be set to allow more/less sensitive detection.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~5.5GB hard disk space.</strong><br>"
		+ "</html>" )
public class StarDistDetector extends AbstractDetector
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	protected boolean validateSettings( StringBuilder errorHolder )
	{
		return checkParameter( settings, StarDistDetectorDescriptor.KEY_MODEL_TYPE, StarDist.ModelType.class, errorHolder );
	}

	@Override
	protected Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions )
	{
		try (StarDist starDist = new StarDist( ( StarDist.ModelType ) settings.get( KEY_MODEL_TYPE ) ))
		{
			return starDist.segmentImage( Cast.unchecked( image ) );
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = "StarDist failed: " + e.getMessage();
			logger.error( "StarDist failed: {}", e.getMessage() );
			return null;
		}
	}

	@Override
	protected void addSpecificDefaultSettings( Map< String, Object > defaultSettings )
	{
		defaultSettings.put( KEY_MODEL_TYPE, StarDist.ModelType.DEMO );
	}

	@Override
	protected String getDetectorName()
	{
		return "StarDist";
	}
}
