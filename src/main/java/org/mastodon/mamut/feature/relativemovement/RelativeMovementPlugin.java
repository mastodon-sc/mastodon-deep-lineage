/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.plugin.MamutPlugin;
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
public class RelativeMovementPlugin implements MamutPlugin
{
	private static final String DESCRIPTION = "Movement of spots relative to nearest neighbors";

	private static final String[] IMAGE_J_KEYS = { "not mapped" };

	private final AbstractNamedAction action;

	private ProjectModel projectModel;

	@SuppressWarnings( "unused" )
	@Parameter
	private CommandService commandService;

	@SuppressWarnings( "unused" )
	public RelativeMovementPlugin()
	{
		action = new RunnableAction( DESCRIPTION, this::showRelativeMovementDialog );
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
				menu( "Plugins", menu( "Compute", item( DESCRIPTION ) ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( action, IMAGE_J_KEYS );
	}

	private void showRelativeMovementDialog()
	{
		commandService.run( RelativeMovementView.class, true, "model", projectModel.getModel(), "context", projectModel.getContext() );
	}
}
