package org.mastodon.mamut.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeometryUtilsTest
{
	@Test
	public void testGetEuclideanDistance()
	{
		assertEquals( Double.NaN, GeometryUtils.getEuclideanDistance( null, new double[ 1 ] ), 0d );
		assertEquals( Double.NaN, GeometryUtils.getEuclideanDistance( new double[ 1 ], null ), 0d );
		assertEquals( Double.NaN, GeometryUtils.getEuclideanDistance( new double[ 0 ], new double[ 1 ] ), 0d );
		assertEquals( Double.NaN, GeometryUtils.getEuclideanDistance( new double[ 1 ], new double[ 0 ] ), 0d );
		assertEquals( 0d, GeometryUtils.getEuclideanDistance( new double[ 1 ], new double[ 1 ] ), 0d );
		assertEquals( 2d, GeometryUtils.getEuclideanDistance( new double[] { 0d }, new double[] { 2d } ), 0d );
		assertEquals( Math.sqrt( 8d ),
				GeometryUtils.getEuclideanDistance( new double[] { 0d, 0d }, new double[] { 2d, 2d } ), 0d );
		assertEquals( Math.sqrt( 12d ),
				GeometryUtils.getEuclideanDistance( new double[] { 0d, 0d, 0d }, new double[] { 2d, 2d, 2d } ), 0d );
		assertEquals( 4d,
				GeometryUtils.getEuclideanDistance( new double[] { 0d, 0d, 0d, 0d }, new double[] { 2d, 2d, 2d, 2d } ),
				0d );
	}
}
