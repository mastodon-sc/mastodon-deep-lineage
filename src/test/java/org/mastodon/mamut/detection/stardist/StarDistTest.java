/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
