package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;

public class DendrogramViewTest
{
	@Test
	public void testGetPanel()
	{
		Cluster cluster = new Cluster( "test" );
		Classification< BranchSpotTree > classification =
				new Classification<>( Collections.singletonList( Pair.of( null, cluster ) ), null, null, 0d );
		DendrogramView< BranchSpotTree > dendrogramView = new DendrogramView<>( classification, "test" );
		DendrogramView< BranchSpotTree > dendrogramViewNull = new DendrogramView<>( null, "test" );
		assertNotNull( dendrogramView );
		assertNotNull( dendrogramView.getPanel() );
		assertNotNull( dendrogramViewNull );
		assertNotNull( dendrogramViewNull.getPanel() );
	}
}
