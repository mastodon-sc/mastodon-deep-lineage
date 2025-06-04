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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.StarDistDetectorDescriptor;
import org.scijava.Context;

import io.scif.img.ImgOpener;

class StarDistDetectorTest
{
	@Disabled( "This test is disabled, because it has very long runtime (> 2 minutes)" )
	@Test
	void testCompute3D() throws IllegalAccessException, URISyntaxException
	{
		StarDistDetector detector = new StarDistDetector();

		try (Context context = new Context())
		{
			ImgOpener imgOpener = new ImgOpener();
			URL url3d = getClass().getClassLoader().getResource( "org/mastodon/mamut/appose/nuclei_3d.tif" );
			URL url2d = getClass().getClassLoader().getResource( "org/mastodon/mamut/appose/blobs.tif" );
			Assertions.assertNotNull( url3d, "Resource not found" );
			Assertions.assertNotNull( url2d, "Resource not found" );

			File file3d = new File( url3d.toURI() );
			File file2d = new File( url2d.toURI() );
			Assertions.assertTrue( file3d.exists(), "File does not exist" );
			Assertions.assertTrue( file2d.exists(), "File does not exist" );
			Img< FloatType > img3d = imgOpener.openImgs( file3d.getAbsolutePath(), new FloatType() ).get( 0 );
			Img< FloatType > img2d = imgOpener.openImgs( file2d.getAbsolutePath(), new FloatType() ).get( 0 );
			context.inject( detector ); // make sure the detector is initialized with the context

			// set up the detector settings
			Map< String, Object > settings = new HashMap<>();
			settings.put( StarDistDetectorDescriptor.KEY_MODEL_TYPE, StarDist.ModelType.PLANT_NUCLEI_3D );
			settings.put( DetectorKeys.KEY_MIN_TIMEPOINT, 0 );
			settings.put( DetectorKeys.KEY_MAX_TIMEPOINT, 0 );
			settings.put( DetectorKeys.KEY_SETUP_ID, 0 );

			// make settings available for the detector
			Field settingsField = ReflectionUtils.findFields( StarDistDetector.class, f -> f.getName().equals( "settings" ),
					ReflectionUtils.HierarchyTraversalMode.TOP_DOWN ).get( 0 );
			settingsField.setAccessible( true );
			settingsField.set( detector, settings );

			// test with 3D model type
			Model model = new Model();
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img3d, model, context );
			Assertions.assertEquals( 0, model.getGraph().vertices().size() ); // before detection
			detector.compute( Collections.singletonList( projectModel.getSharedBdvData().getSources().get( 0 ) ), model.getGraph() );
			Assertions.assertEquals( 13, model.getGraph().vertices().size() ); // after detection

			// test re-use of the same model
			Assertions.assertEquals( 13, model.getGraph().vertices().size() ); // before detection
			detector.compute( Collections.singletonList( projectModel.getSharedBdvData().getSources().get( 0 ) ), model.getGraph() );
			Assertions.assertEquals( 26, model.getGraph().vertices().size() ); // after detection

			// test with DEMO model type
			settings.put( StarDistDetectorDescriptor.KEY_MODEL_TYPE, StarDist.ModelType.DEMO );
			// 3d
			Assertions.assertEquals( 26, model.getGraph().vertices().size() ); // before detection
			detector.compute( Collections.singletonList( projectModel.getSharedBdvData().getSources().get( 0 ) ), model.getGraph() );
			Assertions.assertEquals( 56, model.getGraph().vertices().size() ); // before detection

			// 2d
			ProjectModel projectModel2d = DemoUtils.wrapAsAppModel( img2d, model, context );
			Assertions.assertEquals( 56, model.getGraph().vertices().size() ); // after detection
			detector.compute( Collections.singletonList( projectModel2d.getSharedBdvData().getSources().get( 0 ) ), model.getGraph() );
			Assertions.assertEquals( 121, model.getGraph().vertices().size() ); // after detection
		}
	}
}
