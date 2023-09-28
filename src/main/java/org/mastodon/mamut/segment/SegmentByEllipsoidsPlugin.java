package org.mastodon.mamut.segment;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.segment.ui.SegmentUsingEllipsoidsView;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import java.util.Collections;
import java.util.List;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@SuppressWarnings("unused")
@Plugin(type = MamutPlugin.class)
public class SegmentByEllipsoidsPlugin implements MamutPlugin
{
	private static final String SEGMENT_USING_ELLIPSOIDS = "Segment using ellipsoids";

	private static final String[] LABEL_ELLIPSOIDS_IMAGE_J_KEYS = { "not mapped" };

	private final AbstractNamedAction segmentUsingEllipsoids;

	private MamutAppModel appModel;

	@SuppressWarnings("unused")
	@Parameter
	private CommandService commandService;


	@SuppressWarnings("unused")
	public SegmentByEllipsoidsPlugin()
	{
		segmentUsingEllipsoids = new RunnableAction( SEGMENT_USING_ELLIPSOIDS, this::segmentUsingEllipsoids );
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel pluginAppModel )
	{
		this.appModel = pluginAppModel.getAppModel();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( SEGMENT_USING_ELLIPSOIDS ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( segmentUsingEllipsoids, LABEL_ELLIPSOIDS_IMAGE_J_KEYS );
	}

	private void segmentUsingEllipsoids()
	{
		commandService.run( SegmentUsingEllipsoidsView.class, true, "appModel", appModel );
	}
}
