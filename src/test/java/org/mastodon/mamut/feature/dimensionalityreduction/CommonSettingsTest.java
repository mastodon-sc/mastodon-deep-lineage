/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
