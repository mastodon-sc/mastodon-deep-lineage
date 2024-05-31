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
	public static void renderSphere( final double[] center, final double radius, final float pixelValue,
			final RandomAccessibleInterval< FloatType > image )
	{
		double[] coord = new double[ 3 ];
		LoopBuilder.setImages( Intervals.positions( image ), image ).forEachPixel( ( position, pixel ) -> {
			position.localize( coord );
			if ( isPointInsideSphere( coord, center, radius ) )
				pixel.setReal( pixelValue );
		} );
	}

	private static boolean isPointInsideSphere( final double[] point, final double[] center, final double radius )
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
