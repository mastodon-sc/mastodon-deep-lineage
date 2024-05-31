package org.mastodon.mamut.io.importer.labelimage.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

public class LineRenderer
{

	/**
	 * Renders a line in a given image.
	 * <br>
	 * This method takes in the start and end coordinates of a line and a pixel value. It then iterates over each pixel in the image.
	 * If the pixel is on the line, it sets the pixel's value to the given pixel value.
	 *
	 * @param start An array of integers representing the start coordinates of the line.
	 * @param end An array of integers representing the end coordinates of the line.
	 * @param pixelValue A float representing the value to set for pixels on the line.
	 * @param image A RandomAccessibleInterval of FloatType representing the image in which to render the line.
	 */
	public static void renderLine( int[] start, int[] end, float pixelValue, final RandomAccessibleInterval< FloatType > image )
	{
		int[] coord = new int[ 3 ];
		LoopBuilder.setImages( Intervals.positions( image ), image ).forEachPixel( ( position, pixel ) -> {
			position.localize( coord );
			if ( isPointOnLine( coord[ 0 ], coord[ 1 ], coord[ 2 ], start[ 0 ], start[ 1 ], start[ 2 ], end[ 0 ], end[ 1 ], end[ 2 ] ) )
				pixel.setReal( pixelValue );
		} );
	}

	private static boolean isPointOnLine( double pointX, double pointY, double pointZ,
			double startX, double startY, double startZ,
			double endX, double endY, double endZ )
	{
		// Calculate vectors
		double lineVectorX = endX - startX;
		double lineVectorY = endY - startY;
		double lineVectorZ = endZ - startZ;

		double pointVectorX = pointX - startX;
		double pointVectorY = pointY - startY;
		double pointVectorZ = pointZ - startZ;

		// Calculate the cross product of lineVector and pointVector
		double crossProductX = lineVectorY * pointVectorZ - lineVectorZ * pointVectorY;
		double crossProductY = lineVectorZ * pointVectorX - lineVectorX * pointVectorZ;
		double crossProductZ = lineVectorX * pointVectorY - lineVectorY * pointVectorX;

		// Check if the cross product is (0,0,0)
		boolean isCollinear = ( crossProductX == 0 && crossProductY == 0 && crossProductZ == 0 );

		if ( !isCollinear )
			return false; // The point is not on the infinite line

		// Check if the point lies within the segment bounds
		return ( pointX >= Math.min( startX, endX ) && pointX <= Math.max( startX, endX ) ) &&
				( pointY >= Math.min( startY, endY ) && pointY <= Math.max( startY, endY ) ) &&
				( pointZ >= Math.min( startZ, endZ ) && pointZ <= Math.max( startZ, endZ ) );
	}
}
