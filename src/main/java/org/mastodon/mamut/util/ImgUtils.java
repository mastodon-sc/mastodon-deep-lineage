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

import java.util.Arrays;
import java.util.stream.Collectors;

import net.imglib2.RandomAccessibleInterval;
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

public class ImgUtils
{
	private ImgUtils()
	{
		// prevent instantiation
	}

	public static String getImageDimensionsAsString( final RandomAccessibleInterval< ? > image )
	{
		return Arrays.stream( image.dimensionsAsLongArray() ).mapToObj( String::valueOf ).collect( Collectors.joining( ", " ) );
	}

	/**
	 * Computes the size in bytes of the provided {@code RandomAccessibleInterval}.
	 * The size is calculated based on the number of elements in the interval and the memory size
	 * of each element type.
	 *
	 * @param rai the {@code RandomAccessibleInterval} whose theoretical size is to be calculated
	 * @return the size in bytes of the {@code RandomAccessibleInterval}
	 * @throws IllegalArgumentException if the element type of the {@code RandomAccessibleInterval} is not supported.
	 * Supported types are: {@link UnsignedByteType}, {@link ByteType}, {@link UnsignedShortType}, {@link ShortType}, {@link UnsignedIntType},
	 * {@link IntType}, {@link FloatType}, {@link UnsignedLongType}, {@link LongType}, {@link DoubleType}
	 */
	public static long getSizeInBytes( final RandomAccessibleInterval< ? > rai )
	{
		long bytesPerElement;
		Object type = rai.randomAccess().getType();
		if ( type instanceof UnsignedByteType )
			bytesPerElement = 1;
		else if ( type instanceof ByteType )
			bytesPerElement = 1;
		else if ( type instanceof UnsignedShortType || type instanceof ShortType )
			bytesPerElement = 2;
		else if ( type instanceof UnsignedIntType || type instanceof IntType || type instanceof FloatType )
			bytesPerElement = 4;
		else if ( type instanceof UnsignedLongType || type instanceof LongType || type instanceof DoubleType )
			bytesPerElement = 8;
		else
			throw new IllegalArgumentException( "Unknown element type: " + type.getClass() );

		long size = rai.size();
		return size * bytesPerElement;
	}

	/**
	 * Determines whether the given {@code RandomAccessibleInterval} represents a 2D image.
	 * A 2D image is identified as having at most two dimensions with sizes greater than 1.
	 *
	 * @param image the {@code RandomAccessibleInterval} to analyze
	 * @return {@code true} if the image is 2D, otherwise {@code false}
	 */
	public static boolean is2D( final RandomAccessibleInterval< ? > image )
	{
		return !is3D( image );
	}

	/**
	 * Determines whether the given {@code RandomAccessibleInterval} represents a 3D image.
	 * A 3D image is identified as having more at least three dimensions that have sizes greater than 1.
	 *
	 * @param image the {@code RandomAccessibleInterval} to analyze
	 * @return {@code true} if the image is 3D, otherwise {@code false}
	 */
	public static boolean is3D( final RandomAccessibleInterval< ? > image )
	{
		long[] dimensions = image.dimensionsAsLongArray();
		if ( dimensions.length <= 2 )
			return false;
		else
		{
			int nonPlaneDimensionCount = 0;
			for ( final long dimension : dimensions )
			{
				if ( dimension > 1 )
					nonPlaneDimensionCount++;
			}
			return nonPlaneDimensionCount > 2;
		}
	}

	public static String helloWorld()
	{
		return "Hello World!";
	}
}
