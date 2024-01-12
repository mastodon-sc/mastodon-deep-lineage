package org.mastodon.mamut.feature.branch;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.Arrays;
import java.util.Iterator;

public class BranchSpotFeatureUtils
{
	private BranchSpotFeatureUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Returns an iterator over the spots of a branch spot.
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return an iterator over the spots of the branch spot
	 */
	public static Iterator< Spot > getSpotIterator( final Model model, final BranchSpot branchSpot )
	{
		return model.getBranchGraph().vertexBranchIterator( branchSpot );
	}

	/**
	 * Computes the cumulated distance of a branch spot, i.e. the sum of the distances between all spots of the branch.
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the cumulated distance of the branch spot
	 */
	public static double cumulatedDistance( final Model model, final BranchSpot branchSpot )
	{
		final double[] currentCoordinates = new double[ branchSpot.numDimensions() ];

		Spot spot;
		// cumulated distance during the life cycle of the branch
		double cumulatedDistance = 0d;

		final Iterator< Spot > spotIterator = getSpotIterator( model, branchSpot );
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
			cumulatedDistance += LinAlgHelpers.distance( currentCoordinates, previousCoordinates );
			System.arraycopy( currentCoordinates, 0, previousCoordinates, 0, currentCoordinates.length );
		}

		return cumulatedDistance;
	}
}
