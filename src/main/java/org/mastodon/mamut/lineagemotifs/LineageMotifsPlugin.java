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
package org.mastodon.mamut.lineagemotifs;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.util.Collections;
import java.util.List;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.lineagemotifs.ui.FindLineageMotifsCommand;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.AbstractContextual;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@SuppressWarnings( "unused" )
@Plugin( type = MamutPlugin.class )
public class LineageMotifsPlugin extends AbstractContextual implements MamutPlugin
{
	private static final String FIND_LINEAGE_MOTIFS = "Find lineage motifs";

	private static final String[] FIND_LINEAGE_MOTIFS_KEYS = { "ctrl shift L" };

	private final AbstractNamedAction findLineageMotifsAction;

	private ProjectModel projectModel;

	@SuppressWarnings( "unused" )
	@Parameter
	private CommandService commandService;

	@SuppressWarnings( "unused" )
	@Parameter
	private PrefService prefService;

	@SuppressWarnings( "unused" )
	public LineageMotifsPlugin()
	{
		findLineageMotifsAction = new RunnableAction( FIND_LINEAGE_MOTIFS, this::findLineageMotifs );
	}

	@Override
	public void setAppPluginModel( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", menu( "Lineage analysis", item( FIND_LINEAGE_MOTIFS ) ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( findLineageMotifsAction, FIND_LINEAGE_MOTIFS_KEYS );
	}

	private void findLineageMotifs()
	{
		commandService.run( FindLineageMotifsCommand.class, true, "projectModel", projectModel );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( FIND_LINEAGE_MOTIFS, FIND_LINEAGE_MOTIFS_KEYS, "Find lineage Motifs that are similar to a selected motif." );
		}
	}
}
