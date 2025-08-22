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
