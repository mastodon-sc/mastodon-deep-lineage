package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.Tree;
import org.mastodon.util.ColorUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		double threshold = 57.5d;
		Classification< String > classification =
				ClusterUtils.getClassificationByThreshold( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageUPGMAStrategy(),
						threshold );
		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Map< String, String > objectMapping = classification.getObjectMapping();

		Set< String > class1 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class2 = new HashSet<>( Arrays.asList( "A", "B", "E", "G", "H" ) );
		Set< String > class3 = new HashSet<>( Arrays.asList( "C", "D", "I", "J" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3 ) );
		assertEquals( expectedClasses, classifiedObjects );
		assertEquals( threshold, classification.getCutoff(), 0d );
		assertNotNull( objectMapping );
		assertEquals( ClusterData.names.length, objectMapping.size() );
		assertNotNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testGetClassificationByClassCountAverageLinkage()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances,
				new AverageLinkageUPGMAStrategy(), 3 );
		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Map< String, String > objectMapping = classification.getObjectMapping();
		double cutoff = classification.getCutoff();

		Set< String > class1 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class2 = new HashSet<>( Arrays.asList( "A", "B", "E", "G", "H" ) );
		Set< String > class3 = new HashSet<>( Arrays.asList( "C", "D", "I", "J" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3 ) );
		assertEquals( expectedClasses, classifiedObjects );
		assertNotNull( objectMapping );
		assertEquals( ClusterData.names.length, objectMapping.size() );
		assertEquals( 55.375d, cutoff, 0.000001d );
		assertNotNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testGetClassificationByClassCountCompleteLinkage()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances,
				new CompleteLinkageStrategy(), 4
		);

		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Map< String, String > objectMapping = classification.getObjectMapping();
		double cutoff = classification.getCutoff();

		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );

		Set< String > class1 = new HashSet<>( Arrays.asList( "A", "B", "E", "G", "H" ) );
		Set< String > class2 = new HashSet<>( Collections.singletonList( "I" ) );
		Set< String > class3 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class4 = new HashSet<>( Arrays.asList( "C", "D", "J" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3, class4 ) );

		assertEquals( expectedClasses, classifiedObjects );
		assertNotNull( objectMapping );
		assertEquals( ClusterData.names.length, objectMapping.size() );
		assertEquals( 75, cutoff, 0d );
		assertEquals( 95, cluster.getDistanceValue(), 0d );
		assertEquals( 94, Math.max( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertEquals( 68, Math.min( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertNotNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testGetClassificationByClassCountSingleLinkage()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances,
				new SingleLinkageStrategy(), 5
		);
		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Map< String, String > objectMapping = classification.getObjectMapping();
		double cutoff = classification.getCutoff();

		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );

		Set< String > class1 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class2 = new HashSet<>( Collections.singletonList( "I" ) );
		Set< String > class3 = new HashSet<>( Collections.singletonList( "G" ) );
		Set< String > class4 = new HashSet<>( Arrays.asList( "C", "D", "J" ) );
		Set< String > class5 = new HashSet<>( Arrays.asList( "A", "B", "E", "H" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3, class4, class5 ) );
		assertEquals( expectedClasses, classifiedObjects );
		assertNotNull( objectMapping );
		assertEquals( ClusterData.names.length, objectMapping.size() );
		assertEquals( 20, cutoff, 0d );
		assertEquals( 29, cluster.getDistanceValue(), 0d );
		assertEquals( 26, Math.max( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertEquals( 0, Math.min( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
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
		LinkageStrategy linkageStrategy = new AverageLinkageUPGMAStrategy();
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
				new AverageLinkageUPGMAStrategy(), 1 );
		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Set< String > expected = new HashSet<>( Arrays.asList( ClusterData.names ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Collections.singletonList( expected ) );
		assertEquals( expectedClasses, classifiedObjects );
		assertNull( classification.getObjectMapping() );
		assertNull( classification.getAlgorithmResult() );
	}

	@Test
	public void testBasicExample()
	{
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageUPGMAStrategy(),
								10 );
		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList(
				new HashSet<>( Collections.singletonList( "A" ) ),
				new HashSet<>( Collections.singletonList( "B" ) ),
				new HashSet<>( Collections.singletonList( "C" ) ),
				new HashSet<>( Collections.singletonList( "D" ) ),
				new HashSet<>( Collections.singletonList( "E" ) ),
				new HashSet<>( Collections.singletonList( "F" ) ),
				new HashSet<>( Collections.singletonList( "G" ) ),
				new HashSet<>( Collections.singletonList( "H" ) ),
				new HashSet<>( Collections.singletonList( "I" ) ),
				new HashSet<>( Collections.singletonList( "J" ) )
		) );
		assertEquals( expectedClasses, classifiedObjects );
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
						.getClassificationByClassCount( names, distances, new AverageLinkageUPGMAStrategy(), 2 );
		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );
		Set< Set< String > > classifiedObjects = classification.getClassifiedObjects();
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList(
				new HashSet<>( Collections.singletonList( "3" ) ),
				new HashSet<>( Arrays.asList( "1", "2" ) )
		) );
		assertNotNull( classification.getObjectMapping() );
		assertNotNull( cluster );
		assertEquals( expectedClasses, classifiedObjects );
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
		String[] names = { "1", "2", "3", "4" };
		double[][] distances = new double[][] {
				{ 0, 5, 10, 15 },
				{ 5, 0, 20, 25 },
				{ 10, 20, 0, 10 },
				{ 15, 25, 10, 0 }
		};
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( names, distances, new AverageLinkageUPGMAStrategy(), 2 );
		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );
		assertEquals( 17.5, cluster.getDistanceValue(), 0d );
		assertEquals( 5, child0.getDistance().getDistance(), 0d );
		assertEquals( 10, child1.getDistance().getDistance(), 0d );
	}

	/**
	 * Step 1: the distance matrix - get the minimum distance (i.e. distance between "1" and "2")
	 *  <pre>
	 *  | -  1  2  3  4 |
	 *  | 1  0          |
	 *  | 2 *2* 0       |
	 *  | 3  4  4  0    |
	 *  | 4  8  8  10 0 |
	 *  </pre>
	 *
	 *  Step 2: merge the two closest clusters (i.e. "1" and "2" and update the distance matrix) and find the next minimum distance (i.e. distance between "1,2" and "3")
	 *  <pre>
	 * 	   | -   1,2  3  4 |
	 * 	   | 1,2  0        |
	 * 	   | 3   *4*  0    |
	 * 	   | 4    8   10 0 |
	 * 	</pre>
	 *
	 * 	Step 3: merge the two closest clusters (i.e. "1,2" and "3" and update the distance matrix) and find the next minimum distance (i.e. distance between "1,2,3" and "4")
	 * 	 <pre>
	 * 	    | -     1,2,3  4 |
	 * 	    | 1,2,3   0      |
	 * 	    | 4      *9*    0 |
	 * 	 </pre>
	 *
	 * 	 End. No further merging possible.
	 *
	 * @see <a href="https://www.youtube.com/watch?v=T1ObCUpjq3o">Example how to compute hierarchical clustering with average linkage</a>. NB: the video falsely states that UPGMA is used, while it is actually explaining WPGMA.
	 */
	@Test
	public void testAverageLinkageClusteringWPGMA()
	{
		String[] names = { "1", "2", "3", "4" };
		double[][] distances = new double[][] {
				{ 0, 2, 4, 8 },
				{ 2, 0, 4, 8 },
				{ 4, 4, 0, 10 },
				{ 8, 8, 10, 0 }
		};
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( names, distances, new AverageLinkageWPGMAStrategy(), 2 );
		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );
		Cluster child10 = child1.getChildren().get( 0 );
		Cluster child11 = child1.getChildren().get( 1 );
		assertEquals( 9d, cluster.getDistanceValue(), 0d );
		assertEquals( 0d, child0.getDistanceValue(), 0d );
		assertEquals( 4d, child1.getDistanceValue(), 0d );
		assertEquals( 0d, child10.getDistanceValue(), 0d );
		assertEquals( 2d, child11.getDistanceValue(), 0d );
	}

	@Test
	public void testAverageLinkageClusteringUPGMA()
	{
		String[] names = { "1", "2", "3", "4" };
		double[][] distances = new double[][] {
				{ 0, 2, 4, 8 },
				{ 2, 0, 4, 8 },
				{ 4, 4, 0, 10 },
				{ 8, 8, 10, 0 }
		};
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( names, distances, new AverageLinkageUPGMAStrategy(), 2 );
		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );
		Cluster child10 = child1.getChildren().get( 0 );
		Cluster child11 = child1.getChildren().get( 1 );
		assertEquals( 8.6667d, cluster.getDistanceValue(), 0.0001d );
		assertEquals( 4d, Math.max( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertEquals( 0d, Math.min( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertEquals( 2d, Math.max( child10.getDistanceValue(), child11.getDistanceValue() ), 0d );
		assertEquals( 0d, Math.min( child10.getDistanceValue(), child11.getDistanceValue() ), 0d );
	}

	@Test
	public void testGetGlasbeyColor()
	{
		assertEquals( ColorUtils.GLASBEY[ 1 ].getRGB(), ClusterUtils.getGlasbeyColor( 1 ) );
		assertEquals( ColorUtils.GLASBEY[ 0 ].getRGB(), ClusterUtils.getGlasbeyColor( ColorUtils.GLASBEY.length ) );
		assertEquals( ColorUtils.GLASBEY[ 1 ].getRGB(), ClusterUtils.getGlasbeyColor( ColorUtils.GLASBEY.length + 1 ) );
	}

	@Test
	public void testGetColors()
	{
		Map< Integer, Integer > expected = new HashMap<>();
		expected.put( 0, ColorUtils.GLASBEY[ 1 ].getRGB() );
		expected.put( 1, ColorUtils.GLASBEY[ 2 ].getRGB() );
		expected.put( 2, ColorUtils.GLASBEY[ 3 ].getRGB() );
		expected.put( 3, ColorUtils.GLASBEY[ 4 ].getRGB() );

		assertEquals( expected, ClusterUtils.getGlasbeyColors( 4 ) );
	}

}
