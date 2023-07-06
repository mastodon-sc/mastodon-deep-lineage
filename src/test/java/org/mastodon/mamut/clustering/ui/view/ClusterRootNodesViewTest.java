package org.mastodon.mamut.clustering.ui.view;

import org.junit.Test;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesController;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesControllerImpl;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

import static org.junit.Assert.assertNotNull;

public class ClusterRootNodesViewTest
{

	@Test
	public void testClusterRootNodesView()
	{
		Model model = new Model();
		ClusterRootNodesController< BranchSpotTree > controller = new ClusterRootNodesControllerImpl( model );
		ClusterRootNodesView< BranchSpotTree > clusterRootNodesView = new ClusterRootNodesView<>( controller );
		assertNotNull( clusterRootNodesView );
	}
}
