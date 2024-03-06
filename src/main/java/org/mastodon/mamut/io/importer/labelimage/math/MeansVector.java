package org.mastodon.mamut.io.importer.labelimage.math;

/**
 * Computes the mean of a set of values.<br>
 */
public class MeansVector
{
	private final Mean[] means;

	/**
	 * Create a new means vector for the given number of dimensions.
	 * @param dimensions the number of dimensions
	 */
	public MeansVector( int dimensions )
	{
		means = new Mean[ dimensions ];
	}

	/**
	 * Add a new value vector to the means computation.<br>
	 * The vector must have the same length as the number of dimensions of the means vector.
	 * @param x the value vector
	 */
	public void addValues( int[] x )
	{
		if ( x.length != means.length )
			throw new IllegalArgumentException( "Input array has wrong length." );
		for ( int i = 0; i < x.length; i++ )
		{
			if ( means[ i ] == null )
				means[ i ] = new Mean();
			means[ i ].addValue( x[ i ] );
		}
	}

	/**
	 * Gets the means vector.
	 * Returns a vector with {@link Double#NaN}s, if no samples have been added yet.
	 * @return the means vector
	 */
	public double[] get()
	{
		double[] result = new double[ means.length ];
		for ( int i = 0; i < means.length; i++ )
			result[ i ] = means[ i ].get();
		return result;
	}
}
