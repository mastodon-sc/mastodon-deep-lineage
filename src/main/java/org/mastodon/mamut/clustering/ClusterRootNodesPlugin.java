/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 N/A
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
package org.mastodon.mamut.clustering;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesView;
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

@SuppressWarnings("unused")
@Plugin( type = MamutPlugin.class )
public class ClusterRootNodesPlugin implements MamutPlugin
{
	private static final String CLUSTER_ROOT_NODES = "Classification of Lineage Trees";

	private static final String[] CLUSTER_ROOT_NODES_KEYS = { "not mapped" };

	private final AbstractNamedAction clusterRootNodesAction;

	private ProjectModel projectModel;

	@SuppressWarnings("unused")
	@Parameter
	private CommandService commandService;

	@SuppressWarnings("unused")
	public ClusterRootNodesPlugin()
	{
		clusterRootNodesAction = new RunnableAction( CLUSTER_ROOT_NODES, this::clusterRootNodes );
	}

	@Override
	public void setAppPluginModel( final ProjectModel projectModel )
	{
		this.projectModel = projectModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( CLUSTER_ROOT_NODES ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( clusterRootNodesAction, CLUSTER_ROOT_NODES_KEYS );
	}

	private void clusterRootNodes()
	{
		ClusterRootNodesController controller =
				new ClusterRootNodesController( projectModel.getModel(), projectModel.getBranchGraphSync() );
		commandService.run( ClusterRootNodesView.class, true, "controller", controller );
	}
}
