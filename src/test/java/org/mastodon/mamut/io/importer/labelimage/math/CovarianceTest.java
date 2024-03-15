package org.mastodon.mamut.io.importer.labelimage.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CovarianceTest
{
	@Test
	public void testGet()
	{
		double[] x = { 1, 2, 3, 4, 5 };
		double[] y = { 2, 3, 4, 5, 6 };
		org.mastodon.mamut.io.importer.labelimage.math.Covariance covariance =
				new org.mastodon.mamut.io.importer.labelimage.math.Covariance();
		for ( int i = 0; i < x.length; i++ )
			covariance.addValues( x[ i ], y[ i ] );
		double actual = covariance.get();
		assertEquals( 2.5d, actual, 0.0001d );
		assertEquals( 3d, covariance.getMeanX(), 0.0001d );
		assertEquals( 4d, covariance.getMeanY(), 0.0001d );
	}

	@Test
	public void testException()
	{
		org.mastodon.mamut.io.importer.labelimage.math.Covariance covariance =
				new org.mastodon.mamut.io.importer.labelimage.math.Covariance();
		covariance.addValues( 1, 1 );
		assertThrows( IllegalArgumentException.class, covariance::get );
	}
}
