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
		org.apache.commons.math3.stat.correlation.Covariance covarianceApache = new org.apache.commons.math3.stat.correlation.Covariance();
		double expected = covarianceApache.covariance( x, y );
		org.mastodon.mamut.io.importer.labelimage.math.Covariance covariance =
				new org.mastodon.mamut.io.importer.labelimage.math.Covariance();
		for ( int i = 0; i < x.length; i++ )
			covariance.addValues( x[ i ], y[ i ] );
		double actual = covariance.get();
		assertEquals( expected, actual, 0.0001d );
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
