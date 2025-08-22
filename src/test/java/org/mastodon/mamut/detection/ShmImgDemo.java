package org.mastodon.mamut.detection;

import net.imglib2.appose.ShmImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;

import org.mastodon.mamut.util.ByteFormatter;
import org.mastodon.mamut.util.ImgSizeUtils;

public class ShmImgDemo
{
	public static void main( String[] args )
	{
		ArrayImg< ?, ? > works = ArrayImgs.unsignedShorts( 1024, 1024, 1023 );
		long size = ImgSizeUtils.getSizeInBytes( works );
		String formattedSize = ByteFormatter.humanReadableByteCount( size );
		System.out.println( "Size of the image: " + size + " bytes (" + formattedSize + ")" );
		try (ShmImg< ? > shmImg = ShmImg.copyOf( works ))
		{
			System.out.println( "ShmImg: " + shmImg );
		}
		ArrayImg< ?, ? > doesNotWork = ArrayImgs.unsignedShorts( 1024, 1024, 1024 ); // does not work, size exceeds Integer.MAX_VALUE
		try (ShmImg< ? > shmImg = ShmImg.copyOf( doesNotWork ))
		{
			System.out.println( "ShmImg: " + shmImg );
		}
	}
}
