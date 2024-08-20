package org.mastodon.mamut.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class MathUtilsTest
{

	@Test
	void testRoundToSignificantDigits()
	{
		assertEquals( "1230", MathUtils.roundToSignificantDigits( 1234.5678, 3 ) );
		assertEquals( "-1230", MathUtils.roundToSignificantDigits( -1234.5678, 3 ) );
		assertEquals( "0", MathUtils.roundToSignificantDigits( 0, 3 ) );
		assertEquals( "1", MathUtils.roundToSignificantDigits( 1.2345678, 1 ) );
		assertEquals( "12346000", MathUtils.roundToSignificantDigits( 12345678, 5 ) );
		assertEquals( "1.235", MathUtils.roundToSignificantDigits( 1.2345678, 4 ) );
		assertEquals( "0.000123", MathUtils.roundToSignificantDigits( 0.00012345678, 3 ) );
		assertEquals( "0.6", MathUtils.roundToSignificantDigits( 0.5678, 1 ) );
	}

	@Test
	void testCountZerosAfterDecimalPoint()
	{
		assertEquals( 0, MathUtils.countZerosAfterDecimalPoint( 5 ) );
		assertEquals( 0, MathUtils.countZerosAfterDecimalPoint( 0.1 ) );
		assertEquals( 1, MathUtils.countZerosAfterDecimalPoint( 0.01 ) );
		assertEquals( 2, MathUtils.countZerosAfterDecimalPoint( -0.003 ) );
	}

	@Test
	void testDivideAndRoundUp()
	{
		assertEquals( 1, MathUtils.divideAndRoundUp( 1, 1 ) );
		assertEquals( 1, MathUtils.divideAndRoundUp( 1, 10 ) );
		assertEquals( 1, MathUtils.divideAndRoundUp( 10, 10 ) );
		assertEquals( 10, MathUtils.divideAndRoundUp( 10, 1 ) );
		assertEquals( 2, MathUtils.divideAndRoundUp( 11, 10 ) );
		assertEquals( 2, MathUtils.divideAndRoundUp( 20, 10 ) );
	}
}
