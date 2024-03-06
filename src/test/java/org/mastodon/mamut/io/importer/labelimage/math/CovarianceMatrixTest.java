package org.mastodon.mamut.io.importer.labelimage.math;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

public class CovarianceMatrixTest
{
	@Test
	public void testGet()
	{
		double[] x = { 1, 2, 3, 4, 5 };
		double[] y = { 2, 3, 4, 5, 6 };
		int[][] dataInt = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 6 } };
		org.apache.commons.math3.stat.correlation.Covariance covarianceApache =
				new org.apache.commons.math3.stat.correlation.Covariance();
		double[][] expected = { { covarianceApache.covariance( x, x ), covarianceApache.covariance( x, y ) },
				{ covarianceApache.covariance( y, x ), covarianceApache.covariance( y, y ) } };
		CovarianceMatrix matrix = new CovarianceMatrix( 2 );
		for ( int[] values : dataInt )
			matrix.addValues( values );
		double[][] actual = matrix.get();
		assertArrayEquals( expected[ 0 ], actual[ 0 ], 0.0001d );
		assertArrayEquals( expected[ 1 ], actual[ 1 ], 0.0001d );
	}

	@Test
	public void testException1()
	{
		CovarianceMatrix covarianceMatrix = new CovarianceMatrix( 2 );
		covarianceMatrix.addValues( new int[] { 1, 1 } );
		assertThrows( IllegalArgumentException.class, covarianceMatrix::get );
	}

	@Test
	public void testException2()
	{
		CovarianceMatrix covarianceMatrix = new CovarianceMatrix( 2 );
		assertThrows( IllegalArgumentException.class, () -> covarianceMatrix.addValues( new int[] { 1 } ) );
	}
}
