package org.mastodon.mamut.clustering;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

public class ClusterUtilsTest
{

	@Test
	public void testGetClustersByThreshold()
	{
		Map< Integer, List< String > > leavesToClusterIds =
				ClusterUtils.getClustersByThreshold( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(), 50 );
		assertArrayEquals( new String[] { "F" }, leavesToClusterIds.get( 0 ).toArray() );
		assertArrayEquals( new String[] { "A", "B", "E", "G", "H" }, leavesToClusterIds.get( 1 ).toArray() );
		assertArrayEquals( new String[] { "C", "D", "I", "J" }, leavesToClusterIds.get( 2 ).toArray() );
	}

	@Test
	public void testGetClustersByClassCount()
	{
		Map< Integer, List< String > > leavesToClusterIds =
				ClusterUtils.getClustersByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(), 3 );
		assertArrayEquals( new String[] { "F" }, leavesToClusterIds.get( 0 ).toArray() );
		assertArrayEquals( new String[] { "A", "B", "E", "G", "H" }, leavesToClusterIds.get( 1 ).toArray() );
		assertArrayEquals( new String[] { "C", "D", "I", "J" }, leavesToClusterIds.get( 2 ).toArray() );
	}
}
