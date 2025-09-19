package org.mastodon.mamut.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.imglib2.RandomAccessibleInterval;

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

}
