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
	@Test
	void testCompute3D() throws IllegalAccessException, URISyntaxException
	{
		StarDistDetector detector = new StarDistDetector();
		Model model3d = new Model();
		Model model2d = new Model();

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

			ProjectModel projectModel3d = DemoUtils.wrapAsAppModel( img3d, model3d, context );
			ProjectModel projectModel2d = DemoUtils.wrapAsAppModel( img2d, model2d, context );
			Assertions.assertEquals( 0, model3d.getGraph().vertices().size() ); // before detection
			Assertions.assertEquals( 0, model2d.getGraph().edges().size() ); // before detection
			detector.compute( Collections.singletonList( projectModel3d.getSharedBdvData().getSources().get( 0 ) ), model3d.getGraph() );
			Assertions.assertEquals( 13, model3d.getGraph().vertices().size() ); // after detection
			detector.compute( Collections.singletonList( projectModel2d.getSharedBdvData().getSources().get( 0 ) ), model2d.getGraph() );
			Assertions.assertNotEquals( 0, model2d.getGraph().vertices().size() ); // after detection
		}
	}
}
