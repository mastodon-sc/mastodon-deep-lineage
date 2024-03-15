package org.mastodon.mamut.io.importer.labelimage;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.ui.ImportSpotsFromImgPlusView;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.io.importer.labelimage.ui.ImportSpotsFromBdvChannelView;
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

@SuppressWarnings( "unused" )
@Plugin( type = MamutPlugin.class )
public class ImportSpotsFromLabelImagePlugin implements MamutPlugin
{
	private static final String IMPORT_SPOTS_FROM_IMAGEJ = "Import spots from ImageJ image";

	private static final String IMPORT_SPOTS_FROM_BDV_CHANNEL = "Import spots from BDV channel";

	private static final String[] SHORT_CUTS = { "not mapped" };

	private final AbstractNamedAction imageJImport;

	private final AbstractNamedAction bdvChannelImport;

	private ProjectModel appModel;

	@SuppressWarnings( "unused" )
	@Parameter
	private CommandService commandService;

	@SuppressWarnings( "unused" )
	public ImportSpotsFromLabelImagePlugin()
	{
		imageJImport = new RunnableAction( IMPORT_SPOTS_FROM_IMAGEJ, this::importSpotsFromImageJ );
		bdvChannelImport = new RunnableAction( IMPORT_SPOTS_FROM_BDV_CHANNEL, this::importSpotsFromBdvChannel );
	}

	@Override
	public void setAppPluginModel( final ProjectModel appPluginModel )
	{
		this.appModel = appPluginModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList(
				menu( "Plugins", menu( "Imports",
						menu( "Import spots from label image", item( IMPORT_SPOTS_FROM_IMAGEJ ),
								item( IMPORT_SPOTS_FROM_BDV_CHANNEL ) ) ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( imageJImport, SHORT_CUTS );
		actions.namedAction( bdvChannelImport, SHORT_CUTS );
	}

	private void importSpotsFromImageJ()
	{
		commandService.run( ImportSpotsFromImgPlusView.class, true, "projectModel", appModel );
	}

	private void importSpotsFromBdvChannel()
	{
		commandService.run( ImportSpotsFromBdvChannelView.class, true, "projectModel", appModel );
	}

}
