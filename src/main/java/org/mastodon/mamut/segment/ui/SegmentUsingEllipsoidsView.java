package org.mastodon.mamut.segment.ui;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.segment.SegmentUsingEllipsoidsController;
import org.mastodon.mamut.segment.config.LabelOptions;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, label = "Run segmentation using ellipsoids")
public class SegmentUsingEllipsoidsView implements Command
{
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
