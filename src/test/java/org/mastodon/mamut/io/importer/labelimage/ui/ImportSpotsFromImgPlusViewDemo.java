package org.mastodon.mamut.io.importer.labelimage.ui;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
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

		Img< FloatType > image = DemoUtils.generateExampleImage();

		// show ImageJ
		ui.showUI();
		// show image in ImageJ
		ImagePlus imagePlus = ImageJFunctions.wrap( image, "label image" );
		imagePlus.setDimensions( 1, 100, 2 );
		imagePlus.setZ( 50 );
		imagePlus.show();
		// open the image in Mastodon
		Model model = new Model();
		ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
		new MainWindow( projectModel ).setVisible( true );
		projectModel.getWindowManager().createView( MamutViewBdv.class );
		// run import spots command
		cmd.run( ImportSpotsFromImgPlusView.class, true, "projectModel", projectModel );
	}
}
