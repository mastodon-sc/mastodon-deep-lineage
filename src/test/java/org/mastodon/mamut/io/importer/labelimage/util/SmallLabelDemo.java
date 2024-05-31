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
			double[] center1 = { 20, 80, 50 };
			double radius1 = 0.5d;

			double[] center2 = { 40, 80, 50 };
			double radius2 = 1d;

			double[] center3 = { 60, 80, 50 };
			double radius3 = 10d;

			long[] dimensions = { 100, 100, 100 };

			Img< FloatType > image = ArrayImgs.floats( dimensions );
			SphereRenderer.renderSphere( center1, radius1, 1, image );
			SphereRenderer.renderSphere( center2, radius2, 2, image );
			SphereRenderer.renderSphere( center3, radius3, 3, image );

			Model model = new Model();
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			SourceAndConverter< ? > sourceAndConverter = projectModel.getSharedBdvData().getSources().get( 0 );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, sourceAndConverter.getSpimSource(), 1d, false );
			DemoUtils.showBdvWindow( projectModel );
		}
	}
}
