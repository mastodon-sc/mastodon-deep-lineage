package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionTestUtils;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.manifold.UMAP;

class UmapSmileTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Test
	void test()
	{
		int numCluster1 = 50;
		int numCluster2 = 100;
		// create two distinct clusters of points in 3D space, one having 50 points and the other 100 points
		double[][] sampleData = RandomDataTools.generateSampleData( numCluster1, numCluster2 );
	
		// Recommendations for UMAP defaults: https://github.com/lmcinnes/umap/blob/a012b9d8751d98b94935ca21f278a54b3c3e1b7f/umap/umap_.py#L1073
		double minDist = 0.1;
		int nNeighbors = 15;
		int d = 2;
		double learningRate = 1.0;
		double spread = 1.0;
		int negativeSampleRate = 5;
		double repulsionStrength = 1.0;
		long t0 = System.currentTimeMillis();
		Properties props = new Properties();
		props.setProperty( "smile.umap.k", String.valueOf( nNeighbors ) );
		props.setProperty( "smile.umap.d", String.valueOf( d ) );
		props.setProperty( "smile.umap.learning_rate", String.valueOf( learningRate ) );
		props.setProperty( "smile.umap.min_dist", String.valueOf( minDist ) );
		props.setProperty( "smile.umap.spread", String.valueOf( spread ) );
		props.setProperty( "smile.umap.negative_samples", String.valueOf( negativeSampleRate ) );
		props.setProperty( "smile.umap.repulsion_strength", String.valueOf( repulsionStrength ) );

		double[][] umapResult = UMAP.fit( sampleData, UMAP.Options.of( props ) );
		logger.info( "UMAP took {} ms", System.currentTimeMillis() - t0 );
	
		assertEquals( 2, umapResult[ 0 ].length );
		assertEquals( umapResult.length, sampleData.length ); // fails, because only the largest connected component is used inside the algorithm
	
		double[][] umapResult1 = Arrays.copyOfRange( umapResult, 0, numCluster1 );
		double[][] umapResult2 = Arrays.copyOfRange( umapResult, numCluster1, numCluster1 + numCluster2 );
	
		DimensionalityReductionTestUtils.testNonOverlappingClusters( umapResult1, umapResult2 ); // should pass
	}
}
