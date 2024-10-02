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
package org.mastodon.mamut.io.exporter.labelimage;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.EllipsoidIterable;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.SpotTrackIDFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.io.exporter.labelimage.config.LabelOptions;
import org.mastodon.mamut.util.MathUtils;
import org.mastodon.spatial.SpatialIndex;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class ExportLabelImageController
{

	public static final int LABEL_ID_OFFSET = 1;

	public static final int DEFAULT_SOURCE_ID = 0;

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private final List< TimePoint > timePoints;

	private final Source< RealType< ? > > source;

	private final StatusService statusService;

	private final Context context;

	private final VoxelDimensions voxelDimensions;

	private FeatureProjection< Spot > trackIdProjection = null;

	public ExportLabelImageController( final ProjectModel projectModel, final Context context )
	{
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		this( projectModel.getModel(),
				projectModel.getSharedBdvData().getSpimData().getSequenceDescription().getTimePoints().getTimePointsOrdered(),
				Cast.unchecked( projectModel.getSharedBdvData().getSources().get( DEFAULT_SOURCE_ID ).getSpimSource() ), context,
				projectModel.getSharedBdvData().getSpimData().getSequenceDescription().getViewSetups().get( DEFAULT_SOURCE_ID )
						.getVoxelSize()
		);
	}

	protected ExportLabelImageController(
			final Model model, final List< TimePoint > timePoints, final Source< RealType< ? > > source, final Context context,
			final VoxelDimensions voxelDimensions
	)
	{
		this.model = model;
		this.timePoints = timePoints;
		this.source = source;
		this.context = context;
		this.statusService = context.service( StatusService.class );
		this.voxelDimensions = voxelDimensions;
	}

	/**
	 * Renders ellipsoids of all timepoints into an ImageJ image using the selected {@link LabelOptions} and saves it to a file.
	 * @param labelOption the {@link LabelOptions} to use
	 * @param file the file to save the image to
	 * @param showResult whether to show the result in ImageJ
	 * @param frameRateReduction only use every n-th frame for export. 1 means no reduction.
	 * @param mipMapLevel the mip map (i.e. resolution) level to use for the export. 0 means highest resolution.
	 */
	public void saveLabelImageToFile(
			final LabelOptions labelOption, final File file, boolean showResult, int frameRateReduction, int mipMapLevel )
	{
		if ( file == null )
			throw new IllegalArgumentException( "Cannot write label image to file. Given file is null." );
		if ( labelOption == null )
			throw new IllegalArgumentException( "Cannot write label image to file. Given label options are null." );

		logger.info( "Save label image to file. Label options: {}, file: {}", labelOption, file.getAbsolutePath() );
		long[] spatialDimensions = getDimensionsOfSource( mipMapLevel );
		int numTimePoints = timePoints.size();
		int frames = MathUtils.divideAndRoundUp( numTimePoints, frameRateReduction );
		logger.debug(
				"number of timepoints: {}, frame rate reduction: {}, resulting frames: {}", numTimePoints, frameRateReduction, frames );
		DiskCachedCellImg< FloatType, ? > img = createCachedImage( spatialDimensions, frames );

		ReentrantReadWriteLock.ReadLock lock = getReadLock( labelOption );
		lock.lock();
		for ( TimePoint timepoint : timePoints )
		{
			int sourceFrameId = timepoint.getId();
			if ( sourceFrameId % frameRateReduction != 0 )
				continue;
			AffineTransform3D transform = new AffineTransform3D();
			source.getSourceTransform( sourceFrameId, mipMapLevel, transform );
			int targetFrameId = sourceFrameId / frameRateReduction;
			logger.trace( "sourceFrameId: {}, targetFrameId: {}", sourceFrameId, targetFrameId );
			IntervalView< FloatType > frame = Views.hyperSlice( img, 3, targetFrameId );
			AbstractSource< FloatType > frameSource =
					new RandomAccessibleIntervalSource<>( frame, new FloatType(), transform, "Ellipsoids" );
			final EllipsoidIterable< FloatType > ellipsoidIterable = new EllipsoidIterable<>( frameSource );
			processAllSpotsOfFrame( ellipsoidIterable, labelOption, sourceFrameId, targetFrameId, frames );
		}
		lock.unlock();
		logger.debug( "Generating labels from ellipsoids finished." );

		ImgPlus< FloatType > imgplus = createImgPlus( img );
		saveImgPlus( file, imgplus );
		logger.info( "Done saving label image to file." );
		if ( showResult )
			showImgPlus( imgplus );
	}

	private ReentrantReadWriteLock.ReadLock getReadLock( LabelOptions labelOption )
	{
		switch ( labelOption )
		{
		case SPOT_ID:
		case TRACK_ID:
			return model.getGraph().getLock().readLock();
		case BRANCH_SPOT_ID:
			return model.getBranchGraph().getLock().readLock();
		default:
			throw new IllegalArgumentException( "Unknown label option: " + labelOption );
		}
	}

	private void processAllSpotsOfFrame(
			final EllipsoidIterable< ? extends RealType< ? > > iterable, final LabelOptions option, final int sourceFrameId,
			final int targetFrameId, final int frames
	)
	{
		SpatialIndex< Spot > spots = model.getSpatioTemporalIndex().getSpatialIndex( sourceFrameId );
		int oneBasedFrameId = targetFrameId + 1;
		logger.trace( "frame: {}/{}, spots: {}", oneBasedFrameId, frames, spots.size() );

		for ( Spot spot : spots )
		{
			iterable.reset( spot );
			iterable.forEach( ( Consumer< RealType< ? > > ) pixel -> pixel.setReal( getLabelId( spot, option ) ) );
		}
		statusService.showProgress( oneBasedFrameId, frames );
	}

	private long[] getDimensionsOfSource( final int mipMapLevel )
	{
		// NB: Use the dimensions of the first timepoint only without checking if they are equal in other timepoints.
		int timepoint = 0;
		long[] dimensions = this.source.getSource( timepoint, mipMapLevel ).dimensionsAsLongArray();
		logger.debug( "number of dimensions in source: {}", dimensions.length );
		Arrays.stream( dimensions ).forEach( value -> logger.debug( "dimension: {}", value ) );
		return dimensions;
	}

	private static long[] addTimeDimension( long[] dimensions, int timepoints )
	{
		long[] dimensionsWithTime = Arrays.copyOf( dimensions, dimensions.length + 1 );
		dimensionsWithTime[ dimensions.length ] = timepoints;
		return dimensionsWithTime;
	}

	private static DiskCachedCellImg< FloatType, ? > createCachedImage( long[] dimensions, int timepoints )
	{
		DiskCachedCellImgFactory< FloatType > factory = new DiskCachedCellImgFactory<>( new FloatType() );
		long[] dimensionsWithTime = addTimeDimension( dimensions, timepoints );
		int[] cellDimensions = { 50, 50, 50, 1 }; // x, y, z, t
		final DiskCachedCellImgOptions options = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions );
		return factory.create( dimensionsWithTime, options );
	}

	private int getLabelId( final Spot spot, final LabelOptions option )
	{
		switch ( option )
		{
		case SPOT_ID:
			return spot.getInternalPoolIndex() + LABEL_ID_OFFSET;
		case BRANCH_SPOT_ID:
			BranchSpot ref = model.getBranchGraph().vertexRef();
			int branchSpotId = model.getBranchGraph().getBranchVertex( spot, ref ).getInternalPoolIndex();
			model.getBranchGraph().releaseRef( ref ); // NB: optional, but increases performance, when this method is called often
			return branchSpotId + LABEL_ID_OFFSET;
		case TRACK_ID:
			if ( trackIdProjection == null )
				trackIdProjection = getTrackIDFeatureProjection( context, model );
			return ( int ) trackIdProjection.value( spot ) + LABEL_ID_OFFSET;
		default:
			throw new IllegalArgumentException( "Unknown label option: " + option );
		}
	}

	private ImgPlus< FloatType > createImgPlus( final Img< FloatType > img )
	{
		final CalibratedAxis[] axes = { new DefaultLinearAxis( Axes.X, voxelDimensions.dimension( 0 ) ),
				new DefaultLinearAxis( Axes.Y, voxelDimensions.dimension( 1 ) ),
				new DefaultLinearAxis( Axes.Z, voxelDimensions.dimension( 2 ) ), new DefaultLinearAxis( Axes.TIME ) };
		logger.debug( "voxelDimensions size: {}", voxelDimensions.numDimensions() );
		for ( int i = 0; i < voxelDimensions.numDimensions(); i++ )
			logger.debug( "voxelDimensions: {}:{}", i, voxelDimensions.dimension( i ) );
		return new ImgPlus<>( img, "Result", axes );
	}

	private static void showImgPlus( ImgPlus< FloatType > imgplus )
	{
		ImagePlus result = ImageJFunctions.wrap( imgplus, "Label image representing ellipsoids" );
		result.show();
	}

	private void saveImgPlus( File file, ImgPlus< FloatType > imgplus )
	{
		ImagePlus imagePlus = ImageJFunctions.wrap( imgplus, "Label image representing ellipsoids" );
		IJ.saveAsTiff( imagePlus, file.getAbsolutePath() );
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
