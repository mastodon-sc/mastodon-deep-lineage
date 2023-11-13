package org.mastodon.mamut.io.importer.labelimage;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.io.importer.labelimage.ui.ImportSpotsFromLabelImageView;
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
public class ImportSpotsFromLabelImagePlugin implements MamutPlugin
{
	private static final String IMPORT_SPOTS_FROM_LABEL_IMAGE = "Import spots from label image";

	private static final String[] IMPORT_SPOTS_FROM_LABELS_IMAGE_J_KEYS = { "not mapped" };

	private final AbstractNamedAction action;

	private ProjectModel appModel;

	@SuppressWarnings("unused")
	@Parameter
	private CommandService commandService;

	@SuppressWarnings("unused")
	public ImportSpotsFromLabelImagePlugin()
	{
		action = new RunnableAction( IMPORT_SPOTS_FROM_LABEL_IMAGE, this::importSpotsFromLabelImage );
	}

	@Override
	public void setAppPluginModel( final ProjectModel appPluginModel )
	{
		this.appModel = appPluginModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( IMPORT_SPOTS_FROM_LABEL_IMAGE ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( action, IMPORT_SPOTS_FROM_LABELS_IMAGE_J_KEYS );
	}

	private void importSpotsFromLabelImage()
	{
		commandService.run( ImportSpotsFromLabelImageView.class, true, "appModel", appModel );
	}
}
