package org.mastodon.mamut.feature.dimensionalityreduction.umap.util;

import org.apache.commons.math3.stat.StatUtils;

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
		double[] standardizedColumn = StatUtils.normalize( column );

		for ( int i = 0; i < numRows; i++ )
		{
			matrix[ i ][ columnIndex ] = Double.isNaN( standardizedColumn[ i ] ) ? 0d : standardizedColumn[ i ]; // replace NaNs with 0
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
