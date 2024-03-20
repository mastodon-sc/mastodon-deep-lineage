package org.mastodon.mamut.io.importer.labelimage.math;

/**
 * Computes the mean of a set of values.
 */
public class Mean
{
	private long sum = 0;

	private int n;

	/**
	 * Add a new value to the mean computation.
	 * @param x the value
	 */
	public void addValue( long x )
	{
		n++;
		sum += x;
	}

	/**
	 * Gets the mean.
	 * Returns {@link Double#NaN}, if no samples have been added yet.
	 * @return the mean
	 */
	public double get()
	{
		return ( double ) sum / n;
	}
}
