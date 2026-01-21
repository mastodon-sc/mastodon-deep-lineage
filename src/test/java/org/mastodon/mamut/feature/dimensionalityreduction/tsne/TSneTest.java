/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.feature.dimensionalityreduction.tsne;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionTestUtils;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.manifold.TSNE;

class TSneTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Test
	void test()
	{
		int numCluster1 = 50;
		int numCluster2 = 100;
		double[][] inputData = RandomDataTools.generateSampleData( numCluster1, numCluster2 );
		for ( int i = 0; i < inputData.length; i++ )
		{
			logger.debug( "inputData[{}]: {}, {}", i, inputData[ i ][ 0 ], inputData[ i ][ 1 ] );
		}
		logger.debug( "dimensions rows: {}, columns:{}", inputData.length, inputData[ 0 ].length );


		// Recommendations for t-SNE defaults: https://scikit-learn.org/stable/modules/generated/sklearn.manifold.TSNE.html
		double perplexity = 30d; // recommended value is between 5 and 50
		int maxIterations = 1000; // should be at least 250
		int d = 2; // target dimension
		double eta = 200; // learning rate
		double earlyExaggeration = 12; // default value
		TSNE tsne = TSNE.fit( inputData, new TSNE.Options( d, perplexity, eta, earlyExaggeration, maxIterations ) );
		double[][] tsneResult = tsne.coordinates();

		assertEquals( tsneResult.length, inputData.length );
		assertEquals( 2, tsneResult[ 0 ].length );

		double[][] tsneResult1 = Arrays.copyOfRange( tsneResult, 0, numCluster1 );
		double[][] tsneResult2 = Arrays.copyOfRange( tsneResult, numCluster1, numCluster1 + numCluster2 );

		DimensionalityReductionTestUtils.testNonOverlappingClusters( tsneResult1, tsneResult2 );
	}
}
