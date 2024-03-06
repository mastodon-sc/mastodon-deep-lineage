/*-
 * #%L
 * mastodon-ellipsoid-fitting
 * %%
 * Copyright (C) 2015 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.importer.labelimage.demo;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;

public class MultiVariateNormalDistributionRenderer
{

	/**
	 * Renders the density function of a multivariate normal distribution into a given image.
	 * @see <a href="https://en.wikipedia.org/wiki/Multivariate_normal_distribution">Wikipedia Multivariate normal distribution</a>
	 * @param center center of the distribution
	 * @param cov covariance matrix of the distribution (must be symmetric and positive definite)
	 * @param image the image to render into (image is a cube)
	 *
	 */
	public static void renderMultivariateNormalDistribution( double[] center, double[][] cov,
			RandomAccessibleInterval< FloatType > image )
	{
		AffineTransform3D sigma = new AffineTransform3D();
		sigma.set(
				cov[ 0 ][ 0 ], cov[ 0 ][ 1 ], cov[ 0 ][ 2 ], 0,
				cov[ 1 ][ 0 ], cov[ 1 ][ 1 ], cov[ 1 ][ 2 ], 0,
				cov[ 2 ][ 0 ], cov[ 2 ][ 1 ], cov[ 2 ][ 2 ], 0
		);
		double[] coord = new double[ 3 ];
		double[] out = new double[ 3 ];
		LoopBuilder.setImages( Intervals.positions( image ), image ).forEachPixel( ( position, pixel ) -> {
			position.localize( coord );
			LinAlgHelpers.subtract( coord, center, coord );
			sigma.applyInverse( out, coord );
			// leave out the 1 / (sqrt( ( 2 * pi ) ^ 3 * det( cov )) factor to make the image more visible
			double value = Math.exp( -0.5 * scalarProduct( coord, out ) );
			pixel.setReal( 1000 * value );
		} );
	}

	/**
	 * Computes the scalar product of two vectors.
	 */
	public static double scalarProduct( double[] a, double[] b )
	{
		return a[ 0 ] * b[ 0 ] + a[ 1 ] * b[ 1 ] + a[ 2 ] * b[ 2 ];
	}
}
