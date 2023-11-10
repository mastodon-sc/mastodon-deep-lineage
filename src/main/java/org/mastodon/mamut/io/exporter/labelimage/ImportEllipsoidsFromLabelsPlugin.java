package org.mastodon.mamut.segment;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.segment.ui.ImportSpotsFromLabelsView;
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
public class ImportEllipsoidsFromLabelsPlugin implements MamutPlugin
{
	private static final String IMPORT_SPOTS_FROM_LABELS = "Import spots from labels";

	private static final String[] IMPORT_SPOTS_FROM_LABELS_IMAGE_J_KEYS = { "not mapped" };

	private final AbstractNamedAction importSpotsFromLabels;

	private ProjectModel appModel;

	@SuppressWarnings("unused")
	@Parameter
	private CommandService commandService;

	@SuppressWarnings("unused")
	public ImportEllipsoidsFromLabelsPlugin()
	{
		importSpotsFromLabels = new RunnableAction( IMPORT_SPOTS_FROM_LABELS, this::importSpotsFromLabels );
	}

	@Override
	public void setAppPluginModel( final ProjectModel appPluginModel )
	{
		this.appModel = appPluginModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( IMPORT_SPOTS_FROM_LABELS ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( importSpotsFromLabels, IMPORT_SPOTS_FROM_LABELS_IMAGE_J_KEYS );
	}

	private void importSpotsFromLabels()
	{
		commandService.run( ImportSpotsFromLabelsView.class, true, "appModel", appModel );
	}
}
