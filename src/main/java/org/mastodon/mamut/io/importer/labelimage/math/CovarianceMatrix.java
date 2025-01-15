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
package org.mastodon.mamut.io.importer.labelimage.math;

/**
 * Computes the covariance matrix for independent values given as value vectors.<br>
 * Uses an online algorithm to compute the covariance, cf.: <a href=https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online>Online algorithm for covariance</a>
 */
public class CovarianceMatrix
{
	private final Covariance[][] c;

	/**
	 * Create a new covariance matrix for the given number of dimensions.
	 * @param dimensions the number of dimensions
	 */
	public CovarianceMatrix( int dimensions )
	{
		c = new Covariance[ dimensions ][ dimensions ];
	}

	/**
	 * Add a new value vector to the covariance matrix computation.<br>
	 * The vector must have the same length as the number of dimensions of the covariance matrix.
	 * @param x the value vector
	 */
	public void addValues( int[] x )
	{
		if ( x.length != c.length )
			throw new IllegalArgumentException( "Input array has wrong length." );
		for ( int i = 0; i < x.length; i++ )
		{
			for ( int j = i; j < x.length; j++ )
			{
				if ( c[ i ][ j ] == null )
					c[ i ][ j ] = new Covariance();
				c[ i ][ j ].addValues( x[ i ], x[ j ] );
			}
		}
	}

	/**
	 * Gets the covariance matrix.
	 * @throws IllegalArgumentException if the number of samples is less than 2
	 * @return the covariance matrix
	 */
	public double[][] get()
	{
		double[][] result = new double[ c.length ][ c.length ];
		for ( int i = 0; i < c.length; i++ )
		{
			for ( int j = i; j < c.length; j++ )
			{
				result[ i ][ j ] = c[ i ][ j ].get();
				if ( i != j )
					result[ j ][ i ] = result[ i ][ j ];
			}
		}
		return result;
	}

	/**
	 * Gets the means of the variables.
	 * @return the means of the variables
	 */
	public double[] getMeans()
	{
		double[] result = new double[ c.length ];
		for ( int i = 0; i < c.length; i++ )
			result[ i ] = c[ i ][ i ].getMeanX();
		return result;
	}
}
