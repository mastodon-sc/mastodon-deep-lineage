package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.ClusterData;

public class DendrogramViewDemo
{
	public static void main( final String[] args )
	{
		final DendrogramView< String > view =
				new DendrogramView<>( ClusterData.createSampleClassification3(), "Classification of lineages" );
		view.show();
	}
}
