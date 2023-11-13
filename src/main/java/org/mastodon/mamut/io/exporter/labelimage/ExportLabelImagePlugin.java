package org.mastodon.mamut.io.exporter.labelimage;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.io.exporter.labelimage.ui.ExportLabelImageView;
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
public class ExportLabelImagePlugin implements MamutPlugin
{
	private static final String EXPORT_LABEL_IMAGE_USING = "Export label image using ellipsoids";

	private static final String[] LABEL_ELLIPSOIDS_IMAGE_J_KEYS = { "not mapped" };

	private final AbstractNamedAction action;

	private ProjectModel projectModel;

	@SuppressWarnings("unused")
	@Parameter
	private CommandService commandService;


	@SuppressWarnings("unused")
	public ExportLabelImagePlugin()
	{
		action = new RunnableAction( EXPORT_LABEL_IMAGE_USING, this::exportLabelImage );
	}

	@Override
	public void setAppPluginModel( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList(
				menu( "Plugins", menu( "Exports", item( EXPORT_LABEL_IMAGE_USING ) ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( action, LABEL_ELLIPSOIDS_IMAGE_J_KEYS );
	}

	private void exportLabelImage()
	{
		commandService.run( ExportLabelImageView.class, true, "projectModel", projectModel );
	}
}
