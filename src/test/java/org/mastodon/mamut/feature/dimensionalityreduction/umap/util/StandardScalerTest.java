package org.mastodon.mamut.feature.dimensionalityreduction.umap.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StandardScalerTest
{
	@Test
	void scaleVector_withZeroVariance()
	{
		double[] vector = new double[] { 3, 3, 3, 3, 3 };
		double[] scaledVector = StandardScaler.standardizeVector( vector );
		assertArrayEquals( new double[] { 0, 0, 0, 0, 0 }, scaledVector );
	}

	@Test
	void standardizeVector()
	{
		double[] vector = new double[] { 1, 2, 3, 4, 5 };
		double[] scaledVector = StandardScaler.standardizeVector( vector );
		assertArrayEquals( new double[] { -1.2649110640673518, -0.6324555320336759, 0, 0.6324555320336759, 1.2649110640673518 },
				scaledVector );
	}

	@Test
	void standardizeVector_withNegativeValues()
	{
		double[] vector = new double[] { -1, -2, -3, -4, -5 };
		double[] scaledVector = StandardScaler.standardizeVector( vector );
		assertArrayEquals( new double[] { 1.2649110640673518, 0.6324555320336759, 0, -0.6324555320336759, -1.2649110640673518 },
				scaledVector );
	}

	@Test
	void standardizeVector_withSingleValue()
	{
		double[] vector = new double[] { 42 };
		double[] scaledVector = StandardScaler.standardizeVector( vector );
		assertArrayEquals( new double[] { 0 }, scaledVector );
	}

	@Test
	void standardizeVector_withEmptyVector()
	{
		double[] vector = new double[] {};
		double[] scaledVector = StandardScaler.standardizeVector( vector );
		assertArrayEquals( new double[] {}, scaledVector );
	}

	@Test
	void testScaleVector()
	{
		double[] vector = new double[] { 1, 2, 3, 4, 5 };
		double mean = 3;
		double variance = 2;
		double[] scaledVector = StandardScaler.scaleVector( vector, mean, variance );
		assertArrayEquals( scaledVector,
				new double[] { -1.414213562373095, -0.7071067811865475, 0, 0.7071067811865475, 1.414213562373095 } );
	}
}
