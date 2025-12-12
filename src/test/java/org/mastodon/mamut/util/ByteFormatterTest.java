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
package org.mastodon.mamut.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormatSymbols;

import org.junit.jupiter.api.Test;

/**
 * Tests for the ByteFormatter class.
 * The class provides a utility method, humanReadableByteCount, 
 * to convert a byte count into a human-readable format.
 */
class ByteFormatterTest
{

	@Test
	void testBytesLessThan1024()
	{
		long inputBytes = 500;
		String expectedOutput = "500 Bytes";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testBytesExactly1024()
	{
		long inputBytes = 1024;
		String expectedOutput = "1" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0 KB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testBytesInKilobytes()
	{
		long inputBytes = 2048;
		String expectedOutput = "2" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0 KB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testBytesInMegabytes()
	{
		long inputBytes = 1024 * 1024;
		String expectedOutput = "1" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0 MB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testBytesInGigabytes()
	{
		long inputBytes = 1024L * 1024 * 1024;
		String expectedOutput = "1" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0 GB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testBytesInTerabytes()
	{
		long inputBytes = 1024L * 1024 * 1024 * 1024;
		String expectedOutput = "1" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0 TB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testBytesInPetabytes()
	{
		long inputBytes = 1024L * 1024 * 1024 * 1024 * 1024;
		String expectedOutput = "1" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0 PB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}

	@Test
	void testLargeByteValue()
	{
		long inputBytes = 123456789L;
		String expectedOutput = "117" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "7 MB";
		String actualOutput = ByteFormatter.humanReadableByteCount( inputBytes );
		assertEquals( expectedOutput, actualOutput );
	}
}
