package org.mastodon.mamut.io.importer.labelimage.math;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

public class CovarianceMatrixTest
{
	@Test
	public void testGet()
	{
		int[][] dataInt = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 6 } };
		CovarianceMatrix matrix = new CovarianceMatrix( 2 );
		for ( int[] values : dataInt )
			matrix.addValues( values );
		double[][] actual = matrix.get();

		assertArrayEquals( new double[] { 3d, 4d }, matrix.getMeans(), 0.0001d );
		assertArrayEquals( new double[] { 2.5d, 2.5d }, actual[ 0 ], 0.0001d );
		assertArrayEquals( new double[] { 2.5d, 2.5d }, actual[ 1 ], 0.0001d );
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
