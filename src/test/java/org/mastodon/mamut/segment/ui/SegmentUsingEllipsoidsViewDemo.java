package org.mastodon.mamut.segment.ui;

import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class SegmentUsingEllipsoidsViewDemo
{

	public static void main( String[] args )
	{
		@SuppressWarnings("all")
		Context context = new Context();
		UIService ui = context.service( UIService.class );
		CommandService cmd = context.service( CommandService.class );

		ui.showUI();
		cmd.run( SegmentUsingEllipsoidsView.class, true, "projectModel", null );
	}
}
