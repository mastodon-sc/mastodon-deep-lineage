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
package org.mastodon.mamut.feature.dimensionalityreduction.tsne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TSneSettingsTest
{
	private TSneSettings tSneSettings;

	@BeforeEach
	void setUp()
	{
		tSneSettings = new TSneSettings();
	}

	@Test
	void getPerplexity()
	{
		assertEquals( TSneSettings.DEFAULT_PERPLEXITY, tSneSettings.getPerplexity() );
	}

	@Test
	void testGetMaxIterations()
	{
		assertEquals( TSneSettings.DEFAULT_MAX_ITERATIONS, tSneSettings.getMaxIterations() );
	}

	@Test
	void testSetPerplexity()
	{
		tSneSettings.setPerplexity( 10 );
		assertEquals( 10, tSneSettings.getPerplexity() );
	}

	@Test
	void testSetMaxIterations()
	{
		tSneSettings.setMaxIterations( 20 );
		assertEquals( 20, tSneSettings.getMaxIterations() );
	}

	@Test
	void testIsValidPerplexity()
	{
		assertFalse( tSneSettings.isValidPerplexity( 90 ) );
		assertFalse( tSneSettings.isValidPerplexity( 0 ) );
		assertFalse( tSneSettings.isValidPerplexity( -1 ) );
		assertTrue( tSneSettings.isValidPerplexity( 91 ) );
	}

	@Test
	void testIsValidMaxIterations()
	{
		assertEquals( TSneSettings.DEFAULT_PERPLEXITY, tSneSettings.getMaxValidPerplexity( 91 ) );
		assertEquals( 29, tSneSettings.getMaxValidPerplexity( 90 ) );
		assertEquals( 3, tSneSettings.getMaxValidPerplexity( 10 ) );
	}
}
