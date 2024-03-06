package org.mastodon.mamut.io.importer.labelimage.math;

/**
 * Computes the covariance matrix for independent values given as value vectors.<br>
 * Uses an online algorithm to compute the covariance, cf.: <a href=https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online>Online algorithm for covariance</a>
 */
public class CovarianceMatrix
{
	private final Covariance[][] c;

	/**
	 * Create a new covariance matrix for the given number of dimensions.
	 * @param dimensions the number of dimensions
	 */
	public CovarianceMatrix( int dimensions )
	{
		c = new Covariance[ dimensions ][ dimensions ];
	}

	/**
	 * Add a new value vector to the covariance matrix computation.<br>
	 * The vector must have the same length as the number of dimensions of the covariance matrix.
	 * @param x the value vector
	 */
	public void addValues( int[] x )
	{
		if ( x.length != c.length )
			throw new IllegalArgumentException( "Input array has wrong length." );
		for ( int i = 0; i < x.length; i++ )
		{
			for ( int j = i; j < x.length; j++ )
			{
				if ( c[ i ][ j ] == null )
					c[ i ][ j ] = new Covariance();
				c[ i ][ j ].addValues( x[ i ], x[ j ] );
			}
		}
	}

	/**
	 * Gets the covariance matrix.
	 * @throws IllegalArgumentException if the number of samples is less than 2
	 * @return the covariance matrix
	 */
	public double[][] get()
	{
		double[][] result = new double[ c.length ][ c.length ];
		for ( int i = 0; i < c.length; i++ )
		{
			for ( int j = i; j < c.length; j++ )
			{
				result[ i ][ j ] = c[ i ][ j ].get();
				if ( i != j )
					result[ j ][ i ] = result[ i ][ j ];
			}
		}
		return result;
	}
}
