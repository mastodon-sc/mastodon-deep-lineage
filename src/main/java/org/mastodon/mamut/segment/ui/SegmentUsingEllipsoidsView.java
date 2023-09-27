package org.mastodon.mamut.segment.ui;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.segment.SegmentUsingEllipsoidsController;
import org.mastodon.mamut.segment.config.LabelOptions;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, label = "Run segmentation using ellipsoids")
public class SegmentUsingEllipsoidsView implements Command
{
	private static final int WIDTH = 15;

	@SuppressWarnings("all")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false)
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Segmentation using ellipsoids</h1>\n"
			+ "<p>This plugin is capable of saving a label image to a file using the existing ellipsoids in Mastodon.</p>\n"
			+ "<p>For the labels, the <i>spot ids</i>, <i>branch spot ids</i> or the <i>track ids</i> that correspond to the ellipsoids may be used. Since these Ids are counted zero based in Mastodon, an <b>offset of 1</b> is added to all Ids.</p>\n"
			+ "<p>The recommended export format is to '*.tif'-files. However, it should work also for other formats supported by ImageJ.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings("unused")
	@Parameter(label = "Label Id", choices = { "Track Id", "BranchSpot Id", "Spot Id" })
	private String option;

	@SuppressWarnings("unused")
	@Parameter(label = "Save to")
	private File saveTo;

	@SuppressWarnings("unused")
	@Parameter(label = "Show result in ImageJ window")
	private boolean showResult;

	@SuppressWarnings("unused")
	@Parameter
	private MamutAppModel appModel;

	@SuppressWarnings("unused")
	@Parameter
	private Context context;

	@Override
	public void run()
	{
		SegmentUsingEllipsoidsController controller = new SegmentUsingEllipsoidsController( appModel, context );
		LabelOptions selectedOption = LabelOptions.getByName( option );
		controller.saveEllipsoidSegmentationToFile( selectedOption, saveTo, showResult );
	}
}
