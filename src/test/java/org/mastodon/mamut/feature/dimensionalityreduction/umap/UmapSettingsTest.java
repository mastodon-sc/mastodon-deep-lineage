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
package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UmapSettingsTest
{
	private UmapSettings umapSettings;

	@BeforeEach
	void setUp()
	{
		umapSettings = new UmapSettings();
	}

	@Test
	void testGetNumberOfNeighbors()
	{
		assertEquals( UmapSettings.DEFAULT_NUMBER_OF_NEIGHBORS, umapSettings.getNumberOfNeighbors() );
	}

	@Test
	void testGetMinimumDistance()
	{
		assertEquals( UmapSettings.DEFAULT_MINIMUM_DISTANCE, umapSettings.getMinimumDistance() );
	}

	@Test
	void testSetNumberOfNeighbors()
	{
		umapSettings.setNumberOfNeighbors( 10 );
		assertEquals( 10, umapSettings.getNumberOfNeighbors() );
	}

	@Test
	void testSetMinimumDistance()
	{
		umapSettings.setMinimumDistance( 0.5 );
		assertEquals( 0.5, umapSettings.getMinimumDistance() );
	}
}