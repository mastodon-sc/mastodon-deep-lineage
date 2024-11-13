package org.mastodon.mamut.feature.dimensionalityreduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommonSettingsTest
{
	private CommonSettings commonSettings;

	@BeforeEach
	void setUp()
	{
		commonSettings = new CommonSettings();
	}

	@Test
	void getNumberOfOutputDimensions()
	{
		assertEquals( CommonSettings.DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS, commonSettings.getNumberOfOutputDimensions() );
	}

	@Test
	void isStandardizeFeatures()
	{
		assertEquals( CommonSettings.DEFAULT_STANDARDIZE_FEATURES, commonSettings.isStandardizeFeatures() );
	}

	@Test
	void setNumberOfOutputDimensions()
	{
		commonSettings.setNumberOfOutputDimensions( 5 );
		assertEquals( 5, commonSettings.getNumberOfOutputDimensions() );
	}

	@Test
	void setStandardizeFeatures()
	{
		commonSettings.setStandardizeFeatures( false );
		assertFalse( commonSettings.isStandardizeFeatures() );
	}
}
