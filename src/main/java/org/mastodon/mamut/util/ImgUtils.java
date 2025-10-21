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
}
