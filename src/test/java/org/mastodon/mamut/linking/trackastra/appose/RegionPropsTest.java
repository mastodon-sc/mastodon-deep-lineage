package org.mastodon.mamut.linking.trackastra.appose;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionPropsTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Test
	void testCopyImages()
	{
		Img< FloatType > img0 = ArrayImgs.floats( 94, 3 );
		Img< FloatType > img1 = ArrayImgs.floats( 94, 3 );
		Img< FloatType > img2 = ArrayImgs.floats( 95, 3 );
		img2.getAt( 93, 2 ).setReal( 42 );
		img2.getAt( 94, 2 ).setReal( 42 );

		Img< FloatType > data = ArrayImgs.floats( 3, 95, 3 );

		try (ShmImg< FloatType > shm0 = ShmImg.copyOf( img0 );
				ShmImg< FloatType > shm1 = ShmImg.copyOf( img1 );
				ShmImg< FloatType > shm2 = ShmImg.copyOf( img2 ))
		{
			List< ShmImg< FloatType > > list = Arrays.asList( shm0, shm1, shm2 );

			for ( int i = 0; i < list.size(); i++ )
			{
				ShmImg< FloatType > shm = list.get( i );
				logger.info( "shm: {}, dims: {}", i, Arrays.toString( shm.dimensionsAsLongArray() ) );
				RandomAccessibleInterval< FloatType > slice = Views.hyperSlice( data, 0, i );
				RandomAccessibleInterval< FloatType > restrictedSlice =
						Views.interval( slice, new long[] { 0, 0 }, new long[] { shm.dimension( 0 ) - 1, shm.dimension( 1 ) - 1 } );
				logger.info( "restrictedSlice dims: {}", Arrays.toString( Intervals.dimensionsAsLongArray( restrictedSlice ) ) );
				LoopBuilder.setImages( shm, restrictedSlice ).multiThreaded().forEachPixel( ( source, target ) -> target.set( source ) );
			}
		}

		assertEquals( 42, img2.getAt( 93, 2 ).get(), 0.0 );
		assertEquals( 42, img2.getAt( 94, 2 ).get(), 0.0 );
		assertEquals( 42, data.getAt( 2, 93, 2 ).get(), 0.0 );
		assertEquals( 42, data.getAt( 2, 94, 2 ).get(), 0.0 );
	}

	@Test
	void testRegionProps()
	{
		Img< IntType > labels0 = ArrayImgs.ints( 94 );
		Img< IntType > timepoints0 = ArrayImgs.ints( 94 );
		Img< FloatType > coords0 = ArrayImgs.floats( 94, 3 );
		Img< FloatType > diameters0 = ArrayImgs.floats( 94 );
		Img< FloatType > intensities0 = ArrayImgs.floats( 94 );
		Img< FloatType > inertiaTensors0 = ArrayImgs.floats( 94, 9 );
		Img< FloatType > borderDists0 = ArrayImgs.floats( 94 );

		Img< IntType > labels1 = ArrayImgs.ints( 94 );
		Img< IntType > timepoints1 = ArrayImgs.ints( 94 );
		Img< FloatType > coords1 = ArrayImgs.floats( 94, 3 );
		Img< FloatType > diameters1 = ArrayImgs.floats( 94 );
		Img< FloatType > intensities1 = ArrayImgs.floats( 94 );
		Img< FloatType > inertiaTensors1 = ArrayImgs.floats( 94, 9 );
		Img< FloatType > borderDists1 = ArrayImgs.floats( 94 );

		Img< IntType > labels2 = ArrayImgs.ints( 95 );
		Img< IntType > timepoints2 = ArrayImgs.ints( 95 );
		Img< FloatType > coords2 = ArrayImgs.floats( 95, 3 );
		Img< FloatType > diameters2 = ArrayImgs.floats( 95 );
		Img< FloatType > intensities2 = ArrayImgs.floats( 95 );
		Img< FloatType > inertiaTensors2 = ArrayImgs.floats( 95, 9 );
		Img< FloatType > borderDists2 = ArrayImgs.floats( 95 );

		try (ShmImg< IntType > shmLabels0 = new ShmImg<>( NDArrays.asNDArray( labels0 ) );
				ShmImg< IntType > shmTimepoint0 = new ShmImg<>( NDArrays.asNDArray( timepoints0 ) );
				ShmImg< FloatType > shmCoords0 = new ShmImg<>( NDArrays.asNDArray( coords0 ) );
				ShmImg< FloatType > shmDiameters0 = new ShmImg<>( NDArrays.asNDArray( diameters0 ) );
				ShmImg< FloatType > shmIntensities0 = new ShmImg<>( NDArrays.asNDArray( intensities0 ) );
				ShmImg< FloatType > shmInertiaTensors0 = new ShmImg<>( NDArrays.asNDArray( inertiaTensors0 ) );
				ShmImg< FloatType > shmBorderDists0 = new ShmImg<>( NDArrays.asNDArray( borderDists0 ) );
				ShmImg< IntType > shmLabels1 = new ShmImg<>( NDArrays.asNDArray( labels1 ) );
				ShmImg< IntType > shmTimepoint1 = new ShmImg<>( NDArrays.asNDArray( timepoints1 ) );
				ShmImg< FloatType > shmCoords1 = new ShmImg<>( NDArrays.asNDArray( coords1 ) );
				ShmImg< FloatType > shmDiameters1 = new ShmImg<>( NDArrays.asNDArray( diameters1 ) );
				ShmImg< FloatType > shmIntensities1 = new ShmImg<>( NDArrays.asNDArray( intensities1 ) );
				ShmImg< FloatType > shmInertiaTensors1 = new ShmImg<>( NDArrays.asNDArray( inertiaTensors1 ) );
				ShmImg< FloatType > shmBorderDists1 = new ShmImg<>( NDArrays.asNDArray( borderDists1 ) );
				ShmImg< IntType > shmLabels2 = new ShmImg<>( NDArrays.asNDArray( labels2 ) );
				ShmImg< IntType > shmTimepoint2 = new ShmImg<>( NDArrays.asNDArray( timepoints2 ) );
				ShmImg< FloatType > shmCoords2 = new ShmImg<>( NDArrays.asNDArray( coords2 ) );
				ShmImg< FloatType > shmDiameters2 = new ShmImg<>( NDArrays.asNDArray( diameters2 ) );
				ShmImg< FloatType > shmIntensities2 = new ShmImg<>( NDArrays.asNDArray( intensities2 ) );
				ShmImg< FloatType > shmInertiaTensors2 = new ShmImg<>( NDArrays.asNDArray( inertiaTensors2 ) );
				ShmImg< FloatType > shmBorderDists2 = new ShmImg<>( NDArrays.asNDArray( borderDists2 ) )
		)
		{
			logger.info( "labels0 dims: {}", Arrays.toString( shmLabels0.dimensionsAsLongArray() ) );
			logger.info( "timepoint0 dims: {}", Arrays.toString( shmTimepoint0.dimensionsAsLongArray() ) );
			logger.info( "coords0 dims: {}", Arrays.toString( shmCoords0.dimensionsAsLongArray() ) );
			logger.info( "diameters0 dims: {}", Arrays.toString( shmDiameters0.dimensionsAsLongArray() ) );
			logger.info( "intensities0 dims: {}", Arrays.toString( shmIntensities0.dimensionsAsLongArray() ) );
			logger.info( "inertiaTensors0 dims: {}", Arrays.toString( shmInertiaTensors0.dimensionsAsLongArray() ) );
			logger.info( "borderDists0 dims: {}", Arrays.toString( shmBorderDists0.dimensionsAsLongArray() ) );
			logger.info( "labels0 class: {}", shmLabels0.getImg().getClass() );
			logger.info( "timepoint0 class: {}", shmTimepoint0.getImg().getClass() );
			logger.info( "coords0 class: {}", shmCoords0.getImg().getClass() );
			logger.info( "diameters0 class: {}", shmDiameters0.getImg().getClass() );
			logger.info( "intensities0 class: {}", shmIntensities0.getImg().getClass() );
			logger.info( "inertiaTensors0 class: {}", shmInertiaTensors0.getImg().getClass() );
			logger.info( "borderDists0 class: {}", shmBorderDists0.getImg().getClass() );

			RegionProps rp0 = new RegionProps( shmLabels0, shmTimepoint0, shmCoords0, shmDiameters0, shmIntensities0, shmInertiaTensors0,
					shmBorderDists0 );
			RegionProps rp1 = new RegionProps( shmLabels1, shmTimepoint1, shmCoords1, shmDiameters1, shmIntensities1, shmInertiaTensors1,
					shmBorderDists1 );
			RegionProps rp2 = new RegionProps( shmLabels2, shmTimepoint2, shmCoords2, shmDiameters2, shmIntensities2, shmInertiaTensors2,
					shmBorderDists2 );
			List< RegionProps > list = Arrays.asList( rp0, rp1, rp2 );
			try (RegionProps all = new RegionProps( list ))
			{
				assertEquals( 3, all.labels.dimensionsAsLongArray()[ 0 ] );
				assertEquals( 95, all.labels.dimensionsAsLongArray()[ 1 ] );
			}
		}

	}
}
