package org.mastodon.mamut.segment;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import ij.ImagePlus;
import io.scif.codec.CompressionType;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgSaver;
import mpicbg.spim.data.sequence.TimePoint;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Cast;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.EllipsoidIterable;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.SpotTrackIDFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.segment.config.LabelOptions;
import org.mastodon.spatial.SpatialIndex;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SegmentUsingEllipsoidsController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private final List< TimePoint > timePoints;

	private final Source< RealType< ? > > source;

	private final StatusService statusService;

	private final ImgSaver saver;

	public SegmentUsingEllipsoidsController(
			final Model model, final List< TimePoint > timePoints, final Source< RealType< ? > > source, final Context context
	)
	{
		this.model = model;
		this.timePoints = timePoints;
		this.source = source;
		this.statusService = context.service( StatusService.class );
		this.saver = new ImgSaver( context );
	}

	/**
	 * Renders ellipsoids of all timepoints into an ImageJ image using the selected {@link LabelOptions} and saves it to a file.
	 * @param labelOption the {@link LabelOptions} to use
	 * @param file the file to save the image to
	 * @param withBackground whether to keep the background of the image
	 * @param showResult whether to show the result in ImageJ
	 */
	public void saveEllipsoidSegmentationToFile(
			final LabelOptions labelOption, final File file, boolean withBackground, boolean showResult
	)
	{
		if ( file == null )
			throw new IllegalArgumentException( "Cannot write ellipsoid segmentation to file. Given file is null." );
		if ( labelOption == null )
			throw new IllegalArgumentException( "Cannot write ellipsoid segmentation to file. Given label options are null." );

		logger.info( "Save ellipsoid segmentation to file. Label options: {}, file: {}", labelOption, file.getAbsolutePath() );
		long[] spatialDimensions = getDimensionsSpimSource();
		int frames = timePoints.size();
		logger.debug( "number of frames: {}", frames );
		DiskCachedCellImg< IntType, ? > img = createCachedImage( spatialDimensions, frames );

		for ( TimePoint timepoint : timePoints )
		{
			int timepointId = timepoint.getId();
			AffineTransform3D transform = new AffineTransform3D();
			source.getSourceTransform( timepointId, 0, transform );
			IntervalView< IntType > slice = Views.hyperSlice( img, 3, timepointId );
			AbstractSource< IntType > sliceSource = new RandomAccessibleIntervalSource<>( slice, new IntType(), transform, "Segmentation" );
			if ( withBackground )
			{
				RandomAccessible< RealType< ? > > bdvRandomAccessible = Cast.unchecked( source.getSource( timepointId, 0 ) );
				RandomAccessibleInterval< IntType > sliceRai = sliceSource.getSource( 0, 0 );
				RealTypeConverters.copyFromTo( bdvRandomAccessible, sliceRai );
			}
			final EllipsoidIterable< IntType > ellipsoidIterable = new EllipsoidIterable<>( sliceSource );
			segmentAllSpotsOfTimepoint( ellipsoidIterable, labelOption, timepointId, frames );
		}

		ImgPlus< IntType > imgplus = createImgPlus( img );
		if ( showResult )
			showImgPlus( imgplus );
		saveImgPlus( file, imgplus );
		logger.info( "Done saving ellipsoid segmentation to file." );
	}

	/**
	 * Renders ellipsoids of all timepoints into the BigDataViewer using the selected {@link LabelOptions}.
	 * @param labelOptions the {@link LabelOptions} to use
	 */
	public void showEllipsoidSegmentationInBDV( final LabelOptions labelOptions )
	{
		if ( labelOptions == null )
			throw new IllegalArgumentException( "Cannot show ellipsoid segmentation in BDV. Given label options are null." );
		logger.info( "Show ellipsoid segmentation in BDV. Use label option: {}", labelOptions );

		final EllipsoidIterable< RealType< ? > > ellipsoidIterable = new EllipsoidIterable<>( source );

		int frames = timePoints.size();
		timePoints.forEach( timepoint -> segmentAllSpotsOfTimepoint( ellipsoidIterable, labelOptions, timepoint.getId(), frames ) );
		logger.info( "Done labelling ellipsoids BDV." );
	}

	private void segmentAllSpotsOfTimepoint(
			final EllipsoidIterable< ? extends RealType< ? > > iterable, final LabelOptions option, final int timepointId, final int frames
	)
	{
		SpatialIndex< Spot > spots = model.getSpatioTemporalIndex().getSpatialIndex( timepointId );
		int oneBasedTimepointId = timepointId + 1;
		logger.info( "timepoint: {}/{}, spots: {}", oneBasedTimepointId, frames, spots.size() );

		for ( Spot spot : spots )
		{
			iterable.reset( spot );
			iterable.forEach( ( Consumer< RealType< ? > > ) pixel -> pixel.setReal( getId( spot, option ) ) );
		}
		statusService.showProgress( oneBasedTimepointId, frames );
	}

	private long[] getDimensionsSpimSource()
	{
		// NB: Use the dimensions of the first timepoint only without checking if they are equal in other timepoints.
		int timepoint = 0;
		// NB: the midmaplevel 0 is supposed to be the highest resolution
		int midMipmapLevel = 0;
		long[] dimensions = this.source.getSource( timepoint, midMipmapLevel ).dimensionsAsLongArray();
		logger.debug( "spim source, number of dimensions: {}", dimensions.length );
		Arrays.stream( dimensions ).forEach( value -> logger.debug( "dimension: {}", value ) );
		return dimensions;
	}

	private static long[] addTimeDimension( long[] dimensions, int timepoints )
	{
		long[] dimensionsWithTime = Arrays.copyOf( dimensions, dimensions.length + 1 );
		dimensionsWithTime[ dimensions.length ] = timepoints;
		return dimensionsWithTime;
	}

	private static DiskCachedCellImg< IntType, ? > createCachedImage( long[] dimensions, int timepoints )
	{
		DiskCachedCellImgFactory< IntType > factory = new DiskCachedCellImgFactory<>( new IntType() );
		long[] dimensionsWithTime = addTimeDimension( dimensions, timepoints );
		int[] cellDimensions = { 50, 50, 50, 1 }; // x, y, z, t
		final DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions );
		return factory.create( dimensionsWithTime, options );
	}

	private int getId( final Spot spot, final LabelOptions labelOptions )
	{
		if ( labelOptions.equals( LabelOptions.SPOT_ID ) )
			return spot.getInternalPoolIndex();
		BranchSpot ref = model.getBranchGraph().vertexRef();
		int branchSpotId = model.getBranchGraph().getBranchVertex( spot, ref ).getInternalPoolIndex();
		model.getBranchGraph().releaseRef( ref ); // NB: optional, but increases performance, when this method is called often
		return branchSpotId;
	}

	private static ImgPlus< IntType > createImgPlus( final Img< IntType > img )
	{
		final AxisType[] axesType = new AxisType[ 4 ];
		axesType[ 0 ] = Axes.X;
		axesType[ 1 ] = Axes.Y;
		axesType[ 2 ] = Axes.Z;
		axesType[ 3 ] = Axes.TIME;

		return new ImgPlus<>( img, "Result", axesType );
	}

	private static void showImgPlus( ImgPlus< IntType > imgplus )
	{
		ImagePlus result = ImageJFunctions.wrap( imgplus, "Segmentation using ellipsoids" );
		result.show();
	}

	private void saveImgPlus( File file, ImgPlus< IntType > imgplus )
	{
		SCIFIOConfig config = new SCIFIOConfig();
		config.writerSetCompression( CompressionType.LZW );
		saver.saveImg( file.getAbsolutePath(), imgplus, config );
	}

	private static FeatureProjection< Spot > getTrackIDFeatureProjection( final Context context, final Model model )
	{
		final MamutFeatureComputerService featureComputerService = getMamutFeatureComputerService( context, model );
		SpotTrackIDFeature spotTrackIDFeature =
				Cast.unchecked( featureComputerService.compute( true, SpotTrackIDFeature.SPEC ).get( SpotTrackIDFeature.SPEC ) );
		FeatureProjectionKey key = FeatureProjectionKey.key( new FeatureProjectionSpec( SpotTrackIDFeature.KEY ) );
		return spotTrackIDFeature.project( key );
	}

	private static MamutFeatureComputerService getMamutFeatureComputerService( final Context context, final Model model )
	{
		final MamutFeatureComputerService featureComputerService = MamutFeatureComputerService.newInstance( context );
		featureComputerService.setModel( model );
		return featureComputerService;
	}
}
