package org.mastodon.mamut.io.importer.labelimage;

import bdv.viewer.Source;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.util.List;

import static java.math.BigInteger.valueOf;

public class ImportSpotsFromLabelImageController
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final ModelGraph modelGraph;

	private final List< TimePoint > frames;

	private final Source< ? extends RealType< ? > > source;

	private final StatusService statusService;

	private final VoxelDimensions voxelDimensions;

	private final double sigma;

	public ImportSpotsFromLabelImageController(
			final ProjectModel projectModel, final Context context, int labelChannelIndex, double sigma
	)
	{
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		this( projectModel.getModel(),
				projectModel.getSharedBdvData().getSpimData().getSequenceDescription().getTimePoints().getTimePointsOrdered(),
				Cast.unchecked( projectModel.getSharedBdvData().getSources().get( labelChannelIndex ).getSpimSource() ), context,
				projectModel.getSharedBdvData().getSpimData().getSequenceDescription().getViewSetups().get( 0 ).getVoxelSize(), sigma
		);
	}

	protected ImportSpotsFromLabelImageController(
			final Model model, final List< TimePoint > frames, final Source< ? extends RealType< ? > > source, final Context context,
			final VoxelDimensions voxelDimensions, double sigma
	)
	{
		this.modelGraph = model.getGraph();
		this.frames = frames;
		this.source = source;
		this.statusService = context.getService( StatusService.class );
		this.voxelDimensions = voxelDimensions;
		this.sigma = sigma;
	}

	/**
	 * Converts label images to (spot) ellipsoids.<br>
	 * The method runs twice through each image (i.e. each frame) read. Once to determine maximum/minimum values for array initialization, and once to do summation for covariance and mean.
	 */
	public void createSpotsFromLabelImage()
	{
		int numTimepoints = frames.size();

		for ( TimePoint frame : frames )
		{
			int frameId = frame.getId();
			long[] dimensions = source.getSource( frameId, 0 ).dimensionsAsLongArray();
			final RandomAccessibleInterval< IntegerType< ? > > img = Cast.unchecked( source.getSource( frameId, 0 ) );
			for ( int d = 0; d < dimensions.length; d++ )
				logger.debug( "Dimension {}, : {}", d, dimensions[ d ] );

			createSpotsFromLabelImage( img, frameId );
			if ( statusService != null )
				statusService.showProgress( frameId + 1, numTimepoints );
		}
	}

	private void createSpotsFromLabelImage( RandomAccessibleInterval< IntegerType< ? > > img, int frameId )
	{
		logger.debug( "Computing mean, covariance of all labels at frame {}", frameId );

		// get the maximum value possible to learn how many objects need to be instantiated
		// this is fine because we expect maximum occupancy here.
		// we also subtract the background to truly get the number of elements.
		Pair< Integer, Integer > minAndMax = getPixelValueInterval( img );

		int numLabels = minAndMax.getB() - minAndMax.getA();
		if ( numLabels > 10_000 ) // 10_000 is arbitrary, but we shouldn't expect this many labels from one image
			logger.warn( "found {} labels, are you sure you used the correct channel?", numLabels );
		int[] count = new int[ numLabels ]; // counts the number of pixels in each label, for normalization
		long[][] sum = new long[ numLabels ][ 3 ]; // sums up the positions of the label pixels, used for the 1D means
		BigInteger[][][] mixedSum =
				new BigInteger[ numLabels ][ 3 ][ 3 ]; // sums up the estimates of mixed coordinates (like xy). Used for covariances.

		readImageSumPositions( img, count, sum, mixedSum, minAndMax.getA() );
		createSpotsFromSums( frameId, numLabels, count, sum, mixedSum );
	}

	/**
	 * Read the image and get its maximum and minimum values
	 * @param img an image to read and process
	 * @return A pair of values (min, max) that represent the minimum and maximum pixel values in the image
	 * @author Noam Dori
	 */
	private static Pair< Integer, Integer > getPixelValueInterval( RandomAccessibleInterval< IntegerType< ? > > img )
	{
		// read the picture to sum everything up
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Cursor< IntegerType< ? > > cursor = Views.iterable( img ).cursor();
		while ( cursor.hasNext() )
		{
			int val = cursor.next().getInteger(); // we ignore 0 as it is background
			if ( min > val )
				min = val;
			if ( max < val )
				max = val;
		}
		return new ValuePair<>( min, max );
	}

	/**
	 * Use the gathered information to generate all the spots for the given timepoint.
	 * @param frame the frame of the image the spots should belong to.
	 * @param numLabels the maximum value encountered in the image. Also equal to the number of labels.
	 * @param count the 0D sums (counts). Dimensions: [labelIdx].
	 * @param sum the 1D sums, i.e S[X]. Dimensions: [labelIdx, coord]
	 * @param mixedSum the 2D sums, i.e S[XY]. Dimensions: [labelIdx, coord, coord]
	 * @implNote The covariance formula used here is not the definition COV(X,Y) = E[(X - E[X])(Y - E[Y])]
	 * 			 but instead its simplification COV(X, Y) = E[XY] - E[X]E[Y].
	 * 			 Read more <a href=https://en.wikipedia.org/wiki/Covariance#Definition>here</a>.
	 *           Previously there was a factor of 5 placed on the covariance.
	 *           I removed it, but it might be neccesary for some reason.
	 * @author Noam Dori
	 */
	private void createSpotsFromSums( int frame, int numLabels, int[] count, long[][] sum, BigInteger[][][] mixedSum )
	{
		// combine the sums into mean and covariance matrices, then add the corresponding spot
		logger.debug( "Found {} labels. Adding a spot for each label.", numLabels );
		double[] mean = new double[ 3 ];
		double[][] cov = new double[ 3 ][ 3 ];
		for ( int labelIdx = 0; labelIdx < numLabels; labelIdx++ )
		{
			for ( int i = 0; i < 3; i++ )
			{
				mean[ i ] = sum[ labelIdx ][ i ] / ( double ) count[ labelIdx ] * voxelDimensions.dimension( i );
				for ( int j = i; j < 3; j++ )
				{ // the covariance matrix is symmetric!
					cov[ i ][ j ] = mixedSum[ labelIdx ][ i ][ j ].multiply( valueOf( count[ labelIdx ] ) )
							.subtract( valueOf( sum[ labelIdx ][ i ] ).multiply( valueOf( sum[ labelIdx ][ j ] ) ) )
							.doubleValue() / Math.pow( count[ labelIdx ], 2 );
					cov[ i ][ j ] *= Math.pow( sigma, 2 ) * voxelDimensions.dimension( i ) * voxelDimensions.dimension( j );
					if ( i != j )
						cov[ j ][ i ] = cov[ i ][ j ];
				}
			}
			modelGraph.addVertex().init( frame, mean, cov );
		}
	}

	/**
	 * Reads the image and prepares the coordinates of all labels to obtain the 0D (count), 1D (sums), and 2D (mixed)
	 * sums to prep the ground for the mean and covariance estimates.
	 * @param img the pointer to the image to read.
	 * @param count an empty array to store the 0D sums (counts). Dimensions: [labelIdx].
	 * @param sum an empty array to store the 1D sums, i.e S[X]. Dimensions: [labelIdx, coord]
	 * @param mixedSum an empty array to store the 2D sums, i.e S[XY]. Dimensions: [labelIdx, coord, coord]
	 * @param background the pixel value of the background. Since unsigned is annoying in Fiji, this subtracts the bg value from the label.
	 * @author Noam Dori
	 */
	private static void readImageSumPositions(
			RandomAccessibleInterval< IntegerType< ? > > img, int[] count, long[][] sum, BigInteger[][][] mixedSum, int background
	)
	{
		// read all pixels of the picture to sum everything up
		int[] pixel = new int[ 3 ];
		Cursor< IntegerType< ? > > cursor = Views.iterable( img ).cursor();
		while ( cursor.hasNext() )
		{
			int labelIdx = cursor.next().getInteger() - background - 1; // we ignore 0 as it is BG
			if ( labelIdx < 0 )
				continue;
			cursor.localize( pixel );
			count[ labelIdx ]++;
			for ( int i = 0; i < 3; i++ )
			{
				sum[ labelIdx ][ i ] += pixel[ i ];
				for ( int j = i; j < 3; j++ )
				{ // the covariance matrix is symmetric!
					if ( mixedSum[ labelIdx ][ i ][ j ] == null )
						mixedSum[ labelIdx ][ i ][ j ] = BigInteger.ZERO;
					mixedSum[ labelIdx ][ i ][ j ] =
							mixedSum[ labelIdx ][ i ][ j ].add( valueOf( pixel[ i ] ).multiply( valueOf( pixel[ j ] ) ) );
				}
			}
		}
	}
}
