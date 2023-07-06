package org.mastodon.mamut.clustering;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesView;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.scijava.plugin.Plugin;
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

	private ClusterRootNodesView view;

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
		ClusterRootNodesController clusterRootNodesController = new ClusterRootNodesController( model );
		if ( view == null )
			view = new ClusterRootNodesView( clusterRootNodesController );
		if ( view.isVisible() )
		{
			view.toFront();
			return;
		}
		view.pack();
		view.setVisible( true );
	}
}
