package org.mastodon.mamut.segment;

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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.mastodon.mamut.MamutAppModel;
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

public class ImportSpotFromLabelsController
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final ModelGraph modelGraph;

	private final List< TimePoint > timePoints;

	private final Source< ? extends RealType< ? > > source;

	private final StatusService statusService;

	private final VoxelDimensions voxelDimensions;
	private final double sigma;

	public ImportSpotFromLabelsController( final MamutAppModel appModel, final Context context, int labelChannelIndex, double sigma )
	{
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		this( appModel.getModel(),
				appModel.getSharedBdvData().getSpimData().getSequenceDescription().getTimePoints().getTimePointsOrdered(),
				Cast.unchecked( appModel.getSharedBdvData().getSources().get( labelChannelIndex ).getSpimSource() ), context,
				appModel.getSharedBdvData().getSpimData().getSequenceDescription().getViewSetups().get( 0 ).getVoxelSize(), sigma
		);
	}

	protected ImportSpotFromLabelsController(
			final Model model, final List< TimePoint > timePoints, final Source< ? extends RealType< ? > > source, final Context context,
			VoxelDimensions voxelDimensions, double sigma
	)
	{
		this.modelGraph = model.getGraph();
		this.timePoints = timePoints;
		this.source = source;
		this.statusService = context.getService( StatusService.class );
		this.voxelDimensions = voxelDimensions;
		this.sigma = sigma;
	}

	public void createSpotsFromLabels()
	{
		int numTimepoints = timePoints.size();

		for ( TimePoint frame : timePoints )
		{
			int frameId = frame.getId();
			long[] dimensions = source.getSource( frameId, 0 ).dimensionsAsLongArray();
			final RandomAccessibleInterval< IntegerType< ? > > img = Cast.unchecked( source.getSource( frameId, 0 ) );
			for ( int d = 0; d < dimensions.length; d++ )
				logger.debug( "Dimension {}, : {}", d, dimensions[ d ] );

			createSpotsFromLabelImage( img, frameId );
			if ( statusService != null )
			{
				statusService.showProgress( frameId + 1, numTimepoints );
			}

		}
	}

	private void createSpotsFromLabelImage( @NotNull RandomAccessibleInterval< IntegerType< ? > > img, int timepointId )
	{
		logger.debug( "Computing mean, covariance of all labels at time-point t={}", timepointId );

		// get the maximum value possible to learn how many objects need to be instantiated
		// this is fine because we expect maximum occupancy here.
		// we also subtract the background to truly get the number of elements.
		Pair< Integer, Integer > minAndMax = getPixelValueInterval( img );

		int numLabels = minAndMax.getB() - minAndMax.getA();
		int[] count = new int[ numLabels ]; // counts the number of pixels in each label, for normalization
		long[][] sum = new long[ numLabels ][ 3 ]; // sums up the positions of the label pixels, used for the 1D means
		BigInteger[][][] mixedSum =
				new BigInteger[ numLabels ][ 3 ][ 3 ]; // sums up the estimates of mixed coordinates (like xy). Used for covariances.

		readImageSumPositions( img, count, sum, mixedSum, minAndMax.getA() );

		createSpotsFromSums( timepointId, numLabels, count, sum, mixedSum );
	}

	/**
	 * Read the image and get its maximum and minimum values
	 * @param img an image to read and process
	 * @return A pair of values (min, max) that represent the minimum and maximum pixel values in the image
	 * @author Noam Dori
	 */
	@Contract("_ -> new")
	private static @NotNull Pair< Integer, Integer > getPixelValueInterval( RandomAccessibleInterval< IntegerType< ? > > img )
	{
		// read the picture to sum everything up
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		Cursor< IntegerType< ? > > cursor = Views.iterable( img ).cursor();
		while ( cursor.hasNext() )
		{
			int val = cursor.next().getInteger(); // we ignore 0 as it is BG
			if ( min > val )
			{
				min = val;
			}
			if ( max < val )
			{
				max = val;
			}
		}
		return new ValuePair<>( min, max );
	}

	/**
	 * Use the gathered information to generate all the spots for the given timepoint.
	 * @param timepointId the timepoint of the image the spots should belong to.
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
	private void createSpotsFromSums( int timepointId, int numLabels, int[] count, long[][] sum, BigInteger[][][] mixedSum )
	{
		// combine the sums into mean and covariance matrices, then add the corresponding spot
		logger.debug( "adding spots for the {} labels found", numLabels );
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
					{
						cov[ j ][ i ] = cov[ i ][ j ];
					}
				}
			}
			modelGraph.addVertex().init( timepointId, mean, cov );
		}
	}

	/**
	 * Reads the image and prepares the coordinates of all labels to obtain the 0D (count), 1D (sums), and 2D (mixed)
	 * sums to prep the ground for the mean and covariance estimates.
	 * @param img the pointer to the image to read.
	 * @param count an empty array to store the 0D sums (counts). Dimensions: [labelIdx].
	 * @param sum an empty array to store the 1D sums, i.e S[X]. Dimensions: [labelIdx, coord]
	 * @param mixedSum an empty array to store the 2D sums, i.e S[XY]. Dimensions: [labelIdx, coord, coord]
	 * @param bg the pixel value of the background. Since unsigned is annoying in Fiji, this subtracts the bg value from the label.
	 * @author Noam Dori
	 */
	private static void readImageSumPositions(
			RandomAccessibleInterval< IntegerType< ? > > img, int[] count,
			long[][] sum, BigInteger[][][] mixedSum, int bg
	)
	{
		// read the picture to sum everything up
		int[] position = new int[ 3 ];
		Cursor< IntegerType< ? > > cursor = Views.iterable( img ).cursor();
		while ( cursor.hasNext() )
		{
			int labelIdx = cursor.next().getInteger() - bg - 1; // we ignore 0 as it is BG
			if ( labelIdx < 0 )
			{
				continue;
			}
			cursor.localize( position );
			count[ labelIdx ]++;
			for ( int i = 0; i < 3; i++ )
			{
				sum[ labelIdx ][ i ] += position[ i ];
				for ( int j = i; j < 3; j++ )
				{ // the covariance matrix is symmetric!
					mixedSum[ labelIdx ][ i ][ j ] =
							mixedSum[ labelIdx ][ i ][ j ].add( valueOf( position[ i ] ).multiply( valueOf( position[ j ] ) ) );
				}
			}
		}
	}
}
