package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import org.junit.Test;
import org.mastodon.mamut.clustering.ui.DendrogramView;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

public class DendrogramViewTest
{
	@Test
	public void testGetPanel()
	{
		Cluster cluster = new Cluster( "test" );
		DendrogramView< BranchSpotTree > dendrogramView = new DendrogramView<>( cluster, new HashMap<>(), 0d, "test" );
		DendrogramView< BranchSpotTree > dendrogramViewNull = new DendrogramView<>( null, null, 0d, "test" );
		assertNotNull( dendrogramView );
		assertNotNull( dendrogramView.getPanel() );
		assertNotNull( dendrogramViewNull );
		assertNotNull( dendrogramViewNull.getPanel() );
	}
}
