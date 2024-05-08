package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

public class DendrogramViewDemo
{
	public static void main( final String[] args )
	{
		Model model = new Model();
		try (Context context = new Context())
		{
			model.getTagSetModel().getTagSetStructure().createTagSet( "TagSet1" );
			model.getTagSetModel().getTagSetStructure().createTagSet( "TagSet2" );
			final DendrogramView< String > view =
					new DendrogramView<>( ClusterData.createSampleClassification3(), "Classification of lineages", model,
							context.getService( PrefService.class ) );
			view.show();
		}
	}
}
