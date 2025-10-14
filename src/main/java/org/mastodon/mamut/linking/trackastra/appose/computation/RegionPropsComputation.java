package org.mastodon.mamut.linking.trackastra.appose.computation;

import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.BORDER_DIST;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.COORDS;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.DIAMETER;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.IMAGE;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.INERTIA_TENSOR;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.INTENSITY;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.LABELS;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.MASK;
import static org.mastodon.mamut.linking.trackastra.appose.constants.Contants.TIMEPOINTS;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;

import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.mastodon.mamut.io.exporter.labelimage.ExportLabelImageUtils;
import org.mastodon.mamut.linking.trackastra.TrackastraUtils;
import org.mastodon.mamut.linking.trackastra.appose.types.SingleTimepointRegionProps;
import org.mastodon.mamut.linking.trackastra.appose.exceptions.UnsupportedDataException;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.util.ApposeProcess;
import org.mastodon.mamut.util.ImgUtils;
import org.mastodon.mamut.util.ResourceUtils;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.viewer.Source;

public class RegionPropsComputation extends ApposeProcess
{
	private static final Logger slf4Logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final org.scijava.log.Logger logger;

	private final String model;

	private final int windowSize;

	public RegionPropsComputation( final org.scijava.log.Logger logger, final String model, final int windowSize ) throws IOException
	{
		super();
		this.logger = logger;
		this.model = model;
		this.windowSize = windowSize;
	}

	public List< SingleTimepointRegionProps > computeRegionPropsForSource( final Source< ? > source, final int level,
			final SpatioTemporalIndex< Spot > spatioTemporalIndex, final int minTimepoint, final int maxTimepoint )
	{
		int todo = ( maxTimepoint - minTimepoint + 1 );
		int done = 0;
		slf4Logger.info( "Computing region props for source: {}", source.getName() );
		logger.info( "Computing region props for source: " + source.getName() + "\n" );
		logger.info( "On first time use, this installs a Python new environment. This takes a while an requires internet connection.\n" );
		List< SingleTimepointRegionProps > singleTimepointRegionProps = new ArrayList<>();
		int consecutiveEmptyFrames = 0;
		for ( int timepoint = minTimepoint; timepoint <= maxTimepoint; timepoint++, done++ )
		{
			if ( spatioTemporalIndex.getSpatialIndex( timepoint ).isEmpty() )
			{
				slf4Logger.info( "No spots. Adding empty region props for timepoint: {}", timepoint );
				logger.info( "No spots. Adding empty region props for timepoint: " + timepoint + "\n" );
				singleTimepointRegionProps.add( null );
				consecutiveEmptyFrames++;
				if ( consecutiveEmptyFrames >= windowSize )
				{
					throw new UnsupportedDataException(
							"Found " + consecutiveEmptyFrames + " consecutive frames without spots before timepoint " + timepoint + ".\n"
									+ "This exceeds the configured window size of " + windowSize + " and is currently not supported.\n"
									+ "Please adjust the window size or check your data." );
				}
				continue;
			}
			consecutiveEmptyFrames = 0;
			SingleTimepointRegionProps regionProps = computeSingleTimepointProps( source, timepoint, level, spatioTemporalIndex );
			singleTimepointRegionProps.add( regionProps );
			formatProgress( maxTimepoint, done, todo, timepoint );
		}
		return singleTimepointRegionProps;
	}

