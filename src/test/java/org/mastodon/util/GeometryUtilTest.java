package org.mastodon.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeometryUtilTest
{
	@Test
	public void testGetEuclideanDistance()
	{
		assertEquals( Double.NaN, GeometryUtil.getEuclideanDistance( null, new double[ 1 ] ), 0d );
		assertEquals( Double.NaN, GeometryUtil.getEuclideanDistance( new double[ 1 ], null ), 0d );
		assertEquals( Double.NaN, GeometryUtil.getEuclideanDistance( new double[ 0 ], new double[ 1 ] ), 0d );
		assertEquals( Double.NaN, GeometryUtil.getEuclideanDistance( new double[ 1 ], new double[ 0 ] ), 0d );
		assertEquals( 0d, GeometryUtil.getEuclideanDistance( new double[ 1 ], new double[ 1 ] ), 0d );
		assertEquals( 2d, GeometryUtil.getEuclideanDistance( new double[] { 0d }, new double[] { 2d } ), 0d );
		assertEquals( Math.sqrt( 8d ),
				GeometryUtil.getEuclideanDistance( new double[] { 0d, 0d }, new double[] { 2d, 2d } ), 0d );
		assertEquals( Math.sqrt( 12d ),
				GeometryUtil.getEuclideanDistance( new double[] { 0d, 0d, 0d }, new double[] { 2d, 2d, 2d } ), 0d );
		assertEquals( 4d,
				GeometryUtil.getEuclideanDistance( new double[] { 0d, 0d, 0d, 0d }, new double[] { 2d, 2d, 2d, 2d } ),
				0d );
	}
}
