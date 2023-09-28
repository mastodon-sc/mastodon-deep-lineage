package org.mastodon.mamut.segment;

import bdv.viewer.Source;
import mpicbg.spim.data.sequence.TimePoint;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
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
		Spot spot = modelGraph.addVertex();
		int timepointId = 0;
		double[] center = new double[] { 50, 50, 50 };
		double[][] cov = new double[][] { { 400, 20, -10 }, { 20, 200, 30 }, { -10, 30, 100 } };
		spot.init( timepointId, center, cov );
		int numTimepoints = timePoints.size();

		for ( TimePoint frame : timePoints )
		{
			int frameId = frame.getId();
			long[] dimensions = source.getSource( frameId, 0 ).dimensionsAsLongArray();
			final RandomAccessibleInterval< RealType< ? > > img = source.getSource( frameId, 0 );
			for ( int d = 0; d < dimensions.length; d++ )
				logger.debug( "Dimension {}, : {}", d, dimensions[ d ] );
			IterableInterval< RealType< ? > > iterable = Views.iterable( img );
			double[] mean = computeMean( iterable, 42 );
			double[][] coviarance = computeCovariance( iterable, mean, 42 );
			statusService.showProgress( frameId, numTimepoints );

		}
	}

	/**
	 * Computes the mean position of the pixels whose value equals {@code labelValue}.
	 */
	private static double[] computeMean( IterableInterval< RealType< ? > > iterable, int labelValue )
	{
		logger.debug( "Computing mean of label, {} ", labelValue );
		Cursor< RealType< ? > > cursor = iterable.cursor();
		double[] sum = new double[ 3 ];
		double[] position = new double[ 3 ];
		long counter = 0;
		while ( cursor.hasNext() )
		{
			counter++;
			int pixelValue = ( int ) cursor.next().getRealDouble();
			if ( pixelValue == labelValue )
			{
				cursor.localize( position );
				LinAlgHelpers.add( sum, position, sum );
				counter++;
			}
		}
		logger.debug( "Computed mean of label {}. Searched {} pixels.", labelValue, counter );
		LinAlgHelpers.scale( sum, 1. / counter, sum );
		return sum;
	}

	/**
	 * Computes the covariance matrix of the pixels whose value equals {@code labelValue}.
	 */
	private static double[][] computeCovariance( IterableInterval< RealType< ? > > iterable, double[] mean, int labelValue )
	{
		Cursor< RealType< ? > > cursor = iterable.cursor();
		long counter = 0;
		double[] position = new double[ 3 ];
		double[][] covariance = new double[ 3 ][ 3 ];
		cursor.reset();
		while ( cursor.hasNext() )
		{
			int pixelValue = ( int ) cursor.next().getRealDouble();
			if ( pixelValue == labelValue )
			{
				cursor.localize( position );
				LinAlgHelpers.subtract( position, mean, position );
				for ( int i = 0; i < 3; i++ )
					for ( int j = 0; j < 3; j++ )
						covariance[ i ][ j ] += position[ i ] * position[ j ];
				counter++;
			}
		}

		scale( covariance, 5. / counter ); // I don't know why the factor 5 is needed. But it works.
		return covariance;
	}

	private static void scale( double[][] covariance, double factor )
	{
		for ( int i = 0; i < 3; i++ )
			for ( int j = 0; j < 3; j++ )
				covariance[ i ][ j ] *= factor;
	}
}
