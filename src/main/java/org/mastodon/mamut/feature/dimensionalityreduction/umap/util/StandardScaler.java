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
	 * <br>
	 * This method modifies the input array in place.
	 * <br>
	 * Edge cases:
	 * <ul>
	 *     <li>If the column has variance=0 (i.e. all values are the same), the method will write 0 values in the column.</li>
	 *     <li>If the column contains {@link Double#NaN} values, the method will write 0 values in the column.</li>
	 *     <li>If the input matrix is empty, the method will do nothing.</li>
	 *     <li>If the input matrix has fewer rows than the specified column index, the method will throw an {@link ArrayIndexOutOfBoundsException}.</li>
	 * </ul>
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
			matrix[ i ][ columnIndex ] = Double.isNaN( standardizedColumn[ i ] ) ? 0d : standardizedColumn[ i ]; // StatUtils.normalize(...) writes NaN if a column has variance=0. This may not be desirable for methods that consume the result of this method (e.g. the UMAP algorithm). Thus, NaN values are replaced by 0.
		}
	}

	/**
	 * Standardizes each column of the given 2D array.
	 * <br>
	 * This method iterates over each column of the input array and applies the standardization
	 * to each column
	 * <br>
	 * This method modifies the input array in place.
	 * <br>
	 * Edge cases:
	 * <ul>
	 * 	<li>If a column has variance=0 (i.e. all values are the same), the method will write 0 values in the column.</li>
	 * 	<li>If a column contains {@link Double#NaN} values, the method will write 0 values in the column.</li>
	 * </ul>
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
