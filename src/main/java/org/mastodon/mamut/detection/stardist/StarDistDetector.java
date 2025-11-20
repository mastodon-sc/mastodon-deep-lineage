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
package org.mastodon.mamut.detection.stardist;

import static org.mastodon.mamut.detection.stardist.StarDist.DEFAULT_NMS_THRESHOLD;
import static org.mastodon.mamut.detection.stardist.StarDist.DEFAULT_PROB_THRESHOLD;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor.KEY_MODEL_TYPE;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor.KEY_NMS_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor.KEY_PROB_THRESHOLD;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.util.Cast;

import org.apposed.appose.Service;
import org.mastodon.mamut.detection.DeepLearningDetector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code StarDistDetector} class implements a spot detection algorithm using the StarDist
 * deep learning package for segmentation of objects. StarDist is designed to identify and segment
 * star-convex shapes in image data.<br>
 * The class is implemented as a plugin in the context of the image processing framework
 * and conforms to the {@code SpotDetectorOp} interface. The segmentation process is
 * followed by the fitting of ellipsoids to the pixel clusters of detected objects to determine spots.<br>
 * Features:
 * <ul>
 *     <li>Use the StarDist deep learning package for segmentation</li>
 *     <li>Supports both 2D and 3D image data</li>
 *     <li>Configurable parameters for segmentation sensitivity and non-maximum suppression</li>
 *     <li>Supports downloading necessary resources during the initial use if not already available locally</li>
 * </ul>
 * Usage Notes:
 * <ul>
 *     <li>Ensure that the {@code StarDist} model type matches the dimensionality of your input data (2D vs. 3D)</li>
 *     <li>The detector requires an internet connection upon first use to download and install dependencies</li>
 *     <li>Approximately 5.5 GB of hard disk space is required for the installation of StarDist resources</li>
 * </ul>
 *Exceptions:
 * <ul>
 *     <li>Throws {@code StarDistModelException} if the selected model type does not match the dimensionality of the input image</li>
 *     <li>Catches and logs exceptions related to segmentation failures during the StarDist process</li>
 * </ul>
 * Key Settings:
 * <ul>
 *     <li>{@code KEY_MODEL_TYPE}: Specifies the StarDist model type to be used (e.g., pre-trained models)</li>
 *     <li>{@code KEY_PROB_THRESHOLD}: Sets the probability threshold for segmentation sensitivity</li>
 *     <li>{@code KEY_NMS_THRESHOLD}: Determines the non-maximum suppression threshold for handling overlapping detections</li>
 * </ul>
 */
@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "StarDist detector", description = "<html>"
		+ "This detector uses StarDist for segmentation. StarDist has been published in:<br>"
		+ "<i>Cell Detection with Star-convex Polygons</i> - "
		+ "<i>Uwe Schmidt, Martin Weigert, Coleman Broaddus, and Gene Myers</i>, International Conference on Medical Image Computing and Computer-Assisted Intervention (MICCAI), Granada, Spain, 2018.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "A cell probability threshold can be set to allow more/less sensitive detection.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~5.5GB hard disk space.</strong><br>"
		+ "</html>" )
public class StarDistDetector extends DeepLearningDetector
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	protected boolean validateSettings( StringBuilder errorHolder )
	{
		return checkParameter( settings, StarDistDetectorDescriptor.KEY_MODEL_TYPE, StarDist.ModelType.class, errorHolder );
	}

	@Override
	protected Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions,
			final Service python )
	{
		try
		{
			StarDist starDist = new StarDist( ( StarDist.ModelType ) settings.get( KEY_MODEL_TYPE ), python );
			boolean isData3D = is3D( image );
			Boolean isModelType2D = starDist.getModelType().is2D();
			if ( isModelType2D != null )
			{
				if ( isData3D && isModelType2D )
					throw new StarDistModelException( "StarDist model type is 2D, but the image is 3D. Please select a 3D model type." );
				if ( !isData3D && !isModelType2D )
					throw new StarDistModelException( "StarDist model type is 3D, but the image is 2D. Please select a 2D model type." );
			}
			starDist.setDataIs2D( !isData3D );
			starDist.setProbThresh( ( double ) settings.get( KEY_PROB_THRESHOLD ) );
			starDist.setNmsThresh( ( double ) settings.get( KEY_NMS_THRESHOLD ) );
			return starDist.segmentImage( Cast.unchecked( image ) );
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = " StarDist failed: " + e.getMessage();
			logger.error( "StarDist failed: {}", e.getMessage() );
			return null;
		}
	}

	@Override
	protected void addSpecificDefaultSettings( Map< String, Object > defaultSettings )
	{
		defaultSettings.put( KEY_MODEL_TYPE, StarDist.ModelType.DEMO );
		defaultSettings.put( KEY_PROB_THRESHOLD, DEFAULT_PROB_THRESHOLD );
		defaultSettings.put( KEY_NMS_THRESHOLD, DEFAULT_NMS_THRESHOLD );
	}

	@Override
	protected String getDetectorName()
	{
		return "StarDist";
	}

	@Override
	protected String getPythonEnvContent()
	{
		return StarDist.ENV_FILE_CONTENT;
	}

	@Override
	protected String getPythonEnvName()
	{
		return StarDist.ENV_NAME;
	}

	@Override
	protected String getImportScript( final boolean dataIs2D )
	{
		return StarDist.generateImportStatements( ( StarDist.ModelType ) settings.get( KEY_MODEL_TYPE ), dataIs2D );
	}

	@Override
	protected String getPythonEnvInit()
	{
		return "import numpy\nfrom stardist.models import StarDist2D\n"; // NB: StarDist2D import needs to be inited even for 3D cases
	}
}
