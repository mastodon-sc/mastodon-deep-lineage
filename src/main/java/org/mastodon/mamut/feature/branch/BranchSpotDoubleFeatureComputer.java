package org.mastodon.mamut.feature.branch;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.plugin.Parameter;

import java.util.Arrays;
import java.util.Iterator;

public abstract class BranchSpotDoubleFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{
	@Parameter
	protected Model model;

	@Override
	public void run()
	{
		super.deleteCancelReason();
		computeAll( getOutput() );
	}

	protected abstract DoublePropertyFeature< BranchSpot > getOutput();

	private void computeAll( final DoublePropertyFeature< BranchSpot > output )
	{
		for ( BranchSpot branchSpot : model.getBranchGraph().vertices() )
		{
			if ( isCanceled() )
				break;
			output.map.set( branchSpot, computeValue( branchSpot ) );
		}
	}

	protected abstract double computeValue( final BranchSpot branchSpot );

	protected Iterator< Spot > getSpotIterator( final BranchSpot branchSpot )
	{
		return model.getBranchGraph().vertexBranchIterator( branchSpot );
	}

	protected double accumulatedDistance( final BranchSpot branchSpot )
	{
		final double[] currentCoordinates = new double[ branchSpot.numDimensions() ];

		Spot spot;
		// accumulated distance during the life cycle of the branch
		double accumulatedDistance = 0d;

		final Iterator< Spot > spotIterator = getSpotIterator( branchSpot );
		if ( !spotIterator.hasNext() )
			return Double.NaN;
		spot = spotIterator.next();
		if ( !spotIterator.hasNext() )
			return Double.NaN;

		spot.localize( currentCoordinates );
		double[] previousCoordinates = Arrays.copyOf( currentCoordinates, currentCoordinates.length );

		while ( spotIterator.hasNext() )
		{
			spot = spotIterator.next();
			spot.localize( currentCoordinates );
			accumulatedDistance += LinAlgHelpers.distance( currentCoordinates, previousCoordinates );
			System.arraycopy( currentCoordinates, 0, previousCoordinates, 0, currentCoordinates.length );
		}

		return accumulatedDistance;
	}
}
