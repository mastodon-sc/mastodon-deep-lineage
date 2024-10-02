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
