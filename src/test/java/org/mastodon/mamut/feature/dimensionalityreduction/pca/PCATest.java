package org.mastodon.mamut.feature.dimensionalityreduction.pca;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionTestUtils;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.data.DataFrame;
import smile.feature.extraction.PCA;

class PCATest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Test
	void test()
	{
		int numCluster1 = 50;
		int numCluster2 = 100;
		double[][] inputData = RandomDataTools.generateSampleData( numCluster1, numCluster2 );
		logger.debug( "dimensions rows: {}, columns:{}", inputData.length, inputData[ 0 ].length );

		int targetDimensions = 2;

		DataFrame dataFrame = DataFrame.of( inputData );
		PCA pca = PCA.fit( dataFrame ).getProjection( targetDimensions );
		double[][] pcaResult = pca.apply( inputData );

		assertEquals( pcaResult.length, inputData.length );
		assertEquals( targetDimensions, pcaResult[ 0 ].length );

		double[][] pcaResult1 = Arrays.copyOfRange( pcaResult, 0, numCluster1 );
		double[][] pcaResult2 = Arrays.copyOfRange( pcaResult, numCluster1, numCluster1 + numCluster2 );

		DimensionalityReductionTestUtils.testNonOverlappingClusters( pcaResult1, pcaResult2 );

	}
}
