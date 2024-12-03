package org.mastodon.mamut.feature.dimensionalityreduction;

import java.util.Random;

public class RandomDataTools
{
	private static final Random random = new Random( 42 );

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
