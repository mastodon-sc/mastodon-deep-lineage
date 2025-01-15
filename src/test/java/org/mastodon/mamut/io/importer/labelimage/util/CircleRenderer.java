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
package org.mastodon.mamut.io.importer.labelimage.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

public class CircleRenderer
{

	/**
	 * Renders a circle in a given image on a specified plane (XY, XZ, or YZ).
	 * <br>
	 * This method takes in the center coordinates and radius of a circle, a pixel value, an image, and a plane.
	 * It then creates a slice of the image on the specified plane, and iterates over each pixel in the slice.
	 * If the pixel is inside the circle, it sets the pixel's value to the given pixel value.
	 *
	 * @param center An array of integers representing the center coordinates of the circle.
	 * @param radius A double representing the radius of the circle.
	 * @param pixelValue A float representing the value to set for pixels inside the circle.
	 * @param image A RandomAccessibleInterval of FloatType representing the image in which to render the circle.
	 * @param plane The plane in which the circle lies (XY, XZ, or YZ).
	 * @throws IllegalArgumentException If an unknown plane is provided.
	 */
	public static void renderCircle( int[] center, double radius, float pixelValue,
			final RandomAccessibleInterval< FloatType > image, final Plane plane )
	{
		int[] coord = new int[ 3 ];
		RandomAccessibleInterval< FloatType > slice;
		switch ( plane )
		{
		case XY:
			slice = Views.hyperSlice( image, 2, center[ 2 ] );
			break;
		case XZ:
			slice = Views.hyperSlice( image, 1, center[ 1 ] );
			break;
		case YZ:
			slice = Views.hyperSlice( image, 0, center[ 0 ] );
			break;
		default:
			throw new IllegalArgumentException( "Unknown plane: " + plane );
		}
		LoopBuilder.setImages( Intervals.positions( slice ), slice ).forEachPixel( ( position, pixel ) -> {
			position.localize( coord );
			int[] centerInPlane = { 0, 0, 0 };
			switch ( plane )
			{
			case XY:
				centerInPlane[ 0 ] = center[ 0 ];
				centerInPlane[ 1 ] = center[ 1 ];
				break;
			case XZ:
				centerInPlane[ 0 ] = center[ 0 ];
				centerInPlane[ 1 ] = center[ 2 ];
				break;
			case YZ:
				centerInPlane[ 0 ] = center[ 1 ];
				centerInPlane[ 1 ] = center[ 2 ];
				break;
			}
			if ( SphereRenderer.isPointWithinDistance( coord, centerInPlane, radius ) )
				pixel.setReal( pixelValue );
		} );
	}

	public enum Plane
	{
		XY, XZ, YZ
	}
}
