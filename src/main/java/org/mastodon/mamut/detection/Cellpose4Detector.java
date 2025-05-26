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

import static org.mastodon.tracking.linking.LinkingUtils.checkParameter;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.Cellpose4DetectorDescriptor.KEY_CELL_PROBABILITY_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.Cellpose4DetectorDescriptor.KEY_FLOW_THRESHOLD;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.util.Cast;

import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin( type = SpotDetectorOp.class, priority = Priority.LOW, name = "Cellpose4 detector", description = "<html>"
		+ "This detector uses Cellpose for segmentation. Cellpose has been published in:<br>"
		+ "<i>Cellpose: a generalist algorithm for cellular segmentation </i> - "
		+ "<i>Stringer et al.</i>, 2021, Nature Methods.<br><br>"
		+ "After the segmentation, spots are derived by fitting of ellipsoids to the pixel clouds of the detected objects.<br>"
		+ "A cell probability threshold can be set to allow more/less sensitive detection.<br><br>"
		+ "<strong>When this detection method is used for the first time, internet connection is needed, since an internal installation process is started. The installation consumes ~9GB hard disk space.</strong><br>"
		+ "</html>" )
public class Cellpose4Detector extends AbstractDetector
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	protected boolean validateSettings( final StringBuilder errorHolder )
	{
		return checkParameter( settings, KEY_CELL_PROBABILITY_THRESHOLD, Double.class, errorHolder )
				&& checkParameter( settings, KEY_FLOW_THRESHOLD, Double.class, errorHolder );
	}

	@Override
	protected Img< ? > performSegmentation( final RandomAccessibleInterval< ? > image, final double[] voxelDimensions )
	{
		try (Cellpose4 cellpose = new Cellpose4())
		{
			cellpose.set3D( is3D( image ) );
			cellpose.setCellprobThreshold( ( double ) settings.get( KEY_CELL_PROBABILITY_THRESHOLD ) );
			cellpose.setFlowThreshold( ( double ) settings.get( KEY_FLOW_THRESHOLD ) );
			return cellpose.segmentImage( Cast.unchecked( image ) );
		}
		catch ( Exception e )
		{
			ok = false;
			errorMessage = "Cellpose4 failed: " + e.getMessage();
			logger.error( "Cellpose4 failed: {}", e.getMessage() );
			return null;
		}
	}

	@Override
	protected void addSpecificDefaultSettings( final Map< String, Object > defaultSettings )
	{
		defaultSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, Cellpose4.DEFAULT_CELLPROB_THRESHOLD );
		defaultSettings.put( KEY_FLOW_THRESHOLD, Cellpose4.DEFAULT_FLOW_THRESHOLD );
	}

	@Override
	protected String getDetectorName()
	{
		return "Cellpose4";
	}
}
