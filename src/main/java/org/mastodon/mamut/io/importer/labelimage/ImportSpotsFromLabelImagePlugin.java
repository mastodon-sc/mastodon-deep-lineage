/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.io.importer.labelimage;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.ui.ImportSpotsFromImgPlusView;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.io.importer.labelimage.ui.ImportSpotsFromBdvChannelView;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import java.util.Collections;
import java.util.List;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@SuppressWarnings( "unused" )
@Plugin( type = MamutPlugin.class )
public class ImportSpotsFromLabelImagePlugin extends AbstractContextual implements MamutPlugin
{
	private static final String IMPORT_SPOTS_FROM_IMAGEJ = "Import spots from ImageJ image";

	private static final String IMPORT_SPOTS_FROM_BDV_CHANNEL = "Import spots from BDV channel";

	private static final String[] IMPORT_SPOTS_FROM_IMAGEJ_KEYS = { "ctrl shift I" };

	private static final String[] IMPORT_SPOTS_FROM_BDV_CHANNEL_KEYS = { "ctrl alt I" };

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
		return Collections.singletonList( MamutMenuBuilder.fileMenu( menu( "Import",
				menu( "Import spots from label image", item( IMPORT_SPOTS_FROM_IMAGEJ ), item( IMPORT_SPOTS_FROM_BDV_CHANNEL ) ) ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( imageJImport, IMPORT_SPOTS_FROM_IMAGEJ_KEYS );
		actions.namedAction( bdvChannelImport, IMPORT_SPOTS_FROM_BDV_CHANNEL_KEYS );
	}

	private void importSpotsFromImageJ()
	{
		commandService.run( ImportSpotsFromImgPlusView.class, true, "projectModel", appModel );
	}

	private void importSpotsFromBdvChannel()
	{
		commandService.run( ImportSpotsFromBdvChannelView.class, true, "projectModel", appModel );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( IMPORT_SPOTS_FROM_IMAGEJ, IMPORT_SPOTS_FROM_IMAGEJ_KEYS,
					"Import spots from a label image opened in ImageJ." );
			descriptions.add( IMPORT_SPOTS_FROM_BDV_CHANNEL, IMPORT_SPOTS_FROM_BDV_CHANNEL_KEYS,
					"Import spots from a channel in BigDataViewer that contains a segmentation to labels." );
		}
	}
}
