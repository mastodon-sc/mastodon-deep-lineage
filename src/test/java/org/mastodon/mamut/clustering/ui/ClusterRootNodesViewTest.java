package org.mastodon.mamut.clustering.ui;

import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterRootNodesController;
import org.mastodon.mamut.clustering.ui.ClusterRootNodesView;
import org.mastodon.mamut.model.Model;

import static org.junit.Assert.assertNotNull;

public class ClusterRootNodesViewTest
{

	@Test
	public void testClusterRootNodesView()
	{
		Model model = new Model();
		ClusterRootNodesController controller = new ClusterRootNodesController( model );
		ClusterRootNodesView clusterRootNodesView = new ClusterRootNodesView( controller );
		assertNotNull( clusterRootNodesView );
	}
}
