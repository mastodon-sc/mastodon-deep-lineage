package org.mastodon.mamut.segment.ui;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.segment.ImportSpotFromLabelsController;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, label = "Import spots from labels")
public class ImportSpotsFromLabelsView implements Command
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
	private MamutAppModel appModel;

	@SuppressWarnings("unused")
	@Parameter
	private Context context;

	@Parameter(label = "Channel index of labels", min = "0")
	private int labelChannelIndex = 0;

	@Override
	public void run()
	{
		ImportSpotFromLabelsController controller = new ImportSpotFromLabelsController( appModel, context, labelChannelIndex );
		controller.createSpotsFromLabels();
	}

}
