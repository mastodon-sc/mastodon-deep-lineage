package org.mastodon.mamut.segment;

import bdv.viewer.Source;
import mpicbg.spim.data.sequence.TimePoint;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import org.jetbrains.annotations.NotNull;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class ImportSpotFromLabelsController
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final ModelGraph modelGraph;

	private final List< TimePoint > timePoints;

	private final Source< RealType< ? > > source;

	private final StatusService statusService;

	public ImportSpotFromLabelsController( final MamutAppModel appModel, final Context context, int labelChannelIndex )
	{
		// NB: Use the dimensions of the first source and the first time point only without checking if they are equal in other sources and time points.
		this( appModel.getModel(),
				appModel.getSharedBdvData().getSpimData().getSequenceDescription().getTimePoints().getTimePointsOrdered(),
				Cast.unchecked( appModel.getSharedBdvData().getSources().get( labelChannelIndex ).getSpimSource() ), context
		);
	}

	protected ImportSpotFromLabelsController(
			final Model model, final List< TimePoint > timePoints, final Source< RealType< ? > > source, final Context context
	)
	{
		this.modelGraph = model.getGraph();
		this.timePoints = timePoints;
		this.source = source;
		this.statusService = context.service( StatusService.class );
	}

	public void createSpotsFromLabels()
	{
		int numTimepoints = timePoints.size();

		for ( TimePoint frame : timePoints )
		{
			int frameId = frame.getId();
			long[] dimensions = source.getSource( frameId, 0 ).dimensionsAsLongArray();
			final RandomAccessibleInterval< RealType< ? > > img = source.getSource( frameId, 0 );
			for ( int d = 0; d < dimensions.length; d++ )
				logger.debug( "Dimension {}, : {}", d, dimensions[ d ] );

			createSpotsFromLabelImage(img, frameId);
			statusService.showProgress( frameId, numTimepoints );

		}
	}

	private void createSpotsFromLabelImage(@NotNull RandomAccessibleInterval<RealType<?>> img, int timepointId) {
		logger.debug("Computing mean, covariance of all labels at time-point t={}", timepointId);

		// get the maximum value possible to learn how many objects need to be instantiated
		// this is fine because we expect maximum occupancy here.
		int max = (int) img.randomAccess().setPositionAndGet(img.maxAsPoint()).getRealDouble();

		int[]     count = 	 new int[max];       // counts the number of pixels in each label, for normalization
		int[][]   sum =      new int[max][3];    // sums up the positions of the label pixels, used for the 1D means
		int[][][] mixedSum = new int[max][3][3]; // sums up the estimates of mixed coordinates (like xy). Used for covariances.

		readImageSumPositions(img, count, sum, mixedSum);

		createSpotsFromSums(timepointId, max, count, sum, mixedSum);
	}

	/**
	 * Use the gathered information to generate all the spots for the given timepoint.
	 * @param timepointId the timepoint of the image the spots should belong to.
	 * @param max the maximum value encountered in the image. Also equal to the number of labels.
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
	private void createSpotsFromSums(int timepointId, int max, int[] count, int[][] sum, int[][][] mixedSum) {
		// combine the sums into mean and covariance matrices, then add the corresponding spot
		logger.debug("adding spots for the {} labels found", max);
		double[] mean = new double[3];
		double[][] cov = new double[3][3];
		for (int labelIdx = 0; labelIdx < max; labelIdx++) {
			for (int i = 0; i < 3; i++) {
				mean[i] = sum[labelIdx][i] / (double) count[labelIdx];
				for (int j = i; j < 3; j++) { // the covariance matrix is symmetric!
					cov[i][j] = (mixedSum[labelIdx][i][j] - sum[labelIdx][i] * sum[labelIdx][j]) /
							(double) count[labelIdx]; // * 5.?
					if (i != j) {
						cov[j][i] = cov[i][j];
					}
				}
			}
			modelGraph.addVertex().init(timepointId, mean, cov);
		}
	}

	/**
	 * Reads the image and prepares the coordinates of all labels to obtain the 0D (count), 1D (sums), and 2D (mixed)
	 * sums to prep the ground for the mean and covariance estimates.
	 * @param img the pointer to the image to read.
	 * @param count an empty array to store the 0D sums (counts). Dimensions: [labelIdx].
	 * @param sum an empty array to store the 1D sums, i.e S[X]. Dimensions: [labelIdx, coord]
	 * @param mixedSum an empty array to store the 2D sums, i.e S[XY]. Dimensions: [labelIdx, coord, coord]
	 * @author Noam Dori
	 */
	private static void readImageSumPositions(RandomAccessibleInterval<RealType<?>> img, int[] count,
											  int[][] sum, int[][][] mixedSum) {
		// read the picture to sum everything up
		int[] position = new int[3];
		Cursor<RealType<?>> cursor = Views.iterable(img).cursor();
		while (cursor.hasNext())
		{
			int labelIdx = (int) cursor.next().getRealDouble() - 1; // we ignore 0 as it is BG
			if (labelIdx < 0) {
				continue;
			}
			cursor.localize(position);
			count[labelIdx]++;
			for (int i = 0; i < 3; i++) {
				sum[labelIdx][i] += position[i];
				for (int j = i; j < 3; j++) { // the covariance matrix is symmetric!
					mixedSum[labelIdx][i][j] += position[i] * position[j];
				}
			}
		}
	}
}
