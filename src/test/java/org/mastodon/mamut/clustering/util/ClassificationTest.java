package org.mastodon.mamut.clustering.util;

import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;

import static org.junit.Assert.assertEquals;

public class ClassificationTest
{
	@Test
	public void testGetMedian()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.example1.getKey(),
				ClusterData.example1.getValue(), new AverageLinkageUPGMAStrategy(), 3 );
		assertEquals( 51, classification.getMedian(), 0d );
	}
}
