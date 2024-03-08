package org.mastodon.mamut.io.importer.labelimage.ui;

import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.demo.DemoUtils;
import org.mastodon.mamut.model.Model;
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

		Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
		Model model = new Model();
		ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );

		ui.showUI();
		cmd.run( ImportSpotsFromBdvChannelView.class, true, "projectModel", projectModel );
	}
}
