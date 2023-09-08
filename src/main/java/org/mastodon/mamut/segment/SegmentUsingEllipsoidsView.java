package org.mastodon.mamut.segment;

import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

import java.io.File;

public class SegmentUsingEllipsoidsView implements Command, Initializable
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
