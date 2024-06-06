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

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.FloatType;
import org.mastodon.mamut.feature.EllipsoidIterable;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class SpotRenderer
{
	private static final double ELLIPSOID_INFLATE = 0.5d;

	private static void renderSpot( final Spot spot, final int pixelValue, final Img< FloatType > img )
	{
		AbstractSource< FloatType > source =
				new RandomAccessibleIntervalSource<>( img, new FloatType(), new AffineTransform3D(), "Ellipsoids" );
		renderSpot( spot, pixelValue, source );
	}

	public static void renderSpot( final Spot spot, final int pixelValue, final AbstractSource< FloatType > source )
	{
		final EllipsoidIterable< FloatType > ellipsoidIterable = new EllipsoidIterable<>( source );
		ellipsoidIterable.reset( spot );
		ellipsoidIterable.forEach( pixel -> pixel.set( pixelValue ) );
	}

	public static void renderSphere( final double[] center, final double radius, final int pixelValue, final Img< FloatType > img,
			final ModelGraph graph )
	{
		renderSpot( graph.addVertex().init( 0, center, radius - ELLIPSOID_INFLATE ), pixelValue, img );
	}

	public static void renderCircle( final double[] center, final double radius, final Plane plane, final int pixelValue,
			final Img< FloatType > img, final ModelGraph graph )
	{
		double radiusSquared = ( radius - ELLIPSOID_INFLATE ) * ( radius - ELLIPSOID_INFLATE );
		double[][] cov = new double[ 3 ][ 3 ];
		for ( int i = 0; i < 3; ++i )
		{
			for ( int j = 0; j < 3; ++j )
			{
				if ( i == j )
				{
					if ( i == 0 && ( plane.equals( Plane.XY ) || plane.equals( Plane.XZ ) ) )
						cov[ i ][ j ] = radiusSquared;
					else if ( i == 1 && ( plane.equals( Plane.YZ ) || plane.equals( Plane.XY ) ) )
						cov[ i ][ j ] = radiusSquared;
					else if ( i == 2 && ( plane.equals( Plane.XZ ) || plane.equals( Plane.YZ ) ) )
						cov[ i ][ j ] = radiusSquared;
					else
						cov[ i ][ j ] = 0.01d;
				}
				else
					cov[ i ][ j ] = 0.0d;
			}
		}
		Spot circle = graph.addVertex().init( 0, center, cov );
		renderSpot( circle, pixelValue, img );
	}

	public static void renderLine( final double[] center, final double length, final Axis axis, final int pixelValue,
			final Img< FloatType > img,
			final ModelGraph graph )
	{
		double[][] cov = new double[ 3 ][ 3 ];
		double halfLengthSquared = ( length / 2 - ELLIPSOID_INFLATE ) * ( length / 2 - ELLIPSOID_INFLATE );
		double minimalCov = 0.01d;
		cov[ 0 ][ 0 ] = axis.equals( Axis.X ) ? halfLengthSquared : minimalCov;
		cov[ 1 ][ 1 ] = axis.equals( Axis.Y ) ? halfLengthSquared : minimalCov;
		cov[ 2 ][ 2 ] = axis.equals( Axis.Z ) ? halfLengthSquared : minimalCov;
		Spot line = graph.addVertex().init( 0, center, cov );
		renderSpot( line, pixelValue, img );
	}

	public enum Plane
	{
		XY,
		XZ,
		YZ
	}

	public enum Axis
	{
		X,
		Y,
		Z
	}
}
