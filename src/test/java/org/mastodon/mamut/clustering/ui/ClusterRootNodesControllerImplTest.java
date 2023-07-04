package org.mastodon.mamut.clustering.ui;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

public class ClusterRootNodesControllerImplTest
{
	@Test
	public void testCreateTagSet()
	{
		Model model = new Model();

		ClusterRootNodesController< BranchSpotTree > controller = new ClusterRootNodesControllerImpl( model );
		controller.createTagSet();

	}
}
