package org.mastodon.mamut.io.exporter.labelimage.ui;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.exporter.labelimage.ExportLabelImageController;
import org.mastodon.mamut.io.exporter.labelimage.config.LabelOptions;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, label = "Run export label image using ellipsoids")
public class ExportLabelImageView implements Command
{
	private static final int WIDTH = 15;

	@SuppressWarnings("all")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false)
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Export label image using ellipsoids</h1>\n"
			+ "<p>This plugin is capable of saving a label image to a file using the existing ellipsoids in Mastodon.</p>\n"
			+ "<p>For the labels, the <i>spot ids</i>, <i>branch spot ids</i> or the <i>track ids</i> that correspond to the ellipsoids may be used. Since these Ids are counted zero based in Mastodon, an <b>offset of 1</b> is added to all Ids so that no label clashes with the background of zero.</p>\n"
			+ "<p>The recommended export format is to '*.tif'-files. However, it should work also for other formats supported by ImageJ.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings("all")
	@Parameter(label = "Label Id", choices = { "Track Id", "BranchSpot Id", "Spot Id" })
	private String option = LabelOptions.BRANCH_SPOT_ID.getName();

	@SuppressWarnings("all")
	@Parameter(label = "Frame rate reduction", description = "Only export every n-th. 1 means no reduction. Value must be >= 1.", min = "1")
	private int frameRateReduction = 1;

	@SuppressWarnings("unused")
	@Parameter(label = "Save to")
	private File saveTo;

	@SuppressWarnings("unused")
	@Parameter(label = "Show result in ImageJ window")
	private boolean showResult;

	@SuppressWarnings("unused")
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings("unused")
	@Parameter
	private Context context;

	@Override
	public void run()
	{
		ExportLabelImageController controller = new ExportLabelImageController( projectModel, context );
		LabelOptions selectedOption = LabelOptions.getByName( option );
		controller.saveLabelImageToFile( selectedOption, saveTo, showResult, frameRateReduction );
	}
}
