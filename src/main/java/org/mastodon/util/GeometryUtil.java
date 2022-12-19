package org.mastodon.util;

public class GeometryUtil
{
	/**
	 * Gets the euclidean distances between the two given coordinate arrays. The
	 * number of dimensions of both coordinate array is expected to be the same.
	 * Otherwise {@link Double#NaN} is returned
	 * 
	 * @param coordinates1 the first coordinate array
	 * @param coordinates2 the second coordinate array
	 * @return the euclidean distance
	 */
	public static double getEuclideanDistance(double[] coordinates1, double[] coordinates2)
	{
		if (coordinates1 == null)
			return Double.NaN;
		if (coordinates2 == null)
			return Double.NaN;
		if (coordinates1.length == 0)
			return Double.NaN;
		if (coordinates2.length == 0)
			return Double.NaN;
		if (coordinates1.length != coordinates2.length)
			return Double.NaN;
		if (coordinates1.length == 1)
			return Math.abs( coordinates1[0] - coordinates2[0]);
		double distanceSquared = 0d;
		for ( int dimension = 0; dimension < coordinates1.length; dimension++ )
		{
			final double coordinate = coordinates1[dimension]   - coordinates2[dimension];
			distanceSquared += coordinate * coordinate;
		}
		return Math.sqrt( distanceSquared );
	}
}
