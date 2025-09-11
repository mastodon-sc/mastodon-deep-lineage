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
		logger.info( "Wrote {} spots into image with dimensions: {} and type: {} ", spotCount, masksDimensions,
				masksSource.getType().getClass().getSimpleName() );
		return masksImage;
	}
}
