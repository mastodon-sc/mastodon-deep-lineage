package org.mastodon.mamut.io.importer.labelimage.util;

import bdv.viewer.SourceAndConverter;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

public class SmallLabelDemo
{
	public static void main( String[] args )
	{
		LegacyInjector.preinit();
		try (final Context context = new Context())
		{
			long[] dimensions = { 100, 100, 100 };

			int[] center1 = { 20, 80, 50 };
			double radius1 = 0.5d;

			int[] center2 = { 40, 80, 50 };
			double radius2 = 1d;

			int[] center3 = { 60, 80, 50 };
			double radius3 = 10d;

			Img< FloatType > image = ArrayImgs.floats( dimensions );
			SphereRenderer.renderSphere( center1, radius1, 1, image );
			SphereRenderer.renderSphere( center2, radius2, 2, image );
			SphereRenderer.renderSphere( center3, radius3, 3, image );

			int[] center4 = { 20, 50, 50 };
			double radius4 = 0.5d;

			int[] center5 = { 40, 50, 50 };
			double radius5 = 1d;

			int[] center6 = { 60, 50, 50 };
			double radius6 = 10d;

			CircleRenderer.renderCircle( center4, radius4, 4, image, CircleRenderer.Plane.XY );
			CircleRenderer.renderCircle( center5, radius5, 5, image, CircleRenderer.Plane.XY );
			CircleRenderer.renderCircle( center6, radius6, 6, image, CircleRenderer.Plane.XY );

			int[] start7 = { 80, 45, 50 };
			int[] end7 = { 80, 55, 50 };

			LineRenderer.renderLine( start7, end7, 7, image );

			Model model = new Model();
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			SourceAndConverter< ? > sourceAndConverter = projectModel.getSharedBdvData().getSources().get( 0 );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, sourceAndConverter.getSpimSource(), 1d, false );
			DemoUtils.showBdvWindow( projectModel );
		}
	}
}
