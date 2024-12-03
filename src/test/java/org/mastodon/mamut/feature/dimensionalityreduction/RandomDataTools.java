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
package org.mastodon.mamut.feature.dimensionalityreduction;

import java.util.Random;

public class RandomDataTools
{
	public static double[][] generateSampleData()
	{
		return generateSampleData( 50, 100 );
	}

	public static double[][] generateSampleData( int numCluster1, int numCluster2 )
	{
		double[][] firstPointCloud = generateRandomPointsInSphere( 100, 100, -10, 20, numCluster1 );
		double[][] secondPointCloud = generateRandomPointsInSphere( 250, 250, 10, 50, numCluster2 );

		return concatenateArrays( firstPointCloud, secondPointCloud );
	}

	private static double[][] concatenateArrays( final double[][] firstPointCloud, final double[][] secondPointCloud )
	{
		double[][] concatenated = new double[ firstPointCloud.length + secondPointCloud.length ][ 2 ];
		System.arraycopy( firstPointCloud, 0, concatenated, 0, firstPointCloud.length );
		System.arraycopy( secondPointCloud, 0, concatenated, firstPointCloud.length, secondPointCloud.length );
		return concatenated;
	}

	private static double[][] generateRandomPointsInSphere( double centerX, double centerY, double centerZ, double radius,
			int numberOfPoints )
	{
		double[][] points = new double[ numberOfPoints ][ 3 ];

		final Random random = new Random( 42 );

		for ( int i = 0; i < numberOfPoints; i++ )
		{
			double r = radius * Math.cbrt( random.nextDouble() );
			double theta = 2 * Math.PI * random.nextDouble();
			double phi = Math.acos( 2 * random.nextDouble() - 1 );

			double x = centerX + r * Math.sin( phi ) * Math.cos( theta );
			double y = centerY + r * Math.sin( phi ) * Math.sin( theta );
			double z = centerZ + r * Math.cos( phi );

			points[ i ][ 0 ] = x;
			points[ i ][ 1 ] = y;
			points[ i ][ 2 ] = z;
		}

		return points;
	}
}
