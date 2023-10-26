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
