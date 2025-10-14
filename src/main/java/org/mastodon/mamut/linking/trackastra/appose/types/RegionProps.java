package org.mastodon.mamut.linking.trackastra.appose.types;

import java.util.List;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class RegionProps implements AutoCloseable
{
	public final ShmImg< IntType > labels;

	public final ShmImg< IntType > timepoints;

	public final ShmImg< FloatType > coords;

	public final ShmImg< FloatType > diameters;

	public final ShmImg< FloatType > intensities;

	public final ShmImg< FloatType > inertiaTensors;

	public final ShmImg< FloatType > borderDists;

	public RegionProps( final List< SingleTimepointRegionProps > singleTimepointRegionProps )
	{

		if ( singleTimepointRegionProps == null || singleTimepointRegionProps.isEmpty() )
		{
			throw new IllegalArgumentException( "No timepoints provided." );
		}

		int[] dimensions = computeDimensions( singleTimepointRegionProps );
		int maxEntries = dimensions[ 0 ];
		int coordDims = dimensions[ 1 ];
		int tensorDims = dimensions[ 2 ];
		int numTimepoints = singleTimepointRegionProps.size();

		// Allocate arrays
		Img< IntType > labelsAll = ArrayImgs.ints( numTimepoints, maxEntries );
		Img< IntType > timepointsAll = ArrayImgs.ints( numTimepoints, maxEntries );
		Img< FloatType > coordsAll = ArrayImgs.floats( numTimepoints, maxEntries, coordDims );
		Img< FloatType > diametersAll = ArrayImgs.floats( numTimepoints, maxEntries );
		Img< FloatType > intensitiesAll = ArrayImgs.floats( numTimepoints, maxEntries );
		Img< FloatType > inertiaTensorsAll = ArrayImgs.floats( numTimepoints, maxEntries, tensorDims );
		Img< FloatType > borderDistsAll = ArrayImgs.floats( numTimepoints, maxEntries );

		DataArrays arrays =
				new DataArrays( labelsAll, timepointsAll, coordsAll, diametersAll, intensitiesAll, inertiaTensorsAll, borderDistsAll );
		DimensionInfo dimensionInfo = new DimensionInfo( coordDims, tensorDims );
		Data data = new Data( arrays, dimensionInfo );

		try
		{
			// Copy data from single timepoints
			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{
				SingleTimepointRegionProps regionProps = singleTimepointRegionProps.get( timepoint );
				if ( regionProps != null )
					copyTimepointData( regionProps, timepoint, data );
			}
			// Copy to ShmImgs
			this.labels = ShmImg.copyOf( data.arrays.labels );
			this.timepoints = ShmImg.copyOf( data.arrays.timepoints );
			this.coords = ShmImg.copyOf( data.arrays.coords );
			this.diameters = ShmImg.copyOf( data.arrays.diameters );
			this.intensities = ShmImg.copyOf( data.arrays.intensities );
			this.inertiaTensors = ShmImg.copyOf( data.arrays.inertiaTensors );
			this.borderDists = ShmImg.copyOf( data.arrays.borderDists );
		}
		finally
		{
			// close shared memory images of this timepoint as they are no longer needed
			singleTimepointRegionProps.forEach( rp -> {
				if ( rp != null )
					rp.close();
			} );
		}
	}

	@Override
	public void close()
	{
		labels.close();
		timepoints.close();
		coords.close();
		diameters.close();
		intensities.close();
		inertiaTensors.close();
		borderDists.close();
	}

	private static int[] computeDimensions( final List< SingleTimepointRegionProps > props )
	{
		int maxEntries = Integer.MIN_VALUE;
		int coordDims = -1;
		int tensorDims = -1;

		for ( SingleTimepointRegionProps rp : props )
		{
			if ( rp == null )
				continue;
			if ( coordDims < 0 )
				coordDims = ( int ) rp.coords.dimension( 1 );
			if ( tensorDims < 0 )
				tensorDims = ( int ) rp.inertiaTensors.dimension( 1 );
			if ( rp.labels.dimension( 0 ) > maxEntries )
				maxEntries = ( int ) rp.labels.dimension( 0 );
		}

		if ( maxEntries <= 0 )
			throw new IllegalArgumentException( "No spots found in any timepoint." );

		return new int[] { maxEntries, coordDims, tensorDims };
	}

	private static < T extends NativeType< T > & RealType< T > > void copySlice( final RandomAccessibleInterval< T > source,
			final RandomAccessibleInterval< T > target )
	{
		LoopBuilder.setImages( source, target ).multiThreaded().forEachPixel( ( s, t ) -> t.set( s ) );
	}

	private static Interval createInterval( final long size )
	{
		return new FinalInterval( size );
	}

	private static Interval createInterval( final long rows, final long cols )
	{
		return new FinalInterval( new long[] { 0, 0 }, new long[] { rows - 1, cols - 1 } );
	}

	private static void copyTimepointData( final SingleTimepointRegionProps rp, final int timepoint, final Data data )
	{
		long nLabels = rp.labels.dimension( 0 );
		DataArrays arrays = data.arrays;
		DimensionInfo dims = data.dims;

		copySlice( rp.labels, Views.interval( Views.hyperSlice( arrays.labels, 0, timepoint ), createInterval( nLabels ) ) );
		copySlice( rp.timepoints, Views.interval( Views.hyperSlice( arrays.timepoints, 0, timepoint ), createInterval( nLabels ) ) );
		copySlice( rp.coords,
				Views.interval( Views.hyperSlice( arrays.coords, 0, timepoint ), createInterval( nLabels, dims.coordDims ) ) );
		copySlice( rp.diameters, Views.interval( Views.hyperSlice( arrays.diameters, 0, timepoint ), createInterval( nLabels ) ) );
		copySlice( rp.intensities, Views.interval( Views.hyperSlice( arrays.intensities, 0, timepoint ), createInterval( nLabels ) ) );
		copySlice( rp.inertiaTensors,
				Views.interval( Views.hyperSlice( arrays.inertiaTensors, 0, timepoint ), createInterval( nLabels, dims.tensorDims ) ) );
		copySlice( rp.borderDists, Views.interval( Views.hyperSlice( arrays.borderDists, 0, timepoint ), createInterval( nLabels ) ) );
	}

	private static class DataArrays
	{
		final Img< IntType > labels;

		final Img< IntType > timepoints;

		final Img< FloatType > coords;

		final Img< FloatType > diameters;

		final Img< FloatType > intensities;

		final Img< FloatType > inertiaTensors;

		final Img< FloatType > borderDists;

		DataArrays( final Img< IntType > labels, final Img< IntType > timepoints, final Img< FloatType > coords,
				final Img< FloatType > diameters, final Img< FloatType > intensities, final Img< FloatType > inertiaTensors,
				final Img< FloatType > borderDists )
		{
			this.labels = labels;
			this.timepoints = timepoints;
			this.coords = coords;
			this.diameters = diameters;
			this.intensities = intensities;
			this.inertiaTensors = inertiaTensors;
			this.borderDists = borderDists;
		}
	}

	private static class DimensionInfo
	{
		final int coordDims;

		final int tensorDims;

		DimensionInfo( final int coordDims, final int tensorDims )
		{
			this.coordDims = coordDims;
			this.tensorDims = tensorDims;
		}
	}

	private static class Data
	{
		final DataArrays arrays;

		final DimensionInfo dims;

		Data( final DataArrays arrays, final DimensionInfo dims )
		{
			this.arrays = arrays;
			this.dims = dims;
		}
	}
}
