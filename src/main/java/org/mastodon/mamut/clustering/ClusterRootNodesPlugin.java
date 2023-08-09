package org.mastodon.mamut.clustering;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesView;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import java.util.Collections;
import java.util.List;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = MamutPlugin.class )
public class ClusterRootNodesPlugin implements MamutPlugin
{
	private static final String CLUSTER_ROOT_NODES = "Cluster root nodes";

	private static final String[] CLUSTER_ROOT_NODES_KEYS = { "not mapped" };

	private final AbstractNamedAction cluserRootNodesAction;

	private Model model;

	public ClusterRootNodesPlugin()
	{
		cluserRootNodesAction = new RunnableAction( CLUSTER_ROOT_NODES, this::clusterRootNodes );
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel pluginAppModel )
	{
		this.model = pluginAppModel.getAppModel().getModel();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList( menu( "Plugins", item( CLUSTER_ROOT_NODES ) ) );
	}

	@Override
	public void installGlobalActions( Actions actions )
	{
		actions.namedAction( cluserRootNodesAction, CLUSTER_ROOT_NODES_KEYS );
	}

	private void clusterRootNodes()
	{
		try (Context context = new Context())
		{
			UIService ui = context.service( UIService.class );
			CommandService cmd = context.service( CommandService.class );

			ui.showUI();
			cmd.run( ClusterRootNodesView.class, true, "controller", new ClusterRootNodesController( model ) );
		}
	}
}