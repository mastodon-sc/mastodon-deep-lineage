package org.mastodon.mamut.feature.dimensionalityreduction.umap.util;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.Arrays;

/**
 * Standardize vectors by removing the mean and scaling to unit variance.
 * <br>
 * Re-implemented from scikit-learn's <a href="https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.StandardScaler.html">StandardScaler</a>.
 * <br>
 * Cf. as well <a href="https://scikit-learn.org/stable/modules/preprocessing.html">Preprocessing data</a>.
 */
public class StandardScaler
{
	private StandardScaler()
	{
		// prevent instantiation
	}

	/**
	 * Standardizes a vector by removing the mean and scaling to unit variance.
	 * <br>
	 * This method calculates the mean and variance of the input vector,
	 * then scales each element of the vector by subtracting the mean and dividing by the standard deviation.
	 *
	 * @param vector The input vector to be standardized.
	 * @return A new vector where each element is scaled such that the mean of the vector equals to zero and variance of the values is unit variance.
	 */
	public static double[] standardizeVector( double[] vector )
	{
		double mean = Arrays.stream( vector ).sum() / vector.length;
		Variance varianceCalculator = new Variance();
		double variance = varianceCalculator.evaluate( vector );
		return scaleVector( vector, mean, variance );
	}

	/**
	 * Scales a vector by removing the mean and dividing by the standard deviation.
	 * <br>
	 * This method takes an input vector and scales each element by subtracting the mean and dividing by the standard deviation.
	 *
	 * @param vector The input vector to be scaled.
	 * @param mean The mean of the input vector.
	 * @param variance The variance of the input vector.
	 * @return A new vector where each element is scaled such that the mean of the vector equals to zero and variance of the values is unit variance.
	 */
	public static double[] scaleVector( double[] vector, double mean, double variance )
	{
		double standardDeviation = Math.sqrt( variance );
		double[] scaledVector = new double[ vector.length ];
		for ( int i = 0; i < vector.length; i++ )
		{
			scaledVector[ i ] = standardDeviation == 0 ? 0 : ( vector[ i ] - mean ) / standardDeviation;
		}
		return scaledVector;
	}

	/**
	 * Standardizes a specific column of the given 2D array (matrix).
	 * <br>
	 * This method extracts the specified column from the input matrix, standardizes it by removing the mean and scaling to unit variance,
	 * and then replaces the original column in the matrix with the standardized values.
	 *
	 * @param matrix The 2D array whose column is to be standardized.
	 * @param columnIndex The index of the column to standardize.
	 */
	private static void standardizeColumn( double[][] matrix, int columnIndex )
	{
		int numRows = matrix.length;
		double[] column = new double[ numRows ];

		for ( int i = 0; i < numRows; i++ )
		{
			column[ i ] = matrix[ i ][ columnIndex ];
		}

		double[] standardizedColumn = standardizeVector( column );

		for ( int i = 0; i < numRows; i++ )
		{
			matrix[ i ][ columnIndex ] = standardizedColumn[ i ];
		}
	}

	/**
	 * Standardizes each column of the given 2D array.
	 * <br>
	 * This method iterates over each column of the input array and applies the standardization
	 * to each column
	 *
	 * @param array The 2D array whose columns are to be standardized.
	 */
	public static void standardizeColumns( double[][] array )
	{
		if ( array.length == 0 )
			return;
		int numColumns = array[ 0 ].length;

		for ( int j = 0; j < numColumns; j++ )
		{
			standardizeColumn( array, j );
		}
	}
}
