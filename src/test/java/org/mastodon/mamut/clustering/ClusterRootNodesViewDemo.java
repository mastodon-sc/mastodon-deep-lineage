package org.mastodon.mamut.clustering;

import org.mastodon.mamut.clustering.ui.view.ClusterRootNodesView;

public class ClusterRootNodesViewDemo
{

	public static void main( String... args )
	{
		ClusterRootNodesView dialog = new ClusterRootNodesView( null );
		dialog.pack();
		dialog.setVisible( true );
	}
}
