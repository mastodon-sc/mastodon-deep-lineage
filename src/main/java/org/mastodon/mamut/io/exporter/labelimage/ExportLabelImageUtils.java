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
package org.mastodon.mamut.io.exporter.labelimage;

import java.lang.invoke.MethodHandles;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;

import org.mastodon.mamut.feature.EllipsoidIterable;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ImgUtils;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;

public class ExportLabelImageUtils
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private ExportLabelImageUtils()
	{
		// prevent instantiation
	}

	public static RandomAccessibleInterval< IntType > getLabelImageFromSpots( final AffineTransform3D transform, long[] dimensions,
			final int level, final int timepoint, final SpatioTemporalIndex< Spot > spatioTemporalIndex )
	{
		RandomAccessibleInterval< IntType > masksImage = ArrayImgs.ints( dimensions );
		AbstractSource< IntType > masksSource = new RandomAccessibleIntervalSource<>( masksImage, new IntType(), transform, "masks" );
		final EllipsoidIterable< IntType > ellipsoidIterable = new EllipsoidIterable<>( masksSource );
		int spotCount = 0;
		for ( Spot spot : spatioTemporalIndex.getSpatialIndex( timepoint ) )
		{
			ellipsoidIterable.reset( spot, level );
			ellipsoidIterable.forEach( pixel -> pixel.set( spot.getInternalPoolIndex() + 1 ) );
			spotCount++;
		}
		String masksDimensions = ImgUtils.getImageDimensionsAsString( masksImage );
		logger.info( "Wrote {} spot(s) into image with dimensions: {} and type: {} ", spotCount, masksDimensions,
				masksSource.getType().getClass().getSimpleName() );
		return masksImage;
	}
}
