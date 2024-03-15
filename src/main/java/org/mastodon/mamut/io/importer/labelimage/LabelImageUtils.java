package org.mastodon.mamut.io.importer.labelimage;

import bdv.viewer.Source;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.math.CovarianceMatrix;
import org.mastodon.mamut.io.importer.labelimage.math.MeansVector;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntFunction;

/**
 * Utility class to create spots from label images.
 */
public class LabelImageUtils
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private LabelImageUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Creates spots from the given label image.<br>
	 * The method runs twice through each image (i.e. each frame). Once to determine maximum/minimum values for array initialization, and once to do summation for covariance and mean.
	 * @param frameProvider a function that provides the image data for each frame.
	 * @param frames a list of frames to process.
	 * @param model the model to add the spots to.
	 * @param voxelDimensions the dimensions of the voxels in the image.
	 * @param sigma the standard deviation of the Gaussian distribution to use for the covariance matrix.
	 * @param statusService the status service to report progress to.
	 */
	static void createSpotsFromLabelImage( final IntFunction< RandomAccessibleInterval< RealType< ? > > > frameProvider,
			final List< TimePoint > frames, final Model model, final VoxelDimensions voxelDimensions, final double sigma,
			final StatusService statusService )
	{
		final ModelGraph graph = model.getGraph();
		ReentrantReadWriteLock lock = graph.getLock();
		lock.writeLock().lock();
		int count = 0;
		try
		{
			int numTimepoints = frames.size();
			for ( TimePoint frame : frames )
			{
				int frameId = frame.getId();
				final RandomAccessibleInterval< RealType< ? > > rai = frameProvider.apply( frameId );
				count += createSpotsForFrame( graph, rai, frameId, voxelDimensions, sigma );
				if ( statusService != null )
					statusService.showProgress( frameId + 1, numTimepoints );
			}
			model.setUndoPoint();
		}
		finally
		{
			lock.writeLock().unlock();
		}
		graph.notifyGraphChanged();
		logger.info( "Created {} new spots in {} frames.", count, frames.size() );
	}

	/**
	 * Create spots for the given frame.
	 * @param graph the graph to add the spots to.
	 * @param frame the image data to read and process.
	 * @param frameId the frame id the spots should belong to.
	 * @param voxelDimensions the dimensions of the voxels in the image.
	 * @param sigma the standard deviation of the Gaussian distribution to use for the covariance matrix.
	 * @return the number of spots created.
	 */
	private static int createSpotsForFrame( final ModelGraph graph, final RandomAccessibleInterval< RealType< ? > > frame,
			final int frameId, final VoxelDimensions voxelDimensions, final double sigma )
	{
		logger.debug( "Computing mean, covariance of all labels at frame {}", frameId );
		logger.debug( "Dimensions of frame: {}, {}, {}", frame.dimension( 0 ), frame.dimension( 1 ), frame.dimension( 2 ) );

		// get the maximum value possible to learn how many objects need to be instantiated
		Pair< Integer, Integer > minAndMax = getPixelValueInterval( frame );

		int minimumLabel = minAndMax.getA();
		int maximumLabel = minAndMax.getB();
		if ( minimumLabel == Integer.MAX_VALUE || maximumLabel == Integer.MIN_VALUE )
		{
			logger.debug( "No labels found in frame {}", frameId );
			return 0;
		}
		int numLabels = maximumLabel - minimumLabel + 1;
		if ( numLabels > 10_000 ) // NB: 10_000 is arbitrary, but such a high number of labels is suspicious, thus we log a warning here
			logger.warn( "found {} labels, are you sure you used the correct image source?", numLabels );

		logger.debug( "Found {} label(s) in frame {}. Range: [{}, {}]", numLabels, frameId, minimumLabel, maximumLabel );
		Label[] labels = extractLabelsFromFrame( frame, minimumLabel, numLabels );
		return createSpotsFromFrameLabels( graph, frameId, labels, voxelDimensions, sigma );
	}

	/**
	 * Read the frame and determine the minimum and maximum pixel values in the frame.
	 * @param frame an image to read and process
	 * @return A pair of values (min, max) that represent the minimum and maximum pixel values in the image
	 * @author Noam Dori
	 */
	private static Pair< Integer, Integer > getPixelValueInterval( final RandomAccessibleInterval< RealType< ? > > frame )
	{
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Cursor< RealType< ? > > cursor = Views.iterable( frame ).cursor();
		while ( cursor.hasNext() )
		{
			int val = ( int ) cursor.next().getRealDouble();
			// we ignore 0 as it is background
			if ( val == 0 )
				continue;
			if ( min > val )
				min = val;
			if ( max < val )
				max = val;
		}
		return new ValuePair<>( min, max );
	}

	/**
	 * Create spots from the labels in the given frame.
	 * @param graph the graph to add the spots to.
	 * @param frameId the frame id the spots should belong to.
	 * @param labels the labels in the frame.
	 * @param voxelDimensions the dimensions of the voxels in the image.
	 * @param sigma the standard deviation of the Gaussian distribution to use for the covariance matrix.
	 * @return the number of spots created.
	 */
	private static int createSpotsFromFrameLabels( final ModelGraph graph, final int frameId, final Label[] labels,
			final VoxelDimensions voxelDimensions, final double sigma )
	{
		int count = 0;
		// combine the sums into mean and covariance matrices, then add the corresponding spot
		for ( final Label label : labels )
		{
			// skip labels that are not present in the image or have only one pixel
			if ( label == null || label.numPixels < 2 )
				continue;
			double[] mean = label.means.get();
			double[][] cov = label.covariances.get();
			scale( mean, voxelDimensions );
			scale( cov, sigma, voxelDimensions );
			try
			{
				Spot spot = graph.addVertex().init( frameId, mean, cov );
				spot.setLabel( String.valueOf( label.value ) );
				count++;
			}
			catch ( Exception e )
			{
				logger.trace( "Could not add vertex to graph. Mean: {}, Covariance: {}", Arrays.toString( mean ),
						Arrays.deepToString( cov ) );
			}
		}
		logger.debug( "Added {} spots to frame {}", count, frameId );
		return count;
	}

	/**
	 * Extracts the labels from the given frame by iterating once over all pixels of that frame and computing the means and covariances for all labels.
	 * @param frame the pointer to the image to read.
	 * @param minimumLabelValue the minimum value of the pixels in the image.
	 * @param numLabels the number of labels in the frame.
	 */
	private static Label[] extractLabelsFromFrame( final RandomAccessibleInterval< RealType< ? > > frame, int minimumLabelValue,
			int numLabels )
	{
		Label[] labels = new Label[ numLabels ];
		// read all pixels of the picture to sum everything up
		Cursor< RealType< ? > > cursor = Views.iterable( frame ).cursor();
		int[] pixel = new int[ cursor.numDimensions() ];
		while ( cursor.hasNext() )
		{
			int pixelValue = ( int ) cursor.next().getRealDouble();
			int labelIndex = pixelValue - minimumLabelValue;
			if ( pixelValue <= 0 )
				continue;
			if ( labels[ labelIndex ] == null )
				labels[ labelIndex ] = new Label( pixelValue, cursor.numDimensions() );
			Label label = labels[ labelIndex ];
			cursor.localize( pixel );
			label.addPixel( pixel );
		}
		return labels;
	}

	/**
	 * Scales the mean vector by the voxel dimensions.
	 * @param mean the mean vector to scale.
	 * @param voxelDimensions the dimensions of the voxels in the image.
	 */
	private static void scale( final double[] mean, final VoxelDimensions voxelDimensions )
	{
		if ( mean.length != voxelDimensions.numDimensions() )
			throw new IllegalArgumentException( "Mean vector has wrong dimension." );
		for ( int i = 0; i < mean.length; i++ )
			mean[ i ] = mean[ i ] * voxelDimensions.dimension( i );
	}

	/**
	 * Returns the dimensions of the given image as an array in the order x, y, z, t.
	 * @param imgPlus the image to get the dimensions from.
	 * @return the dimensions of the image.
	 */
	public static long[] getImgPlusDimensions( final ImgPlus< ? > imgPlus )
	{
		long[] imgPlusDimensions = imgPlus.getImg().dimensionsAsLongArray();
		int numDimensions = imgPlusDimensions.length;
		long imgPlusX = imgPlusDimensions[ 0 ];
		long imgPlusY = imgPlusDimensions[ 1 ];
		long imgPlusZ = numDimensions > 2 ? imgPlusDimensions[ 2 ] : 0;
		long imgPlusT = numDimensions > 3 ? imgPlusDimensions[ 3 ] : 0;
		return new long[] { imgPlusX, imgPlusY, imgPlusZ, imgPlusT };
	}

	/**
	 * Returns the dimensions of the given big data viewer image as an array in the order x, y, z, t.
	 * @param sharedBdvData the big data viewer data to get the dimensions from.
	 * @return the dimensions of the big data viewer image.
	 */
	public static long[] getBdvDimensions( final SharedBigDataViewerData sharedBdvData )
	{
		AbstractSequenceDescription< ?, ?, ? > sequenceDescription = sharedBdvData.getSpimData().getSequenceDescription();
		int bdvTimepoints = sequenceDescription.getTimePoints().size();
		long[] bdvSpatialDimensions = sequenceDescription.getViewSetups().get( 0 ).getSize().dimensionsAsLongArray();
		return new long[] { bdvSpatialDimensions[ 0 ], bdvSpatialDimensions[ 1 ], bdvSpatialDimensions[ 2 ], bdvTimepoints };
	}

	/**
	 * Checks if the dimensions of the given big data viewer image match the dimensions of the given image.
	 * @param sharedBigDataViewerData the big data viewer data to check the dimensions against.
	 * @param imgPlus the image to check the dimensions against.
	 * @return true if the dimensions match, false otherwise.
	 */
	public static boolean dimensionsMatch( final SharedBigDataViewerData sharedBigDataViewerData, final ImgPlus< ? > imgPlus )
	{
		long[] bdvDimensions = getBdvDimensions( sharedBigDataViewerData );
		long[] imgPlusDimensions = getImgPlusDimensions( imgPlus );
		return Arrays.equals( bdvDimensions, imgPlusDimensions );
	}

	/**
	 * Imports spots from the given ImageJ image into the given project model.
	 * @param imgPlus the image to import the spots from.
	 * @param sigma the standard deviation of the Gaussian distribution to use for the covariance matrix.
	 * @param projectModel the project model to add the spots to.
	 * @throws IllegalArgumentException if the dimensions of the given image do not match the dimensions of the big data viewer image contained in the project model.
	 */
	public static void importSpotsFromImgPlus( final ImgPlus< ? > imgPlus, final double sigma, final ProjectModel projectModel )
	{
		logger.debug( "ImageJ image: {}", imgPlus.getName() );
		final SharedBigDataViewerData sharedBdvData = projectModel.getSharedBdvData();
		if ( !dimensionsMatch( sharedBdvData, imgPlus ) )
			throw new IllegalArgumentException( "The dimensions of the ImageJ image " + imgPlus.getName()
					+ " do not match the dimensions of the big data viewer image." );
		final Context context = projectModel.getContext();
		final AbstractSequenceDescription< ?, ?, ? > sequenceDescription = sharedBdvData.getSpimData().getSequenceDescription();
		final List< TimePoint > frames = sequenceDescription.getTimePoints().getTimePointsOrdered();
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		final VoxelDimensions voxelDimensions = sequenceDescription.getViewSetups().get( 0 ).getVoxelSize();
		IntFunction< RandomAccessibleInterval< RealType< ? > > > frameProvider =
				frameId -> Cast.unchecked( Views.hyperSlice( imgPlus.getImg(), 3, frameId ) );
		LabelImageUtils.createSpotsFromLabelImage( frameProvider, frames, projectModel.getModel(), voxelDimensions, sigma,
				context.getService( StatusService.class ) );
	}

	/**
	 * Imports spots from the given big data viewer channel into the given project model.
	 * @param projectModel the project model to add the spots to.
	 * @param source the source to import the spots from.
	 * @param sigma the standard deviation of the Gaussian distribution to use for the covariance matrix.
	 * @throws IllegalArgumentException if the label channel index is out of bounds, i.e. if it is greater than the number of channels in the big data viewer source contained in the project model.
	 */
	public static void importSpotsFromBdvChannel( final ProjectModel projectModel, final Source< ? > source, final double sigma )
	{
		final SharedBigDataViewerData sharedBigDataViewerData = projectModel.getSharedBdvData();
		final AbstractSequenceDescription< ?, ?, ? > sequenceDescription = sharedBigDataViewerData.getSpimData().getSequenceDescription();
		final List< TimePoint > frames = sequenceDescription.getTimePoints().getTimePointsOrdered();
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		final VoxelDimensions voxelDimensions = sequenceDescription.getViewSetups().get( 0 ).getVoxelSize();
		IntFunction< RandomAccessibleInterval< RealType< ? > > > imgProvider =
				frameId -> Cast.unchecked( source.getSource( frameId, 0 ) );
		LabelImageUtils.createSpotsFromLabelImage( imgProvider, frames, projectModel.getModel(), voxelDimensions, sigma,
				projectModel.getContext().getService( StatusService.class ) );
	}

	/**
	 * Scales the covariance matrix the given sigma and the voxel dimensions.
	 * @param covariance the covariance matrix to scale.
	 * @param sigma the standard deviation of the Gaussian distribution to use for the covariance matrix.
	 * @param voxelDimensions the dimensions of the voxels in the image.
	 * @throws IllegalArgumentException if the covariance matrix has not the same dimensions as the given voxelDimensions.
	 */
	public static void scale( final double[][] covariance, final double sigma, final VoxelDimensions voxelDimensions )
	{
		if ( covariance.length != voxelDimensions.numDimensions() )
			throw new IllegalArgumentException( "Covariance matrix has wrong dimension." );
		for ( int i = 0; i < covariance.length; i++ )
		{
			if ( covariance[ i ].length != voxelDimensions.numDimensions() )
				throw new IllegalArgumentException( "Covariance matrix has wrong dimension." );
			for ( int j = i; j < covariance.length; j++ )
			{
				covariance[ i ][ j ] *= Math.pow( sigma, 2 ) * voxelDimensions.dimension( i ) * voxelDimensions.dimension( j );
				// the covariance matrix is symmetric!
				if ( i != j )
					covariance[ j ][ i ] = covariance[ i ][ j ];
			}
		}
	}

	/**
	 * A class to hold the pixel count, mean and covariances of a label in the image.
	 */
	private static class Label
	{
		private final int value;

		private int numPixels;

		private final MeansVector means;

		private final CovarianceMatrix covariances;

		private Label( final int value, final int numDimensions )
		{
			this.value = value;
			numPixels = 0;
			means = new MeansVector( numDimensions );
			covariances = new CovarianceMatrix( numDimensions );
		}

		private void addPixel( int[] pixel )
		{
			numPixels++;
			means.addValues( pixel );
			covariances.addValues( pixel );
		}
	}
}
