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
package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionTestUtils;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tagbio.umap.Umap;

class UmapTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Test
	void test()
	{
		int numCluster1 = 50;
		int numCluster2 = 100;
		double[][] sampleData = RandomDataTools.generateSampleData( numCluster1, numCluster2 );
		Umap umap = UmapDemo.setUpUmap();
		long t0 = System.currentTimeMillis();
		double[][] umapResult = umap.fitTransform( sampleData );
		logger.info( "UMAP took {} ms", System.currentTimeMillis() - t0 );

		assertEquals( umapResult.length, sampleData.length );
		assertEquals( 2, umapResult[ 0 ].length );

		double[][] umapResult1 = Arrays.copyOfRange( umapResult, 0, numCluster1 );
		double[][] umapResult2 = Arrays.copyOfRange( umapResult, numCluster1, numCluster1 + numCluster2 );

		DimensionalityReductionTestUtils.testNonOverlappingClusters( umapResult1, umapResult2 );

	}
}
