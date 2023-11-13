package org.mastodon.mamut.io.importer.labelimage.ui;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.ImportSpotsFromLabelImageController;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, label = "Import spots from label image")
public class ImportSpotsFromLabelImageView implements Command
{
	private static final int WIDTH = 15;

	@SuppressWarnings("all")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false)
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Import spots from label image</h1>\n"
			+ "<p>This plugin is capable importing spots from a label image.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings("unused")
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings("unused")
	@Parameter
	private Context context;

	@SuppressWarnings("all")
	@Parameter(label = "Channel index of labels", min = "0")
	private int labelChannelIndex = 0;

	@SuppressWarnings("all")
	@Parameter(label = "Sigma", min = "0", description = "#deviations from center to form border")
	private double sigma = 2.2;

	@Override
	public void run()
	{
		ImportSpotsFromLabelImageController controller =
				new ImportSpotsFromLabelImageController( projectModel, context, labelChannelIndex, sigma );
		controller.createSpotsFromLabelImage();
	}

}
