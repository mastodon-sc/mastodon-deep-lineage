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
