package org.mastodon.mamut.feature.dimensionalityreduction;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.geom.Rectangle2D;

public class DimensionalityReductionTestUtils
{
	public static void testNonOverlappingClusters( final double[][] cluster1, final double[][] cluster2 )
	{
		Rectangle2D.Double boundingBox1 = findBoundingBox( cluster1 );
		Rectangle2D.Double boundingBox2 = findBoundingBox( cluster2 );
		testNoPointInsideBoundingBox( cluster1, boundingBox2 );
		testNoPointInsideBoundingBox( cluster2, boundingBox1 );
	}

	private static void testNoPointInsideBoundingBox( final double[][] points, final Rectangle2D.Double boundingBox )
	{
		for ( double[] point : points )
		{
			double x = point[ 0 ];
			double y = point[ 1 ];
			assertFalse( boundingBox.contains( x, y ) );
		}
	}

	private static Rectangle2D.Double findBoundingBox( double[][] points )
	{
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for ( double[] point : points )
		{
			double x = point[ 0 ];
			double y = point[ 1 ];

			minX = Math.min( x, minX );
			maxX = Math.max( x, maxX );
			minY = Math.min( y, minY );
			maxY = Math.max( y, maxY );
		}

		return new Rectangle2D.Double( minX, minY, maxX - minX, maxY - minY );
	}
}
