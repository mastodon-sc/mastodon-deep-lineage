package org.mastodon.mamut.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class MathUtilsTest
{

	@Test
	void testRoundToSignificantDigits()
	{
		assertEquals( 1230, MathUtils.roundToSignificantDigits( 1234.5678, 3 ) );
		assertEquals( -1230, MathUtils.roundToSignificantDigits( -1234.5678, 3 ) );
		assertEquals( 0, MathUtils.roundToSignificantDigits( 0, 3 ) );
		assertEquals( 1, MathUtils.roundToSignificantDigits( 1.2345678, 1 ) );
		assertEquals( 12346000, MathUtils.roundToSignificantDigits( 12345678, 5 ) );
		assertEquals( 0.000123, MathUtils.roundToSignificantDigits( 0.00012345678, 3 ) );
	}
}
