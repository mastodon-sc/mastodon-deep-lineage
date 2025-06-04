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
package org.mastodon.mamut.detection.cellpose;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Cellpose3Test
{
	/**
	 * Tests the fromString method of the {@link org.mastodon.mamut.detection.cellpose.Cellpose3.ModelType} enum.
	 * This method is used to convert a string representation of a model name
	 * into its corresponding ModelType enum constant.
	 * It ensures that the method correctly identifies existing enum constants,
	 * while throwing an exception for invalid or non-matching model names.
	 */
	@Test
	void testFromStringWithValidModelName()
	{
		Cellpose3.ModelType result = Cellpose3.ModelType.fromString( "cyto3" );
		assertEquals( Cellpose3.ModelType.CYTO3, result, "Expected ModelType.CYTO3 for input 'cyto3'" );
	}

	@Test
	void testFromStringWithValidModelNameCaseInsensitive()
	{
		Cellpose3.ModelType result = Cellpose3.ModelType.fromString( "CYTO3" );
		assertEquals( Cellpose3.ModelType.CYTO3, result, "Expected ModelType.CYTO3 for input 'CYTO3'" );
	}

	@Test
	void testFromStringWithAnotherValidModelName()
	{
		Cellpose3.ModelType result = Cellpose3.ModelType.fromString( "nuclei" );
		assertEquals( Cellpose3.ModelType.NUCLEI, result, "Expected ModelType.NUCLEI for input 'nuclei'" );
	}

	@Test
	void testFromStringWithValidModelNameContainingUnderscore()
	{
		Cellpose3.ModelType result = Cellpose3.ModelType.fromString( "cyto2_cp3" );
		assertEquals( Cellpose3.ModelType.CYTO2_CP3, result, "Expected ModelType.CYTO2_CP3 for input 'cyto2_cp3'" );
	}

	@Test
	void testFromStringWithInvalidModelName()
	{
		Exception exception = assertThrows(
				IllegalArgumentException.class, () -> {
					Cellpose3.ModelType.fromString( "invalid_model_name" );
				}
		);
		assertEquals( "No enum constant for model name: invalid_model_name", exception.getMessage() );
	}

	@Test
	void testFromStringWithEmptyString()
	{
		Exception exception = assertThrows(
				IllegalArgumentException.class, () -> {
					Cellpose3.ModelType.fromString( "" );
				}
		);
		assertEquals( "No enum constant for model name: ", exception.getMessage() );
	}

	@Test
	void testFromStringWithNullInput()
	{
		Exception exception = assertThrows(
				IllegalArgumentException.class, () -> {
					Cellpose3.ModelType.fromString( null );
				}
		);
		assertEquals( IllegalArgumentException.class, exception.getClass(), "Expected IllegalArgumentException for null input." );
	}
}
