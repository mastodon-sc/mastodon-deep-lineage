package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class ClusterUtilsTest
{

	@Test
	public void testGetClassificationByThreshold()
	{
		double threshold = 60d;
		Classification< String > classification =
				ClusterUtils.getClassificationByThreshold( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(),
						threshold );
		Map< Integer, List< String > > classifiedObjects = classification.getClassifiedObjects();
		Map< String, String > objectMapping = classification.getObjectMapping();

		assertArrayEquals( new String[] { "F" }, classifiedObjects.get( 0 ).toArray() );
		assertArrayEquals( new String[] { "A", "B", "E", "G", "H" }, classifiedObjects.get( 1 ).toArray() );
		assertArrayEquals( new String[] { "C", "D", "I", "J" }, classifiedObjects.get( 2 ).toArray() );
		assertEquals( threshold, classification.getCutoff(), 0d );
		assertNotNull( objectMapping );
		assertEquals( ClusterData.names.length, objectMapping.size() );
		assertNotNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testGetClassificationByClassCount()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances,
				new AverageLinkageStrategy(), 3 );
		Map< Integer, List< String > > classifiedObjects = classification.getClassifiedObjects();
		Map< String, String > objectMapping = classification.getObjectMapping();
		double cutoff = classification.getCutoff();

		assertArrayEquals( new String[] { "F" }, classifiedObjects.get( 0 ).toArray() );
		assertArrayEquals( new String[] { "A", "B", "E", "G", "H" }, classifiedObjects.get( 1 ).toArray() );
		assertArrayEquals( new String[] { "C", "D", "I", "J" }, classifiedObjects.get( 2 ).toArray() );
		assertNotNull( objectMapping );
		assertEquals( ClusterData.names.length, objectMapping.size() );
		assertEquals( 61.5d, cutoff, 0d );
		assertNotNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testGetDistanceMatrix()
	{
		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		Tree< Double > tree2 = SimpleTreeExamples.tree2();
		Tree< Double > tree3 = SimpleTreeExamples.tree3();

		double t1t2 = 20d;
		double t1t3 = 100d;
		double t2t3 = 104d;

		double[][] distanceMatrix =
				ClusterUtils.getDistanceMatrix( Arrays.asList( tree1, tree2, tree3 ), SimilarityMeasure.ABSOLUTE_DIFFERENCE );
		assertArrayEquals( new double[] { 0, t1t2, t1t3 }, distanceMatrix[ 0 ], 0d );
		assertArrayEquals( new double[] { t1t2, 0, t2t3 }, distanceMatrix[ 1 ], 0d );
		assertArrayEquals( new double[] { t1t3, t2t3, 0 }, distanceMatrix[ 2 ], 0d );

		t1t2 = 20d / 6d;
		t1t3 = 100d / 8d;
		t2t3 = 104d / 8d;

		distanceMatrix = ClusterUtils.getDistanceMatrix( Arrays.asList( tree1, tree2, tree3 ),
				SimilarityMeasure.AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE );
		assertArrayEquals( new double[] { 0, t1t2, t1t3 }, distanceMatrix[ 0 ], 0d );
		assertArrayEquals( new double[] { t1t2, 0, t2t3 }, distanceMatrix[ 1 ], 0d );
		assertArrayEquals( new double[] { t1t3, t2t3, 0 }, distanceMatrix[ 2 ], 0d );

		t1t2 = 20d / 120d;
		t1t3 = 100d / 164d;
		t2t3 = 104d / 164d;
		distanceMatrix = ClusterUtils.getDistanceMatrix( Arrays.asList( tree1, tree2, tree3 ),
				SimilarityMeasure.NORMALIZED_DIFFERENCE );
		assertArrayEquals( new double[] { 0, t1t2, t1t3 }, distanceMatrix[ 0 ], 0d );
		assertArrayEquals( new double[] { t1t2, 0, t2t3 }, distanceMatrix[ 1 ], 0d );
		assertArrayEquals( new double[] { t1t3, t2t3, 0 }, distanceMatrix[ 2 ], 0d );
	}

	@Test
	public void testExceptions()
	{
		LinkageStrategy linkageStrategy = new AverageLinkageStrategy();
		// zero classes
		assertThrows( IllegalArgumentException.class,
				() -> ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, linkageStrategy, 0 ) );
		// too many classes
		assertThrows( IllegalArgumentException.class,
				() -> ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, linkageStrategy, 11 ) );
		// negative threshold
		assertThrows( IllegalArgumentException.class,
				() -> ClusterUtils.getClassificationByThreshold( ClusterData.names, ClusterData.fixedDistances, linkageStrategy, -1 ) );
	}

	@Test
	public void testOneClass()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances,
				new AverageLinkageStrategy(), 1 );
		Map< Integer, List< String > > classifiedObjects = classification.getClassifiedObjects();
		assertArrayEquals( new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" }, classifiedObjects.get( 0 ).toArray() );
		assertNull( classification.getObjectMapping() );
		assertNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testBasicExample()
	{
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(), 10 );
		Map< Integer, List< String > > classifiedObjects = classification.getClassifiedObjects();
		assertArrayEquals( new String[] { "A" }, classifiedObjects.get( 0 ).toArray() );
		assertArrayEquals( new String[] { "B" }, classifiedObjects.get( 1 ).toArray() );
		assertArrayEquals( new String[] { "C" }, classifiedObjects.get( 2 ).toArray() );
		assertArrayEquals( new String[] { "D" }, classifiedObjects.get( 3 ).toArray() );
		assertArrayEquals( new String[] { "E" }, classifiedObjects.get( 4 ).toArray() );
		assertArrayEquals( new String[] { "F" }, classifiedObjects.get( 5 ).toArray() );
		assertArrayEquals( new String[] { "G" }, classifiedObjects.get( 6 ).toArray() );
		assertArrayEquals( new String[] { "H" }, classifiedObjects.get( 7 ).toArray() );
		assertArrayEquals( new String[] { "I" }, classifiedObjects.get( 8 ).toArray() );
		assertArrayEquals( new String[] { "J" }, classifiedObjects.get( 9 ).toArray() );
		assertNull( classification.getObjectMapping() );
		assertNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testTrivialExample()
	{
		String[] names = { "1", "2", "3" };
		double[][] distances = new double[][] {
				{ 0, 2, 4 },
				{ 2, 0, 8 },
				{ 4, 8, 0 }
		};
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( names, distances, new AverageLinkageStrategy(), 2 );
		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );
		Map< Integer, List< String > > classifiedObjects = classification.getClassifiedObjects();
		assertNotNull( classification.getObjectMapping() );
		assertNotNull( cluster );
		assertArrayEquals( new String[] { "3" }, classifiedObjects.get( 0 ).toArray() );
		assertArrayEquals( new String[] { "1", "2" }, classifiedObjects.get( 1 ).toArray() );
		assertEquals( 6, cluster.getDistanceValue(), 0d );
		assertEquals( 6, cluster.getDistance().getDistance(), 0d );
		assertEquals( 6, cluster.getTotalDistance(), 0d );
		assertEquals( 0, child0.getDistanceValue(), 0d );
		assertEquals( 2, child1.getDistanceValue(), 0d );
		assertEquals( 2, child1.getChildren().size() );
	}

	@Test
	public void testAverageLinkageClustering()
	{

	}
}
