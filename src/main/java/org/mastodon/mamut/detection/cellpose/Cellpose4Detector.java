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
package org.mastodon.mamut.detection.cellpose;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.DEFAULT_GPU_ID;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.DEFAULT_GPU_MEMORY_FRACTION;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.DEFAULT_LEVEL;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_GPU_ID;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_GPU_MEMORY_FRACTION;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_CELLPROB_THRESHOLD;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_FLOW_THRESHOLD;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor.KEY_CELL_PROBABILITY_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor.KEY_DIAMETER;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor.KEY_FLOW_THRESHOLD;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.util.Cast;

import org.apposed.appose.Appose;
import org.apposed.appose.Builder;
import org.apposed.appose.Service;
import org.mastodon.mamut.detection.DeepLearningDetector;
import org.mastodon.mamut.util.ImgUtils;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A detector class that uses the Cellpose4 algorithm for image segmentation.<br>
 * This class performs segmentation of images and derives spots by fitting ellipsoids
 * to the pixel clouds of detected objects. It supports various Cellpose pre-trained.<br>
 * Key Features:
 * <ul>
 *     <li>Allows setting a cell probability threshold for sensitivity adjustments.</li>
 *     <li>Requires an internet connection for initial installation, consuming approximately 9GB of hard disk space.</li>
 * </ul>
 */
@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "Cellpose4 detector", description = "<html>"
		+ "This detector uses Cellpose SAM for segmentation. Cellpose SAM has been published in:<br>"
		+ "<i>Cellpose-SAM: superhuman generalization for cellular segmentation. </i> - "
		+ "<i>Pachitariu, M., Rariden, M., & Stringer, C.</i>, 2025, bioRxiv.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "A cell probability threshold can be set to allow more/less sensitive detection.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~9GB hard disk space.</strong><br>"
		+ "</html>" )
public class Cellpose4Detector extends DeepLearningDetector
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	protected boolean validateSettings( final StringBuilder errorHolder )
	{
		return checkParameter( settings, Cellpose4DetectorDescriptor.KEY_CELL_PROBABILITY_THRESHOLD, Double.class, errorHolder )
				&& checkParameter( settings, Cellpose4DetectorDescriptor.KEY_FLOW_THRESHOLD, Double.class, errorHolder )
				&& checkParameter( settings, Cellpose4DetectorDescriptor.KEY_DIAMETER, Double.class, errorHolder )
				&& checkParameter( settings, KEY_LEVEL, Integer.class, errorHolder )
				&& checkParameter( settings, KEY_GPU_ID, Integer.class, errorHolder )
				&& checkParameter( settings, KEY_GPU_MEMORY_FRACTION, Double.class, errorHolder );
	}

	@Override
	protected Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions,
			final Service python )
	{
		try
		{
			Cellpose4 cellpose = new Cellpose4( python, log );
			cellpose.set3D( ImgUtils.is3D( image ) );
			cellpose.setCellProbThreshold( ( double ) settings.get( KEY_CELL_PROBABILITY_THRESHOLD ) );
			cellpose.setFlowThreshold( ( double ) settings.get( KEY_FLOW_THRESHOLD ) );
			cellpose.setDiameter( ( double ) settings.get( KEY_DIAMETER ) );
			cellpose.setGpuID( ( int ) settings.get( KEY_GPU_ID ) );
			cellpose.setGpuMemoryFraction( ( double ) settings.get( KEY_GPU_MEMORY_FRACTION ) );
			return cellpose.segmentImage( Cast.unchecked( image ) );
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = " Cellpose4 failed: " + e.getMessage();
			logger.error( "Cellpose4 failed: {}", e.getMessage() );
			return null;
		}
	}

	@Override
	protected void addSpecificDefaultSettings( final Map< String, Object > defaultSettings )
	{
		defaultSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, DEFAULT_CELLPROB_THRESHOLD );
		defaultSettings.put( KEY_FLOW_THRESHOLD, DEFAULT_FLOW_THRESHOLD );
		defaultSettings.put( KEY_DIAMETER, Cellpose.DEFAULT_DIAMETER );
		defaultSettings.put( KEY_LEVEL, DEFAULT_LEVEL );
		defaultSettings.put( KEY_GPU_ID, DEFAULT_GPU_ID );
		defaultSettings.put( KEY_GPU_MEMORY_FRACTION, DEFAULT_GPU_MEMORY_FRACTION );
	}

	@Override
	protected String getDetectorName()
	{
		return "Cellpose4";
	}

	@Override
	protected String getPythonEnvContent()
	{
		return Cellpose4.ENV_FILE_CONTENT;
	}

	@Override
	protected String getPythonEnvName()
	{
		return Cellpose4.ENV_NAME;
	}

	@Override
	protected Builder< ? > getBuilder()
	{
		return Appose.mamba().scheme( "environment.yml" );
	}

	@Override
	protected String getImportScript( final boolean dataIs2D )
	{
		return Cellpose.generateImportStatements();
	}
}
