package org.mastodon.mamut.detection.cellpose;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.io.importer.labelimage.util.SphereRenderer;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.detection.DetectorKeys;
import org.scijava.Context;

class Cellpose3DetectorTest
{

	@Test
	void testCompute() throws IllegalAccessException
	{
		Cellpose3Detector detector = new Cellpose3Detector();
		Model model = new Model();

		try (Context context = new Context())
		{
			Img< FloatType > img = ArrayImgs.floats( 12, 12, 12 );
			SphereRenderer.renderSphere( new int[] { 5, 5, 5 }, 5, 100, img );
			context.inject( detector ); // make sure the detector is initialized with the context

			// make settings available for the detector
			Field settingsField = ReflectionUtils.findFields( Cellpose3Detector.class, f -> f.getName().equals( "settings" ),
					ReflectionUtils.HierarchyTraversalMode.TOP_DOWN ).get( 0 );
			settingsField.setAccessible( true );
			Map< String, Object > settings = new HashMap<>();
			settings.put( DetectorKeys.KEY_MIN_TIMEPOINT, 0 );
			settings.put( DetectorKeys.KEY_MAX_TIMEPOINT, 0 );
			settings.put( DetectorKeys.KEY_SETUP_ID, 0 );
			settingsField.set( detector, settings );

			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			Assertions.assertEquals( 0, model.getGraph().vertices().size() ); // before detection
			detector.compute( Collections.singletonList( projectModel.getSharedBdvData().getSources().get( 0 ) ), model.getGraph() );
			Assertions.assertEquals( 1, model.getGraph().vertices().size() ); // after detection
		}
	}
}
