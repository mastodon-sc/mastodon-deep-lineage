package org.mastodon.mamut.linking.trackastra.appose;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Consumer;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionProps implements AutoCloseable
{
	final ShmImg< IntType > labels;

	final ShmImg< IntType > timepoints;

	final ShmImg< FloatType > coords;

	final ShmImg< FloatType > diameters;

	final ShmImg< FloatType > intensities;

	final ShmImg< FloatType > inertiaTensors;

	final ShmImg< FloatType > borderDists;

	public RegionProps( final List< RegionProps > regionProps )
	{

		try
		{
			// Find max number of entries
			int maxEntries = Integer.MIN_VALUE;
			int coordDimensions = -1;
			int tensorDimensions = -1;
			for ( RegionProps rp : regionProps )
			{
				if ( rp == null )
					continue;
				if ( coordDimensions < 0 )
					coordDimensions = ( int ) rp.coords.dimension( 1 );
				if ( tensorDimensions < 0 )
					tensorDimensions = ( int ) rp.inertiaTensors.dimension( 1 );
				if ( rp.labels.dimension( 0 ) > maxEntries )
					maxEntries = ( int ) rp.labels.dimension( 0 );
			}
			if ( maxEntries <= 0 )
				throw new IllegalArgumentException( "No spots found in any timepoint." );

			// Create arrays big enough to hold all timepoints
			Img< IntType > labelsAllTimepoints = ArrayImgs.ints( regionProps.size(), maxEntries );
			Img< IntType > timepointsAllTimepoints = ArrayImgs.ints( regionProps.size(), maxEntries );
			Img< FloatType > coordsAllTimepoints =
					ArrayImgs.floats( regionProps.size(), maxEntries, coordDimensions );
			Img< FloatType > diametersAllTimepoints = ArrayImgs.floats( regionProps.size(), maxEntries );
			Img< FloatType > intensitiesAllTimepoints = ArrayImgs.floats( regionProps.size(), maxEntries );
			Img< FloatType > inertiaTensorsAllTimepoints =
					ArrayImgs.floats( regionProps.size(), maxEntries, tensorDimensions );
			Img< FloatType > borderDistsAllTimepoints = ArrayImgs.floats( regionProps.size(), maxEntries );

			// Copy data
			for ( int timepoint = 0; timepoint < regionProps.size(); timepoint++ )
			{
				RegionProps rp = regionProps.get( timepoint );
				if ( rp == null )
					continue;
				long numLabelsAtTimepoint = rp.labels.dimension( 0 );
				Interval interval = new FinalInterval( numLabelsAtTimepoint );
				Interval intervalCoords =
						new FinalInterval( new long[] { 0, 0 }, new long[] { numLabelsAtTimepoint - 1, coordDimensions - 1 } );
				Interval intervalTensors =
						new FinalInterval( new long[] { 0, 0 }, new long[] { numLabelsAtTimepoint - 1, tensorDimensions - 1 } );

				RandomAccessibleInterval< IntType > sliceInt = Views.hyperSlice( labelsAllTimepoints, 0, timepoint );
				RandomAccessibleInterval< IntType > restrictedIntervalInt = Views.interval( sliceInt, interval );
				LoopBuilder.setImages( rp.labels, restrictedIntervalInt ).multiThreaded()
						.forEachPixel( ( source, target ) -> target.set( source ) );

				sliceInt = Views.hyperSlice( timepointsAllTimepoints, 0, timepoint );
				restrictedIntervalInt = Views.interval( sliceInt, interval );
				LoopBuilder.setImages( rp.timepoints, restrictedIntervalInt ).multiThreaded()
						.forEachPixel( ( source, target ) -> target.set( source ) );

				RandomAccessibleInterval< FloatType > sliceFloat = Views.hyperSlice( coordsAllTimepoints, 0, timepoint );
				RandomAccessibleInterval< FloatType > restrictedFloat = Views.interval( sliceFloat, intervalCoords );

				LoopBuilder.setImages( rp.coords, restrictedFloat )
						.forEachPixel( ( source, target ) -> target.set( source ) );

				sliceFloat = Views.hyperSlice( diametersAllTimepoints, 0, timepoint );
				restrictedFloat = Views.interval( sliceFloat, interval );

				LoopBuilder.setImages( rp.diameters, restrictedFloat ).multiThreaded()
						.forEachPixel( ( source, target ) -> target.set( source ) );

				sliceFloat = Views.hyperSlice( intensitiesAllTimepoints, 0, timepoint );
				restrictedFloat = Views.interval( sliceFloat, interval );
				LoopBuilder.setImages( rp.intensities, restrictedFloat ).multiThreaded()
						.forEachPixel( ( source, target ) -> target.set( source ) );

				sliceFloat = Views.hyperSlice( inertiaTensorsAllTimepoints, 0, timepoint );
				restrictedFloat = Views.interval( sliceFloat, intervalTensors );
				LoopBuilder.setImages( rp.inertiaTensors, restrictedFloat ).multiThreaded()
						.forEachPixel( ( source, target ) -> target.set( source ) );

				sliceFloat = Views.hyperSlice( borderDistsAllTimepoints, 0, timepoint );
				restrictedFloat = Views.interval( sliceFloat, interval );
				LoopBuilder.setImages( rp.borderDists, restrictedFloat ).multiThreaded()
						.forEachPixel( ( source, target ) -> target.set( source ) );
			}
			// Copy to ShmImgs
			this.labels = ShmImg.copyOf( labelsAllTimepoints );
			this.timepoints = ShmImg.copyOf( timepointsAllTimepoints );
			this.coords = ShmImg.copyOf( coordsAllTimepoints );
			this.diameters = ShmImg.copyOf( diametersAllTimepoints );
			this.intensities = ShmImg.copyOf( intensitiesAllTimepoints );
			this.inertiaTensors = ShmImg.copyOf( inertiaTensorsAllTimepoints );
			this.borderDists = ShmImg.copyOf( borderDistsAllTimepoints );
		}
		finally
		{
			// close shared memory images of this timepoint as they are no longer needed
			regionProps.forEach( rp -> {
				if ( rp != null )
					rp.close();
			} );
		}
	}

	public RegionProps( final ShmImg< IntType > labels, final ShmImg< IntType > timepoints, final ShmImg< FloatType > coords,
			final ShmImg< FloatType > diameters, final ShmImg< FloatType > intensities,
			final ShmImg< FloatType > inertiaTensors, final ShmImg< FloatType > borderDists )
	{
		this.labels = labels;
		this.timepoints = timepoints;
		this.coords = coords;
		this.diameters = diameters;
		this.intensities = intensities;
		this.inertiaTensors = inertiaTensors;
		this.borderDists = borderDists;
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
}
