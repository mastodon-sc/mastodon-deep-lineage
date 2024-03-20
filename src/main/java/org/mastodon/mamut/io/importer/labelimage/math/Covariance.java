package org.mastodon.mamut.io.importer.labelimage.math;

/**
 * Computes the covariance for two independent variables.<br>
 * Uses an online algorithm to compute the covariance, cf.: <a href=https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online>Online algorithm for covariance</a>
 */
public class Covariance
{
	private double meanX = 0;

	private double meanY = 0;

	private double c = 0;

	private int n = 0;

	/**
	 * Add a new pair of values to the covariance computation.
	 * @param x the first value
	 * @param y the second value
	 */
	public void addValues( double x, double y )
	{
		n++;
		double dx = x - meanX;
		meanX += dx / n;
		meanY += ( y - meanY ) / n;
		c += dx * ( y - meanY );
	}

	/**
	 * Gets the covariance.
	 * @throws IllegalArgumentException if the number of samples is less than 2
	 * @return the covariance
	 */
	public double get()
	{
		if ( n < 2 )
			throw new IllegalArgumentException( "Number of samples is less than 2." );
		return c / ( n - 1 );
	}

	/**
	 * Gets the mean of the first variable.
	 * @return the mean of the first variable
	 */
	public double getMeanX()
	{
		return meanX;
	}

	/**
	 * Gets the mean of the second variable.
	 * @return the mean of the second variable
	 */
	public double getMeanY()
	{
		return meanY;
	}
}
