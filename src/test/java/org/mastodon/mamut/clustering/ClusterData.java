package org.mastodon.mamut.clustering;

public class ClusterData
{
	public final static String[] names = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

	public final static double[][] fixedDistances = getFixedSymmetricDistanceMatrix();

	private static double[][] getFixedSymmetricDistanceMatrix()
	{
		return new double[][] {
				{ 0, 51, 81, 35, 9, 95, 37, 19, 48, 21 },
				{ 51, 0, 51, 21, 51, 55, 26, 17, 95, 75 },
				{ 81, 51, 0, 9, 59, 66, 29, 73, 39, 3 },
				{ 35, 21, 9, 0, 81, 46, 71, 27, 26, 11 },
				{ 9, 51, 59, 81, 0, 29, 50, 68, 93, 84 },
				{ 95, 55, 66, 46, 29, 0, 85, 91, 87, 82 },
				{ 37, 26, 29, 71, 50, 85, 0, 34, 60, 80 },
				{ 19, 17, 73, 27, 68, 91, 34, 0, 42, 29 },
				{ 48, 95, 39, 26, 93, 87, 60, 42, 0, 94 },
				{ 21, 75, 3, 11, 84, 82, 80, 29, 94, 0 }
		};
	}
}
