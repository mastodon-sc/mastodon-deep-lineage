package org.mastodon.mamut.segment.ui;

import org.mastodon.mamut.segment.SegmentUsingEllipsoidsController;
import org.mastodon.mamut.segment.config.LabelOptions;
import org.mastodon.mamut.segment.config.SegmentationTask;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, label = "Run segmentation using ellipsoids")
public class SegmentUsingEllipsoidsView implements Command
{

	@SuppressWarnings("unused")
	@Parameter
	private SegmentUsingEllipsoidsController controller;

	@SuppressWarnings("unused")
	@Parameter(label = "Task", choices = { "Save segmentation result to file", "Show segmentation in BigDataViewer" })
	private String task;

	@SuppressWarnings("unused")
	@Parameter(label = "Label Id", choices = { "BranchSpot Id", "Spot Id" })
	private String option;

	@SuppressWarnings("unused")
	@Parameter(label = "Save to")
	private File saveTo;

	@SuppressWarnings("unused")
	@Parameter(label = "Show result")
	private boolean showResult;

	@SuppressWarnings("unused")
	@Parameter(label = "Keep background")
	private boolean withBackground;

	@Override
	public void run()
	{
		LabelOptions selectedOption = LabelOptions.getByName( option );
		SegmentationTask selectedTask = SegmentationTask.getByName( task );
		if ( selectedTask.equals( SegmentationTask.SEGMENT_ELLIPSOIDS_BDV ) )
			controller.showEllipsoidSegmentationInBDV( selectedOption );
		else
			controller.saveEllipsoidSegmentationToFile( selectedOption, saveTo, showResult, withBackground );
	}
}
