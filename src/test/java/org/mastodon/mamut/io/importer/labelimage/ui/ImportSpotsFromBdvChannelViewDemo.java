/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.io.importer.labelimage.ui;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
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
