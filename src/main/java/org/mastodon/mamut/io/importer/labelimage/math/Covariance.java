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
package org.mastodon.mamut.io.importer.labelimage.math;

/**
 * Computes the covariance for two independent variables.<br>
 * Uses an online algorithm to compute the covariance, cf.: <a href=https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online>Online algorithm for covariance</a>
 */
public class Covariance
{
	private double meanX = 0;

	private double meanY = 0;

	private double c = 0;

	private int n = 0;

	/**
	 * Add a new pair of values to the covariance computation.
	 * @param x the first value
	 * @param y the second value
	 */
	public void addValues( double x, double y )
	{
		n++;
		double dx = x - meanX;
		meanX += dx / n;
		meanY += ( y - meanY ) / n;
		c += dx * ( y - meanY );
	}

	/**
	 * Gets the covariance.
	 * @throws IllegalArgumentException if the number of samples is less than 2
	 * @return the covariance
	 */
	public double get()
	{
		if ( n < 2 )
			throw new IllegalArgumentException( "Number of samples is less than 2." );
		return c / ( n - 1 );
	}

	/**
	 * Gets the mean of the first variable.
	 * @return the mean of the first variable
	 */
	public double getMeanX()
	{
		return meanX;
	}

	/**
	 * Gets the mean of the second variable.
	 * @return the mean of the second variable
	 */
	public double getMeanY()
	{
		return meanY;
	}
}
