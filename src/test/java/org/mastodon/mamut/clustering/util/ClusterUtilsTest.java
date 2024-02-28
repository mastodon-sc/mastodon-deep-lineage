/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class ClusterUtilsTest
{

	@Test
	public void testGetClassificationByThreshold()
	{
		double threshold = 57.5d;
		Classification< String > classification =
				ClusterUtils.getClassificationByThreshold( ClusterData.example1.getKey(), ClusterData.example1.getValue(),
						new AverageLinkageUPGMAStrategy(), threshold );

		Set< String > class1 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class2 = new HashSet<>( Arrays.asList( "A", "B", "E", "G", "H" ) );
		Set< String > class3 = new HashSet<>( Arrays.asList( "C", "D", "I", "J" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3 ) );
		assertEquals( expectedClasses, classification.getClassifiedObjects() );
		assertEquals( threshold, classification.getCutoff(), 0d );
		assertNotNull( classification.getRootCluster() );
	}

	@Test
	public void testGetClassificationByClassCountAverageLinkage()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.example1.getKey(),
				ClusterData.example1.getValue(), new AverageLinkageUPGMAStrategy(), 3 );
		double cutoff = classification.getCutoff();

		Set< String > class1 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class2 = new HashSet<>( Arrays.asList( "A", "B", "E", "G", "H" ) );
		Set< String > class3 = new HashSet<>( Arrays.asList( "C", "D", "I", "J" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3 ) );
		assertEquals( expectedClasses, classification.getClassifiedObjects() );
		assertEquals( 55.375d, cutoff, 0.000001d );
		assertNotNull( classification.getRootCluster() );
	}

	@Test
	public void testGetClassificationByClassCountCompleteLinkage()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.example1.getKey(),
				ClusterData.example1.getValue(), new CompleteLinkageStrategy(), 4
		);

		double cutoff = classification.getCutoff();

		Cluster cluster = classification.getRootCluster();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );

		Set< String > class1 = new HashSet<>( Arrays.asList( "A", "B", "E", "G", "H" ) );
		Set< String > class2 = new HashSet<>( Collections.singletonList( "I" ) );
		Set< String > class3 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class4 = new HashSet<>( Arrays.asList( "C", "D", "J" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3, class4 ) );

		assertEquals( expectedClasses, new HashSet<>( classification.getClassifiedObjects() ) );
		assertEquals( 75, cutoff, 0d );
		assertEquals( 95, cluster.getDistanceValue(), 0d );
		assertEquals( 94, Math.max( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertEquals( 68, Math.min( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertNotNull( classification.getRootCluster() );
	}

	@Test
	public void testGetClassificationByClassCountSingleLinkage()
	{
		Classification< String > classification = ClusterUtils.getClassificationByClassCount( ClusterData.example1.getKey(),
				ClusterData.example1.getValue(), new SingleLinkageStrategy(), 5 );
		double cutoff = classification.getCutoff();

		Cluster cluster = classification.getRootCluster();
		assertNotNull( cluster );
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );

		Set< String > class1 = new HashSet<>( Collections.singletonList( "F" ) );
		Set< String > class2 = new HashSet<>( Collections.singletonList( "I" ) );
		Set< String > class3 = new HashSet<>( Collections.singletonList( "G" ) );
		Set< String > class4 = new HashSet<>( Arrays.asList( "C", "D", "J" ) );
		Set< String > class5 = new HashSet<>( Arrays.asList( "A", "B", "E", "H" ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList( class1, class2, class3, class4, class5 ) );
		assertEquals( expectedClasses, new HashSet<>( classification.getClassifiedObjects() ) );
		assertEquals( 20, cutoff, 0d );
		assertEquals( 29, cluster.getDistanceValue(), 0d );
		assertEquals( 26, Math.max( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertEquals( 0, Math.min( child0.getDistanceValue(), child1.getDistanceValue() ), 0d );
		assertNotNull( classification.getRootCluster() );
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
		String[] classNames = ClusterData.example1.getKey();
		double[][] distances = ClusterData.example1.getValue();
		// zero classes
		assertThrows( IllegalArgumentException.class,
				() -> ClusterUtils.getClassificationByClassCount( classNames, distances, linkageStrategy, 0 ) );
		// too many classes
		assertThrows( IllegalArgumentException.class,
				() -> ClusterUtils.getClassificationByClassCount( classNames, distances, linkageStrategy, 11 ) );
		// negative threshold
		assertThrows( IllegalArgumentException.class,
				() -> ClusterUtils.getClassificationByThreshold( classNames, distances, linkageStrategy, -1 ) );
	}

	@Test
	public void testOneClass()
	{
		Classification< String > classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.example1.getKey(), ClusterData.example1.getValue(),
				new AverageLinkageUPGMAStrategy(), 1 );
		Set< String > expected = new HashSet<>( Arrays.asList( ClusterData.example1.getKey() ) );
		Set< Set< String > > expectedClasses = new HashSet<>( Collections.singletonList( expected ) );
		assertEquals( expectedClasses, classification.getClassifiedObjects() );
		assertNotNull( classification.getRootCluster() );
	}

	@Test
	public void testBasicExample()
	{
		Classification< String > classification =
				ClusterUtils
						.getClassificationByClassCount( ClusterData.example1.getKey(), ClusterData.example1.getValue(),
								new AverageLinkageUPGMAStrategy(),
								10 );
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
		assertEquals( expectedClasses, classification.getClassifiedObjects() );
		assertNotNull( classification.getRootCluster() );
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
		Cluster cluster = classification.getRootCluster();
		Cluster child0 = cluster.getChildren().get( 0 );
		Cluster child1 = cluster.getChildren().get( 1 );
		Set< Set< String > > expectedClasses = new HashSet<>( Arrays.asList(
				new HashSet<>( Collections.singletonList( "3" ) ),
				new HashSet<>( Arrays.asList( "1", "2" ) )
		) );
		assertNotNull( cluster );
		assertEquals( expectedClasses, classification.getClassifiedObjects() );
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
		Cluster cluster = classification.getRootCluster();
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
		Cluster cluster = classification.getRootCluster();
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
		Cluster cluster = classification.getRootCluster();
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
		int skipFirstNColors = 5;
		assertEquals( ColorUtils.GLASBEY[ 5 ].getRGB(), ClusterUtils.getGlasbeyColor( 0, skipFirstNColors ) );
		assertEquals(
				ColorUtils.GLASBEY[ 5 ].getRGB(),
				ClusterUtils.getGlasbeyColor( ColorUtils.GLASBEY.length - skipFirstNColors, skipFirstNColors )
		);
		assertEquals(
				ColorUtils.GLASBEY[ 6 ].getRGB(),
				ClusterUtils.getGlasbeyColor( ColorUtils.GLASBEY.length - skipFirstNColors + 1, skipFirstNColors )
		);
	}

	@Test
	public void testGetColors()
	{
		List< Integer > expected = new ArrayList<>();
		expected.add( ColorUtils.GLASBEY[ 5 ].getRGB() );
		expected.add( ColorUtils.GLASBEY[ 6 ].getRGB() );
		expected.add( ColorUtils.GLASBEY[ 7 ].getRGB() );
		expected.add( ColorUtils.GLASBEY[ 8 ].getRGB() );

		assertEquals( expected, ClusterUtils.getGlasbeyColors( 4 ) );
		assertEquals( 0, ClusterUtils.getGlasbeyColors( 0 ).size() );
	}

	@Test
	public void testGetUpperTriangle()
	{
		double[][] inputMatrix0x0 = new double[ 0 ][ 0 ];
		double[][] inputMatrix1x1 = new double[][] {
				{ 0 }
		};
		double[][] inputMatrix4x3 = new double[][] {
				{ 0, 1, 2, 6 },
				{ 1, 0, 3, 8 },
				{ 6, 8, 9, 0 }
		};
		double[][] inputMatrix2x2 = new double[][] {
				{ 0, 1 },
				{ 1, 0 }
		};
		double[] expectedResult2x2 = new double[] { 1 };
		double[][] inputMatrix3x3 = new double[][] {
				{ 0, 1, 2 },
				{ 1, 0, 3 },
				{ 2, 3, 0 }
		};
		double[] expectedResult3x3 = new double[] { 1, 2, 3 };
		double[][] inputMatrix4x4 = new double[][] {
				{ 0, 1, 2, 6 },
				{ 1, 0, 3, 8 },
				{ 2, 3, 0, 9 },
				{ 6, 8, 9, 0 }
		};
		double[] expectedResult4x4 = new double[] { 1, 2, 6, 3, 8, 9 };

		assertEquals( 0, ClusterUtils.getUpperTriangle( null ).length );
		assertEquals( 0, ClusterUtils.getUpperTriangle( inputMatrix0x0 ).length );
		assertEquals( 0, ClusterUtils.getUpperTriangle( inputMatrix1x1 ).length );
		assertThrows( IllegalArgumentException.class, () -> ClusterUtils.getUpperTriangle( inputMatrix4x3 ) );
		assertArrayEquals( expectedResult2x2, ClusterUtils.getUpperTriangle( inputMatrix2x2 ), 0d );
		assertArrayEquals( expectedResult3x3, ClusterUtils.getUpperTriangle( inputMatrix3x3 ), 0d );
		assertArrayEquals( expectedResult4x4, ClusterUtils.getUpperTriangle( inputMatrix4x4 ), 0d );
	}
}
