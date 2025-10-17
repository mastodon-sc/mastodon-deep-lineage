// ImgSizeUtilsTest.java
package org.mastodon.mamut.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImgSizeUtilsTest
{

	/**
	 * Tests for the method getSizeInBytes in the ImgSizeUtils class.
	 * This method calculates the size in bytes of a given RandomAccessibleInterval 
	 * based on its type and total number of elements.
	 */
	@Test
	void testGetSizeInBytes_WithUnsignedByteType()
	{
		RandomAccessibleInterval< UnsignedByteType > rai = ArrayImgs.unsignedBytes( 10, 10 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 100, result );
	}

	@Test
	void testGetSizeInBytes_WithByteType()
	{
		RandomAccessibleInterval< ByteType > rai = ArrayImgs.bytes( 5, 20 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 100, result );
	}

	@Test
	void testGetSizeInBytes_WithUnsignedShortType()
	{
		RandomAccessibleInterval< UnsignedShortType > rai = ArrayImgs.unsignedShorts( 5, 4 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 40, result );
	}

	@Test
	void testGetSizeInBytes_WithShortType()
	{
		RandomAccessibleInterval< ShortType > rai = ArrayImgs.shorts( 3, 7 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 42, result );
	}

	@Test
	void testGetSizeInBytes_WithUnsignedIntType()
	{
		RandomAccessibleInterval< UnsignedIntType > rai = ArrayImgs.unsignedInts( 2, 5 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 40, result );
	}

	@Test
	void testGetSizeInBytes_WithIntType()
	{
		RandomAccessibleInterval< IntType > rai = ArrayImgs.ints( 2, 10 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 80, result );
	}

	@Test
	void testGetSizeInBytes_WithFloatType()
	{
		RandomAccessibleInterval< FloatType > rai = ArrayImgs.floats( 10 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 40, result );
	}

	@Test
	void testGetSizeInBytes_WithUnsignedLongType()
	{
		RandomAccessibleInterval< UnsignedLongType > rai = ArrayImgs.unsignedLongs( 5, 2 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 80, result );
	}

	@Test
	void testGetSizeInBytes_WithLongType()
	{
		RandomAccessibleInterval< LongType > rai = ArrayImgs.longs( 4, 5 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 160, result );
	}

	@Test
	void testGetSizeInBytes_WithDoubleType()
	{
		RandomAccessibleInterval< DoubleType > rai = ArrayImgs.doubles( 3, 5 );
		long result = ImgSizeUtils.getSizeInBytes( rai );
		Assertions.assertEquals( 120, result );
	}

	@Test
	void testGetSizeInBytes_WithUnknownType()
	{
		RandomAccessibleInterval< ComplexDoubleType > rai = ArrayImgs.complexDoubles( 10 );
		Assertions.assertThrows( IllegalArgumentException.class, () -> ImgSizeUtils.getSizeInBytes( rai ) );
	}
}
