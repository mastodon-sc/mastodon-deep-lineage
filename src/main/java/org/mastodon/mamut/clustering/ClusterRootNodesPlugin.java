package org.mastodon.mamut.clustering;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesControllerImpl;
import org.mastodon.mamut.clustering.ui.view.ClusterRootNodesView;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = MamutPlugin.class )
public class ClusterRootNodesPlugin implements MamutPlugin
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String CLUSTER_ROOT_NODES = "Cluster root nodes";

	private static final String[] CLUSTER_ROOT_NODES_KEYS = { "not mapped" };

	private MamutPluginAppModel pluginAppModel = null;

	private final AbstractNamedAction cluserRootNodesAction;

	public ClusterRootNodesPlugin()
	{
		cluserRootNodesAction = new RunnableAction( CLUSTER_ROOT_NODES, this::showClusterRootNodesView );
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel model )
	{
		this.pluginAppModel = model;
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

	private void showClusterRootNodesView()
	{
		if ( pluginAppModel != null )
			new ClusterRootNodesView<>( new ClusterRootNodesControllerImpl( pluginAppModel.getAppModel().getModel() ) ).setVisible( true );
	}
}
