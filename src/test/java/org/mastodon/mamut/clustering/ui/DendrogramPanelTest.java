package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DendrogramPanelTest
{
	@Test
	public void testCountZerosAfterDecimalPoint()
	{
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 5 ) );
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 0.1 ) );
		assertEquals( 1, DendrogramPanel.countZerosAfterDecimalPoint( 0.01 ) );
		assertEquals( 2, DendrogramPanel.countZerosAfterDecimalPoint( -0.003 ) );
	}

	@Test
	public void testDendrogramPanel()
	{
		Classification< String > classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(),
						3
				);
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		assertNotNull( dendrogramPanel );
	}
}
