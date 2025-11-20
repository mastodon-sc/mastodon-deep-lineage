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

import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor.KEY_CELL_PROBABILITY_THRESHOLD;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor.KEY_DIAMETER;
import static org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose.Cellpose4DetectorDescriptor.KEY_FLOW_THRESHOLD;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.detection.DeepLearningDetectorKeys;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.io.importer.labelimage.util.SphereRenderer;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.detection.AbstractSpotDetectorOp;
import org.scijava.Context;


class Cellpose4DetectorTest
{

	@Disabled( "This test is disabled, because it has very long runtime (> 5 minutes)" )
	@Test
	void testCompute3D() throws IllegalAccessException, NoSuchFieldException
	{
		Cellpose4Detector detector = new Cellpose4Detector();
		detector.setConfirmEnvInstallation( false );
		Model model = new Model();

		try (Context context = new Context())
		{
			org.scijava.log.Logger log = context.getService( org.scijava.log.LogService.class ).subLogger( "Cellpose4DetectorTest" );
			Field logger = AbstractSpotDetectorOp.class.getDeclaredField( "log" );
			logger.setAccessible( true );
			logger.set( detector, log );

			Img< FloatType > img = ArrayImgs.floats( 12, 12, 12 );
			SphereRenderer.renderSphere( new int[] { 5, 5, 5 }, 5, 100, img );
			context.inject( detector ); // make sure the detector is initialized with the context

			// set up the detector settings
			Map< String, Object > settings = new HashMap<>();
			settings.put( KEY_CELL_PROBABILITY_THRESHOLD, Cellpose.DEFAULT_CELLPROB_THRESHOLD );
			settings.put( KEY_FLOW_THRESHOLD, Cellpose.DEFAULT_FLOW_THRESHOLD );
			settings.put( KEY_DIAMETER, Cellpose.DEFAULT_DIAMETER );
			settings.put( DetectorKeys.KEY_MIN_TIMEPOINT, 0 );
			settings.put( DetectorKeys.KEY_MAX_TIMEPOINT, 0 );
			settings.put( DetectorKeys.KEY_SETUP_ID, 0 );
			settings.put( DeepLearningDetectorKeys.KEY_LEVEL, 0 );
			settings.put( DeepLearningDetectorKeys.KEY_GPU_ID, 0 );
			settings.put( DeepLearningDetectorKeys.KEY_GPU_MEMORY_FRACTION, 1d );

			// make settings available for the detector
			Field settingsField = ReflectionUtils.findFields( Cellpose4Detector.class, f -> f.getName().equals( "settings" ),
					ReflectionUtils.HierarchyTraversalMode.TOP_DOWN ).get( 0 );
			settingsField.setAccessible( true );
			settingsField.set( detector, settings );

			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			Assertions.assertEquals( 0, model.getGraph().vertices().size() ); // before detection
			detector.compute( Collections.singletonList( projectModel.getSharedBdvData().getSources().get( 0 ) ), model.getGraph() );
			Assertions.assertEquals( 1, model.getGraph().vertices().size() ); // after detection
		}
	}
}
