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
package org.mastodon.mamut.detection.cellpose;

import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_CELLPROB_THRESHOLD;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_DIAMETER;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_FLOW_THRESHOLD;
import static org.mastodon.mamut.detection.cellpose.Cellpose3.ModelType.CYTO3;
import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose3DetectorDescriptor.KEY_DIAMETER;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose3DetectorDescriptor.KEY_RESPECT_ANISOTROPY;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose3DetectorDescriptor.KEY_CELL_PROBABILITY_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose3DetectorDescriptor.KEY_MODEL_TYPE;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose3DetectorDescriptor.KEY_FLOW_THRESHOLD;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.util.Cast;

import org.mastodon.mamut.detection.DeepLearningDetector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A detector class that uses the Cellpose3 algorithm for image segmentation.<br>
 * This class performs segmentation of images and derives spots by fitting ellipsoids
 * to the pixel clouds of detected objects. It supports various Cellpose pre-trained.<br>
 * Key Features:
 * <ul>
 *     <li>Supports using different Cellpose models for various image types.</li>
 *     <li>Allows setting a cell probability threshold for sensitivity adjustments.</li>
 *     <li>Supports anisotropy for 3D data.</li>
 *     <li>Requires an internet connection for initial installation, consuming approximately 7.5GB of hard disk space.</li>
 * </ul>
 */
@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "Cellpose3 detector", description = "<html>"
		+ "This detector uses Cellpose for segmentation. Cellpose has been published in:<br>"
		+ "<i>Cellpose: a generalist algorithm for cellular segmentation </i> - "
		+ "<i>Stringer et al.</i>, 2021, Nature Methods.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "Different Cellpose models can be used for different types of images. A cell probability threshold can be set to allow more/less sensitive detection.<br>"
		+ "For 3D data, anisotropy can be respected.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~7.5GB hard disk space.</strong><br>"
		+ "</html>" )
public class Cellpose3Detector extends DeepLearningDetector
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	protected boolean validateSettings( final StringBuilder errorHolder )
	{
		return checkParameter( settings, KEY_MODEL_TYPE, Cellpose3.ModelType.class, errorHolder )
				&& checkParameter( settings, KEY_CELL_PROBABILITY_THRESHOLD, Double.class, errorHolder )
				&& checkParameter( settings, KEY_FLOW_THRESHOLD, Double.class, errorHolder )
				&& checkParameter( settings, KEY_DIAMETER, Double.class, errorHolder )
				&& checkParameter( settings, KEY_RESPECT_ANISOTROPY, Boolean.class, errorHolder );
	}

	@Override
	protected Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions )
	{
		try (Cellpose3 cellpose = new Cellpose3( ( Cellpose3.ModelType ) settings.get( KEY_MODEL_TYPE ) ))
		{
			cellpose.set3D( is3D( image ) );
			cellpose.setCellProbThreshold( ( double ) settings.get( KEY_CELL_PROBABILITY_THRESHOLD ) );
			cellpose.setFlowThreshold( ( double ) settings.get( KEY_FLOW_THRESHOLD ) );
			cellpose.setDiameter( ( double ) settings.get( KEY_DIAMETER ) );
			final boolean respectAnisotropy = ( boolean ) settings.get( KEY_RESPECT_ANISOTROPY );
			double anisotropy = respectAnisotropy ? getAnisotropy( voxelDimensions ) : 1.0;
			cellpose.setAnisotropy( ( float ) anisotropy );
			return cellpose.segmentImage( Cast.unchecked( image ) );
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = "Cellpose3 failed: " + e.getMessage();
			logger.error( "Cellpose3 failed: {}", e.getMessage() );
			return null;
		}
	}

	@Override
	protected void addSpecificDefaultSettings( final Map< String, Object > defaultSettings )
	{
		defaultSettings.put( KEY_MODEL_TYPE, CYTO3 );
		defaultSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, DEFAULT_CELLPROB_THRESHOLD );
		defaultSettings.put( KEY_FLOW_THRESHOLD, DEFAULT_FLOW_THRESHOLD );
		defaultSettings.put( KEY_DIAMETER, DEFAULT_DIAMETER );
		defaultSettings.put( KEY_RESPECT_ANISOTROPY, true );
	}

	@Override
	protected String getDetectorName()
	{
		return "Cellpose3";
	}
}
