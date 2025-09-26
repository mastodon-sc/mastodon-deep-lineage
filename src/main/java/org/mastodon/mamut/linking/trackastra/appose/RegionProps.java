package org.mastodon.mamut.linking.trackastra.appose;

import java.util.List;

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
		// Find max number of entries
		int maxEntries = Integer.MIN_VALUE;
		for ( RegionProps rp : regionProps )
		{
			if ( rp.labels.dimension( 0 ) > maxEntries )
				maxEntries = ( int ) rp.labels.dimension( 0 );
		}

		// Create arrays big enough to hold all timepoints
		Img< IntType > labelsAllTimepoints = ArrayImgs.ints( regionProps.size(), maxEntries );
		Img< IntType > timepointsAllTimepoints = ArrayImgs.ints( regionProps.size(), maxEntries );
		Img< FloatType > coordsAllTimepoints =
				ArrayImgs.floats( regionProps.size(), maxEntries, regionProps.get( 0 ).coords.dimension( 1 ) );
		Img< FloatType > diametersAllTimepoints = ArrayImgs.floats( regionProps.size(), maxEntries );
		Img< FloatType > intensitiesAllTimepoints = ArrayImgs.floats( regionProps.size(), maxEntries );
		Img< FloatType > inertiaTensorsAllTimepoints =
				ArrayImgs.floats( regionProps.size(), maxEntries, regionProps.get( 0 ).inertiaTensors.dimension( 1 ) );
		Img< FloatType > borderDistsAllTimepoints = ArrayImgs.floats( regionProps.size(), maxEntries );

		// Copy data
		for ( int timepoint = 0; timepoint < regionProps.size(); timepoint++ )
		{
			ShmImg< IntType > labelsAtTimepoint = regionProps.get( timepoint ).labels;
			long numLabelsAtTimepoint = labelsAtTimepoint.dimension( 0 );
			Interval interval = new FinalInterval( numLabelsAtTimepoint );
			long numDimensions = regionProps.get( 0 ).coords.dimension( 1 );
			Interval interval3 = new FinalInterval( new long[] { 0, 0 }, new long[] { numLabelsAtTimepoint - 1, numDimensions - 1 } );
			long numTensors = regionProps.get( 0 ).inertiaTensors.dimension( 1 );
			Interval interval9 = new FinalInterval( new long[] { 0, 0 }, new long[] { numLabelsAtTimepoint - 1, numTensors - 1 } );

			RandomAccessibleInterval< IntType > sliceInt = Views.hyperSlice( labelsAllTimepoints, 0, timepoint );
			RandomAccessibleInterval< IntType > restrictedIntervalInt = Views.interval( sliceInt, interval );
			LoopBuilder.setImages( regionProps.get( timepoint ).labels, restrictedIntervalInt ).multiThreaded()
					.forEachPixel( ( source, target ) -> target.set( source ) );

			sliceInt = Views.hyperSlice( timepointsAllTimepoints, 0, timepoint );
			restrictedIntervalInt = Views.interval( sliceInt, interval );
			LoopBuilder.setImages( regionProps.get( timepoint ).timepoints, restrictedIntervalInt ).multiThreaded()
					.forEachPixel( ( source, target ) -> target.set( source ) );

			RandomAccessibleInterval< FloatType > sliceFloat = Views.hyperSlice( coordsAllTimepoints, 0, timepoint );
			RandomAccessibleInterval< FloatType > restrictedFloat = Views.interval( sliceFloat, interval3 );

			LoopBuilder.setImages( regionProps.get( timepoint ).coords, restrictedFloat )
					.forEachPixel( ( source, target ) -> target.set( source ) );

			sliceFloat = Views.hyperSlice( diametersAllTimepoints, 0, timepoint );
			restrictedFloat = Views.interval( sliceFloat, interval );

			LoopBuilder.setImages( regionProps.get( timepoint ).diameters, restrictedFloat ).multiThreaded()
					.forEachPixel( ( source, target ) -> target.set( source ) );

			sliceFloat = Views.hyperSlice( intensitiesAllTimepoints, 0, timepoint );
			restrictedFloat = Views.interval( sliceFloat, interval );
			LoopBuilder.setImages( regionProps.get( timepoint ).intensities, restrictedFloat ).multiThreaded()
					.forEachPixel( ( source, target ) -> target.set( source ) );

			sliceFloat = Views.hyperSlice( inertiaTensorsAllTimepoints, 0, timepoint );
			restrictedFloat = Views.interval( sliceFloat, interval9 );
			LoopBuilder.setImages( regionProps.get( timepoint ).inertiaTensors, restrictedFloat ).multiThreaded()
					.forEachPixel( ( source, target ) -> target.set( source ) );

			sliceFloat = Views.hyperSlice( borderDistsAllTimepoints, 0, timepoint );
			restrictedFloat = Views.interval( sliceFloat, interval );
			LoopBuilder.setImages( regionProps.get( timepoint ).borderDists, restrictedFloat ).multiThreaded()
					.forEachPixel( ( source, target ) -> target.set( source ) );
		}
		// close shared memory images of this timepoint as they are no longer needed
		regionProps.forEach( RegionProps::close );

		// Copy to ShmImgs
		this.labels = ShmImg.copyOf( labelsAllTimepoints );
		this.timepoints = ShmImg.copyOf( timepointsAllTimepoints );
		this.coords = ShmImg.copyOf( coordsAllTimepoints );
		this.diameters = ShmImg.copyOf( diametersAllTimepoints );
		this.intensities = ShmImg.copyOf( intensitiesAllTimepoints );
		this.inertiaTensors = ShmImg.copyOf( inertiaTensorsAllTimepoints );
		this.borderDists = ShmImg.copyOf( borderDistsAllTimepoints );
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
