/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

public class TSneDemo
{
	public static void main( final String[] args )
	{
		double[][] inputData = RandomDataTools.generateSampleData();
		TSneConfiguration config = setUpTSne( inputData );
		BarnesHutTSne tsne = new ParallelBHTsne(); // according to https://github.com/lejon/T-SNE-Java/ the parallel version is faster at same accuracy
		double[][] tsneResult = tsne.tsne( config );
		PlotPoints.plot( inputData, tsneResult, resultValues -> resultValues[ 0 ] > 10 );
	}

	static TSneConfiguration setUpTSne( double[][] inputData )
	{
		// Recommendations for t-SNE defaults: https://scikit-learn.org/stable/modules/generated/sklearn.manifold.TSNE.html
		int initialDimensions = 50; // used if PCA is true and dimensions of the input data are greater than this value
		double perplexity = 30d; // recommended value is between 5 and 50
		int maxIterations = 1000; // should be at least 250

		return TSneUtils.buildConfig( inputData, 2, initialDimensions, perplexity, maxIterations, true, 0.5d, false, true );
	}
}
