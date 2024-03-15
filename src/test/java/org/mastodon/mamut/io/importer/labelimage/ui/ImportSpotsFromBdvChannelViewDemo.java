package org.mastodon.mamut.io.importer.labelimage.ui;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.demo.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class ImportSpotsFromBdvChannelViewDemo
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
		// open the image in Mastodon
		Model model = new Model();
		ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
		new MainWindow( projectModel ).setVisible( true );
		projectModel.getWindowManager().createView( MamutViewBdv.class );
		// run import spots command
		cmd.run( ImportSpotsFromBdvChannelView.class, true, "projectModel", projectModel );
	}
}
