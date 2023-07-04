package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.Arrays;
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

	@Test
	public void testGetDistanceMatrix()
	{
		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		Tree< Double > tree2 = SimpleTreeExamples.tree2();
		Tree< Double > tree3 = SimpleTreeExamples.tree3();

		double t1t2 = 20d;
		double t1t3 = 92d;
		double t2t3 = 104d;

		double[][] distanceMatrix =
				ClusterUtils.getDistanceMatrix( Arrays.asList( tree1, tree2, tree3 ), SimilarityMeasure.ABSOLUTE_DIFFERENCE );
		assertArrayEquals( new double[] { 0, t1t2, t1t3 }, distanceMatrix[ 0 ], 0d );
		assertArrayEquals( new double[] { t1t2, 0, t2t3 }, distanceMatrix[ 1 ], 0d );
		assertArrayEquals( new double[] { t1t3, t2t3, 0 }, distanceMatrix[ 2 ], 0d );

		t1t2 = 20d / 6d;
		t1t3 = 92d / 8d;
		t2t3 = 104d / 8d;

		distanceMatrix = ClusterUtils.getDistanceMatrix( Arrays.asList( tree1, tree2, tree3 ),
				SimilarityMeasure.AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE );
		assertArrayEquals( new double[] { 0, t1t2, t1t3 }, distanceMatrix[ 0 ], 0d );
		assertArrayEquals( new double[] { t1t2, 0, t2t3 }, distanceMatrix[ 1 ], 0d );
		assertArrayEquals( new double[] { t1t3, t2t3, 0 }, distanceMatrix[ 2 ], 0d );

		t1t2 = 20d / 120d;
		t1t3 = 92d / 164d;
		t2t3 = 104d / 164d;
		distanceMatrix = ClusterUtils.getDistanceMatrix( Arrays.asList( tree1, tree2, tree3 ),
				SimilarityMeasure.NORMALIZED_DIFFERENCE );
		assertArrayEquals( new double[] { 0, t1t2, t1t3 }, distanceMatrix[ 0 ], 0d );
		assertArrayEquals( new double[] { t1t2, 0, t2t3 }, distanceMatrix[ 1 ], 0d );
		assertArrayEquals( new double[] { t1t3, t2t3, 0 }, distanceMatrix[ 2 ], 0d );
	}
}
