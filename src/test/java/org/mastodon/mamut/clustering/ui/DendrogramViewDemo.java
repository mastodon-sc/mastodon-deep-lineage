package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.model.Model;

public class DendrogramViewDemo
{
	public static void main( final String[] args )
	{
		Model model = new Model();
		final DendrogramView< String > view =
				new DendrogramView<>( ClusterData.createSampleClassification3(), "Classification of lineages", model );
		view.show();
	}
}
