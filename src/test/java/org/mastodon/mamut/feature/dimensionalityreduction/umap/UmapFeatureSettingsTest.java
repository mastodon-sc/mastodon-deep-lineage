package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UmapFeatureSettingsTest
{
	private UmapFeatureSettings umapFeatureSettings;

	@BeforeEach
	void setUp()
	{
		umapFeatureSettings = new UmapFeatureSettings();
	}

	@Test
	void getNumberOfOutputDimensions()
	{
		assertEquals( UmapFeatureSettings.DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS, umapFeatureSettings.getNumberOfOutputDimensions() );
	}

	@Test
	void getNumberOfNeighbors()
	{
		assertEquals( UmapFeatureSettings.DEFAULT_NUMBER_OF_NEIGHBORS, umapFeatureSettings.getNumberOfNeighbors() );
	}

	@Test
	void getMinimumDistance()
	{
		assertEquals( UmapFeatureSettings.DEFAULT_MINIMUM_DISTANCE, umapFeatureSettings.getMinimumDistance() );
	}

	@Test
	void isStandardizeFeatures()
	{
		assertEquals( UmapFeatureSettings.DEFAULT_STANDARDIZE_FEATURES, umapFeatureSettings.isStandardizeFeatures() );
	}

	@Test
	void setNumberOfOutputDimensions()
	{
		umapFeatureSettings.setNumberOfOutputDimensions( 5 );
		assertEquals( 5, umapFeatureSettings.getNumberOfOutputDimensions() );
	}

	@Test
	void setNumberOfNeighbors()
	{
		umapFeatureSettings.setNumberOfNeighbors( 10 );
		assertEquals( 10, umapFeatureSettings.getNumberOfNeighbors() );
	}

	@Test
	void setMinimumDistance()
	{
		umapFeatureSettings.setMinimumDistance( 0.5 );
		assertEquals( 0.5, umapFeatureSettings.getMinimumDistance() );
	}

	@Test
	void setStandardizeFeatures()
	{
		umapFeatureSettings.setStandardizeFeatures( false );
		assertFalse( umapFeatureSettings.isStandardizeFeatures() );
	}
}
