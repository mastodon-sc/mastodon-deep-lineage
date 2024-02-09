/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.io.exporter.spotcounts.ui;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.exporter.spotcounts.ExportSpotCountsController;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin( type = Command.class, label = "Run export total spot counts" )
public class ExportSpotCountsView implements Command
{
	private static final int WIDTH = 15;

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Export total spot counts</h1>\n"
			+ "<p>This plugin writes the timepoint and the total number of spots at each timepoint to a CSV file.</p>\n"
			+ "<p>The format is: \"timepoint\", \"number of spots\".</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings( "unused" )
	@Parameter( label = "Save to" )
	private File saveTo;

	@SuppressWarnings( "unused" )
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings( "unused" )
	@Parameter
	private Context context;

	@Override
	public void run()
	{
		ExportSpotCountsController controller = new ExportSpotCountsController( projectModel, context );
		controller.writeSpotCountsToFile( saveTo );
	}
}
