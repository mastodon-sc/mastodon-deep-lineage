package org.mastodon.mamut.clustering;

import org.mastodon.mamut.clustering.ui.view.ClusterRootNodesView;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

public class ClusterRootNodesViewDemo
{

	public static void main( String... args )
	{
		ClusterRootNodesView< BranchSpotTree > dialog = new ClusterRootNodesView<>( null );
		dialog.pack();
		dialog.setVisible( true );
	}
}