	private SingleTimepointRegionProps computeSingleTimepointProps( final Source< ? > source, final int timepoint, final int level,
			final SpatioTemporalIndex< Spot > spatioTemporalIndex )
	{
		AffineTransform3D transform = new AffineTransform3D();
		source.getSourceTransform( timepoint, level, transform );
		RandomAccessibleInterval< ? > image = source.getSource( timepoint, level );
		String imageDimensions = ImgUtils.getImageDimensionsAsString( image );
		slf4Logger.info( "Processing timepoint: {}", timepoint );
		slf4Logger.info( "Getting features from image with {} dimensions: ({}) of type: {}", image.numDimensions(), imageDimensions,
				image.getType().getClass().getSimpleName() );
		RandomAccessibleInterval< IntType > masksImage = ExportLabelImageUtils.getLabelImageFromSpots( transform,
				image.dimensionsAsLongArray(), level, timepoint, spatioTemporalIndex );
		// Prepare inputs.
		try (ShmImg< ? > sharedMemoryImage = ShmImg.copyOf( Cast.unchecked( Views.dropSingletonDimensions( image ) ) );
				ShmImg< ? > sharedMemoryMasks = ShmImg.copyOf( Cast.unchecked( Views.dropSingletonDimensions( masksImage ) ) ))
		{
			stopWatch.split();
			if ( slf4Logger.isInfoEnabled() )
				slf4Logger.info( "Copied image and masks to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );
			NDArray imageNDArray = NDArrays.asNDArray( sharedMemoryImage );
			NDArray masksNDArray = NDArrays.asNDArray( sharedMemoryMasks );
			stopWatch.split();
			if ( slf4Logger.isInfoEnabled() )
				slf4Logger.info( "Converted image and masks to nd arrays: {} and {}. Time elapsed: {}", imageNDArray, masksNDArray,
						stopWatch.formatSplitTime() );

			inputs.put( IMAGE, imageNDArray );
			inputs.put( MASK, masksNDArray );

			Service.Task result = runScript();
			ShmImg< IntType > labels = new ShmImg<>( ( NDArray ) result.outputs.get( LABELS ) );
			LoopBuilder.setImages( labels ).multiThreaded().forEachPixel( p -> p.set( p.get() - 1 ) ); // make labels zero-based again
			ShmImg< IntType > timepoints = new ShmImg<>( ( NDArray ) result.outputs.get( TIMEPOINTS ) );
			LoopBuilder.setImages( timepoints ).multiThreaded().forEachPixel( p -> p.set( timepoint ) ); // all timepoints are the same
			ShmImg< FloatType > coords = new ShmImg<>( ( NDArray ) result.outputs.get( COORDS ) );
			ShmImg< FloatType > diameter = new ShmImg<>( ( NDArray ) result.outputs.get( DIAMETER ) );
			ShmImg< FloatType > intensity = new ShmImg<>( ( NDArray ) result.outputs.get( INTENSITY ) );
			ShmImg< FloatType > inertiaTensor = new ShmImg<>( ( NDArray ) result.outputs.get( INERTIA_TENSOR ) );
			ShmImg< FloatType > borderDist = new ShmImg<>( ( NDArray ) result.outputs.get( BORDER_DIST ) );
			return new SingleTimepointRegionProps( labels, timepoints, coords, diameter, intensity, inertiaTensor, borderDist );
		}
		catch ( IOException e )
		{
			throw new UncheckedIOException( e );
		}
	}

	private void formatProgress( final int maxTimepoint, final double done, final int todo, final int timepoint )
	{
		double progress = done / todo;
		NumberFormat percentFormatter = NumberFormat.getPercentInstance();
		percentFormatter.setMinimumFractionDigits( 0 );
		percentFormatter.setMaximumFractionDigits( 0 );
		String message = String.format( "Computed region props for timepoint %d/%d. Progress: %s", timepoint, maxTimepoint,
				percentFormatter.format( progress ) );
		slf4Logger.info( message );
		logger.info( message + "\n" );
	}

	@Override
	protected String generateEnvFileContent()
	{
		return TrackastraUtils.getEnv( getApposeVersion() );
	}

	@Override
	protected String generateScript()
	{
		final String template =
				ResourceUtils.readResourceAsString( "org/mastodon/mamut/linking/trackastra/appose/region_props.py", getClass() );
		return template
				.replace( "{IMAGE}", IMAGE )
				.replace( "{MASK}", MASK )
				.replace( "{MODEL}", model )
				.replace( "{LABELS}", LABELS )
				.replace( "{TIMEPOINTS}", TIMEPOINTS )
				.replace( "{COORDS}", COORDS )
				.replace( "{DIAMETER}", DIAMETER )
				.replace( "{INTENSITY}", INTENSITY )
				.replace( "{INERTIA_TENSOR}", INERTIA_TENSOR )
				.replace( "{BORDER_DIST}", BORDER_DIST );
	}

	@Override
	protected String generateImportStatements()
	{
		return ResourceUtils.readResourceAsString( "org/mastodon/mamut/linking/trackastra/appose/region_props_imports.py", getClass() );
	}
}
