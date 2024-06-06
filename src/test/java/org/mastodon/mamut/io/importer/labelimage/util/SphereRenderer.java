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
package org.mastodon.mamut.io.importer.labelimage.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

public class SphereRenderer
{

	/**
	 * Renders a sphere in a given image.
	 * This method takes in the center coordinates and radius of a sphere, a pixel value, and an image.
	 * It then iterates over each pixel in the image. If the pixel is inside the sphere, it sets the pixel's value to the given pixel value.
	 *
	 * @param center An array of doubles representing the center coordinates of the sphere.
	 * @param radius A double representing the radius of the sphere.
	 * @param pixelValue A float representing the value to set for pixels inside the sphere.
	 * @param image A RandomAccessibleInterval of FloatType representing the image in which to render the sphere.
	 */
	public static void renderSphere( final int[] center, final double radius, final float pixelValue,
			final RandomAccessibleInterval< FloatType > image )
	{
		int[] coord = new int[ 3 ];
		LoopBuilder.setImages( Intervals.positions( image ), image ).forEachPixel( ( position, pixel ) -> {
			position.localize( coord );
			if ( isPointWithinDistance( coord, center, radius ) )
				pixel.setReal( pixelValue );
		} );
	}

	public static boolean isPointWithinDistance( final int[] point, final int[] center, final double radius )
	{
		// Calculate the square of the distance between the point and the center of the sphere
		double distanceSquared = Math.pow( point[ 0 ] - center[ 0 ], 2 )
				+ Math.pow( point[ 1 ] - center[ 1 ], 2 )
				+ Math.pow( point[ 2 ] - center[ 2 ], 2 );

		// Calculate the square of the radius
		double radiusSquared = Math.pow( radius, 2 );

		// A point is inside the sphere if the distance squared is less than or equal to the radius squared
		return distanceSquared <= radiusSquared;
	}
}
