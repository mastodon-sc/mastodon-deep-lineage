package org.mastodon.mamut.detection.stardist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StarDistTest
{

	/**
	 * Tests for the fromString method of the {@link org.mastodon.mamut.detection.stardist.StarDist.ModelType} enum.
	 * The method is used to retrieve the corresponding ModelType based on the given string.
	 */
	@Test
	void testFromStringWithValidModelTypePlantNuclei3D()
	{
		// Arrange
		String modelName = "StarDist Plant Nuclei 3D ResNet";

		// Act
		StarDist.ModelType result = StarDist.ModelType.fromString( modelName );

		// Assert
		assertEquals( StarDist.ModelType.PLANT_NUCLEI_3D, result );
	}

	@Test
	void testFromStringWithValidModelTypeFluo2D()
	{
		// Arrange
		String modelName = "StarDist Fluorescence Nuclei Segmentation";

		// Act
		StarDist.ModelType result = StarDist.ModelType.fromString( modelName );

		// Assert
		assertEquals( StarDist.ModelType.FLUO_2D, result );
	}

	@Test
	void testFromStringWithValidModelTypeDemo()
	{
		// Arrange
		String modelName = "StarDist Demo";

		// Act
		StarDist.ModelType result = StarDist.ModelType.fromString( modelName );

		// Assert
		assertEquals( StarDist.ModelType.DEMO, result );
	}

	@Test
	void testFromStringWithInvalidModelName()
	{
		// Arrange
		String invalidModelName = "Nonexistent Model";

		// Act and Assert
		Exception exception = assertThrows( IllegalArgumentException.class, () -> StarDist.ModelType.fromString( invalidModelName ) );
		assertEquals( "No enum constant for model name: Nonexistent Model", exception.getMessage() );
	}

	@Test
	void testFromStringCaseInsensitiveMatch()
	{
		// Arrange
		String modelName = "stardist plant nuclei 3d resnet";

		// Act
		StarDist.ModelType result = StarDist.ModelType.fromString( modelName );

		// Assert
		assertEquals( StarDist.ModelType.PLANT_NUCLEI_3D, result );
	}
}
