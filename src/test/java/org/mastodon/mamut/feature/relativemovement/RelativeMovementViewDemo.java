package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.ui.UIService;

public class RelativeMovementViewDemo
{
	public static void main( String[] args )
	{
		@SuppressWarnings( "all" )
		Context context = new Context();
		UIService ui = context.service( UIService.class );
		CommandService cmd = context.service( CommandService.class );

		ui.showUI();
		cmd.run( RelativeMovementView.class, true, "model", new Model(), "context", context );
	}
}
