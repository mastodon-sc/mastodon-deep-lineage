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

import java.util.Properties;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

import smile.manifold.TSNE;

public class TSneDemo
{
	public static void main( final String[] args )
	{
		double[][] inputData = RandomDataTools.generateSampleData();
		double[][] tsneResult = setUpTSne( inputData );
		PlotPoints.plot( inputData, tsneResult, resultValues -> resultValues[ 1 ] > 0 );
	}

	static double[][] setUpTSne( double[][] inputData )
	{
		int d = 2; // target dimension
		double perplexity = 30d;
		double eta = 200; // learning rate
		int maxIter = 1000; // maximum number of iterations
		Properties p = new Properties();
		p.setProperty( "smile.t_sne.d", String.valueOf( d ) );
		p.setProperty( "smile.t_sne.perplexity", String.valueOf( perplexity ) );
		p.setProperty( "smile.t_sne.eta", String.valueOf( eta ) );
		p.setProperty( "smile.t_sne.iterations", String.valueOf( maxIter ) );
		TSNE tsne = TSNE.fit( inputData, TSNE.Options.of( p ) );
		return tsne.coordinates();
	}
}
