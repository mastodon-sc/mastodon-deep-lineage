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
 * Computes the mean of a set of values.<br>
 */
public class MeansVector
{
	private final Mean[] means;

	/**
	 * Create a new means vector for the given number of dimensions.
	 * @param dimensions the number of dimensions
	 */
	public MeansVector( int dimensions )
	{
		means = new Mean[ dimensions ];
	}

	/**
	 * Add a new value vector to the means computation.<br>
	 * The vector must have the same length as the number of dimensions of the means vector.
	 * @param x the value vector
	 */
	public void addValues( int[] x )
	{
		if ( x.length != means.length )
			throw new IllegalArgumentException( "Input array has wrong length." );
		for ( int i = 0; i < x.length; i++ )
		{
			if ( means[ i ] == null )
				means[ i ] = new Mean();
			means[ i ].addValue( x[ i ] );
		}
	}

	/**
	 * Gets the means vector.
	 * Returns a vector with {@link Double#NaN}s, if no samples have been added yet.
	 * @return the means vector
	 */
	public double[] get()
	{
		double[] result = new double[ means.length ];
		for ( int i = 0; i < means.length; i++ )
			result[ i ] = means[ i ].get();
		return result;
	}
}
