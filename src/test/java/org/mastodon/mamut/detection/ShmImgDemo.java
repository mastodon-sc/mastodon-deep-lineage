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
package org.mastodon.mamut.detection;

import net.imglib2.appose.ShmImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;

import org.mastodon.mamut.util.ByteFormatter;
import org.mastodon.mamut.util.ImgUtils;

public class ShmImgDemo
{
	public static void main( String[] args )
	{
		ArrayImg< ?, ? > works = ArrayImgs.unsignedShorts( 1024, 1024, 1023 );
		long size = ImgUtils.getSizeInBytes( works );
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
