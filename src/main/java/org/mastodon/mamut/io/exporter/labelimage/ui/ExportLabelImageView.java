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
package org.mastodon.mamut.io.exporter.labelimage.ui;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.exporter.labelimage.ExportLabelImageController;
import org.mastodon.mamut.io.exporter.labelimage.config.LabelOptions;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, label = "Run export label image using ellipsoids")
public class ExportLabelImageView extends DynamicCommand
{
	private static final int WIDTH = 15;

	@SuppressWarnings("all")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false)
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Export label image using ellipsoids</h1>\n"
			+ "<p>This plugin is capable of saving a label image to a file using the existing ellipsoids in Mastodon.</p>\n"
			+ "<p>For the labels, the <i>spot ids</i>, <i>branch spot ids</i> or the <i>track ids</i> that correspond to the ellipsoids may be used. Since these Ids are counted zero based in Mastodon, an <b>offset of 1</b> is added to all Ids so that no label clashes with the background of zero.</p>\n"
			+ "<p>Ids in the range between 0 and 16.777.216 (24 bit) are supported.</p>\n"
			+ "<p>The supported export format is '*.tif'-files.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings("all")
	@Parameter(label = "Label Id", choices = { "Spot track Id", "Branch spot ID", "Spot ID" })
	private String option = LabelOptions.BRANCH_SPOT_ID.getName();

	@SuppressWarnings("all")
	@Parameter( label = "Frame rate reduction", description = "Only export every n-th frame. 1 means no reduction. Value must be >= 1.", min = "1" )
	private int frameRateReduction = 1;

	@SuppressWarnings( "all" )
	@Parameter( label = "Resolution level", description = "Spatial resolution level of export. 0 means highest resolution. Value > 0 mean lower resolution.", initializer = "initResolutionMinMax" )
	private int resolutionLevel = 0;

	@SuppressWarnings("unused")
	@Parameter(label = "Save to")
	private File saveTo;

	@SuppressWarnings("unused")
	@Parameter(label = "Show result in ImageJ window")
	private boolean showResult;

	@SuppressWarnings("unused")
	@Parameter
	private ProjectModel projectModel;

	@Override
	public void run()
	{
		ExportLabelImageController controller = new ExportLabelImageController( projectModel, getContext() );
		LabelOptions selectedOption = LabelOptions.getByName( option );
		controller.saveLabelImageToFile( selectedOption, saveTo, showResult, frameRateReduction, resolutionLevel );
	}

	@SuppressWarnings( "unused" )
	private void initResolutionMinMax()
	{
		int mipMapLevels = projectModel.getSharedBdvData().getSources().get( ExportLabelImageController.DEFAULT_SOURCE_ID ).getSpimSource()
				.getNumMipmapLevels();
		getInfo().getMutableInput( "resolutionLevel", Integer.class ).setMinimumValue( 0 );
		getInfo().getMutableInput( "resolutionLevel", Integer.class ).setMaximumValue( mipMapLevels - 1 );
	}
}
