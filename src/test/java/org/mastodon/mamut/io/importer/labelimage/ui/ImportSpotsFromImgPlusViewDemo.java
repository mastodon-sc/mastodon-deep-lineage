package org.mastodon.mamut.io.importer.labelimage.ui;

import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.demo.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class ImportSpotsFromImgPlusViewDemo
{

	public static void main( String[] args )
	{
		@SuppressWarnings( "all" )
		Context context = new Context();
		UIService ui = context.service( UIService.class );
		CommandService cmd = context.service( CommandService.class );

		Img< FloatType > image = generateExampleImage();

		// show image in ImageJ
		ui.showUI();
		ImageJFunctions.wrap( image, "label image" ).show(); 
		// open the image in Mastodon
		Model model = new Model();
		ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
		new MainWindow( projectModel ).setVisible( true );
		projectModel.getWindowManager().createView( MamutViewBdv.class );
		// run import spots command
		cmd.run( ImportSpotsFromImgPlusView.class, true, "projectModel", projectModel );
	}

	private static Img< FloatType > generateExampleImage()
	{
		double[] center = { 40, 50, 60 };
		double[][] givenCovariance = {
				{ 400, 20, -10 },
				{ 20, 200, 30 },
				{ -10, 30, 100 }
		};
		long[] dimensions = { 100, 100, 100 };
		int background = 0;
		int pixelValue = 1;
		Img< FloatType > frame = DemoUtils.generateExampleImage( center, givenCovariance, dimensions, background, pixelValue );
		List< Img< FloatType > > twoIdenticalFrames = Arrays.asList( frame, frame );
		return ImgView.wrap( Views.stack( twoIdenticalFrames ) );
	}
}
