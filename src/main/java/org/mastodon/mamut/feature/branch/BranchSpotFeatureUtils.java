package org.mastodon.mamut.feature.branch;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.mamut.feature.spot.SpotFeatureUtils;
import org.mastodon.mamut.feature.spot.movement.relative.SpotRelativeMovementFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Utility class with methods for branch spot features.
 */
public class BranchSpotFeatureUtils
{
	private BranchSpotFeatureUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Returns an iterator over the spots of a branch spot.
	 * <p>
	 * <strong>Remember to call model.getBranchGraph().releaseIterator(...) after you are done using the iterator.</strong>
	 * </p>
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return an iterator over the spots of the branch spot
	 * @see Model#getBranchGraph()
	 * @see BranchGraph#releaseIterator(Iterator)
	 */
	static Iterator< Spot > getSpotIterator( final Model model, final BranchSpot branchSpot )
	{
		return model.getBranchGraph().vertexBranchIterator( branchSpot );
	}

	/**
	 * Returns a Spot object that can be used as a pointer in the SpotGraph.
	 * <p>
	 * <strong>Remember to call model.getGraph().releaseRef(...) after you are done using this object.</strong>
	 * </p>
	 * @param model the model, which contains the spot graph
	 * @return the reference object pointer
	 * @see Model#getGraph() ()
	 * @see Graph#releaseRef(Vertex)
	 */
	static Spot getSpotRef( final Model model )
	{
		return model.getGraph().vertexRef();
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
		model.getBranchGraph().releaseIterator( spotIterator );

		return cumulatedDistance;
	}

	/**
	 * Computes the direct distance of a branch spot, i.e. the distance between the first and the last spot of the branch.
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the direct distance of the branch spot
	 */
	public static double directDistance( final Model model, final BranchSpot branchSpot )
	{
		return LinAlgHelpers.distance( getFirstSpotCoordinates( model, branchSpot ), getLastSpotCoordinates( model, branchSpot ) );
	}

	/**
	 * Computes the normalized direction of a branch spot, i.e. the direction from the first to the last spot of the branch, normalized to length 1.
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the normalized direction of the branch spot
	 */
	public static double[] normalizedDirection( final Model model, final BranchSpot branchSpot )
	{
		final double[] first = getFirstSpotCoordinates( model, branchSpot );
		final double[] last = getLastSpotCoordinates( model, branchSpot );
		final double[] direction = new double[ first.length ];
		LinAlgHelpers.subtract( last, first, direction );
		LinAlgHelpers.normalize( direction );
		return direction;
	}

	/**
	 * Returns the coordinates of the first spot of a branch spot.
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the coordinates of the first spot of the branch spot
	 */
	public static double[] getFirstSpotCoordinates( final Model model, final BranchSpot branchSpot )
	{
		Spot ref = getSpotRef( model );
		Spot first = model.getBranchGraph().getFirstLinkedVertex( branchSpot, ref );
		final double[] firstCoordinates = new double[ branchSpot.numDimensions() ];
		first.localize( firstCoordinates );
		model.getGraph().releaseRef( ref );
		return firstCoordinates;
	}

	/**
	 * Returns the coordinates of the last spot of a branch spot.
	 * @param model the model, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the coordinates of the last spot of the branch spot
	 */
	public static double[] getLastSpotCoordinates( final Model model, final BranchSpot branchSpot )
	{
		Spot ref = getSpotRef( model );
		Spot last = model.getBranchGraph().getLastLinkedVertex( branchSpot, ref );
		final double[] lastCoordinates = new double[ branchSpot.numDimensions() ];
		last.localize( lastCoordinates );
		model.getGraph().releaseRef( ref );
		return lastCoordinates;
	}

	/**
	 * Computes the normalized relative movement direction of a branch spot relative to its nearest neighbours during its life cycle.
	 * <p>
	 * The normalized relative movement of a branch spot is equivalent to the normalized sum of relative movement vectors of the branch spot's spots.
	 * <p>
	 * A single relative movement vector results from difference of the movement of the spot (considering its predecessor) and the average movement of its neighbors.
	 *
	 * @param branchSpot the branch spot
	 * @param numberOfNeighbors the number of nearest neighbors to consider
	 * @param model the model, which contains the branch spot
	 * @return the relative movement of the branch spot
	 */
	public static double[] normalizedRelativeMovementDirection( final BranchSpot branchSpot, final int numberOfNeighbors,
			final Model model )
	{
		Function< Spot, double[] > spotRelativeMovementProvider =
				spot -> SpotFeatureUtils.relativeMovement( spot, numberOfNeighbors, model );
		return normalizedRelativeMovementDirection( branchSpot, spotRelativeMovementProvider, model );
	}

	/**
	 * Computes the normalized relative movement direction of a branch spot relative to its nearest neighbours during its life cycle.
	 * <p>
	 * The normalized relative movement of a branch spot is equivalent to the normalized sum of relative movement vectors of the branch spot's spots.
	 * <p>
	 * A single relative movement vector results from difference of the movement of the spot (considering its predecessor) and the average movement of its neighbors.
	 *
	 * @param branchSpot the branch spot
	 * @param feature the relative movement feature
	 * @param model the model, which contains the branch spot
	 * @return the relative movement of the branch spot
	 */
	public static double[] normalizedRelativeMovementDirection( final BranchSpot branchSpot, final SpotRelativeMovementFeature feature,
			final Model model )
	{
		Function< Spot, double[] > spotRelativeMovementProvider =
				spot -> new double[] { feature.x.get( spot ), feature.y.get( spot ), feature.z.get( spot ) };
		return normalizedRelativeMovementDirection( branchSpot, spotRelativeMovementProvider, model );
	}

	/**
	 * Computes the normalized relative movement direction of a branch spot relative to its nearest neighbours during its life cycle.
	 * <p>
	 * The normalized relative movement of a branch spot is equivalent to the normalized sum of relative movement vectors of the branch spot's spots.
	 * <p>
	 * A single relative movement vector results from difference of the movement of the spot (considering its predecessor) and the average movement of its neighbors.
	 *
	 * @param branchSpot the branch spot
	 * @param spotRelativeMovementProvider the function that provides the relative movement for a specific spot
	 * @param model the model, which contains the branch spot
	 * @return the relative movement of the branch spot
	 */
	private static double[] normalizedRelativeMovementDirection( final BranchSpot branchSpot,
			final Function< Spot, double[] > spotRelativeMovementProvider,
			final Model model )
	{
		final Iterator< Spot > spotIterator = getSpotIterator( model, branchSpot );
		if ( !spotIterator.hasNext() )
			return new double[ 0 ];
		double[] cumulatedRelativeMovement = new double[ branchSpot.numDimensions() ];
		Spot spot;
		while ( spotIterator.hasNext() )
		{
			spot = spotIterator.next();
			if ( spot.incomingEdges().isEmpty() )
				continue;
			double[] relativeMovement = spotRelativeMovementProvider.apply( spot );
			LinAlgHelpers.add( cumulatedRelativeMovement, relativeMovement, cumulatedRelativeMovement );
		}
		model.getBranchGraph().releaseIterator( spotIterator );
		LinAlgHelpers.normalize( cumulatedRelativeMovement );
		return cumulatedRelativeMovement;
	}

	/**
	 * Computes the amount of movement of a branch spot relative to its nearest neighbours during its life cycle.
	 * <p>
	 * The relative movement of a branch spot is equivalent to the average of the relative movements of the branch spot's spots.
	 *
	 * @param branchSpot the branch spot
	 * @param numberOfNeighbors the number of nearest neighbors to consider
	 * @param model the model, which contains the branch spot
	 * @return the relative movement of the branch spot
	 */
	public static double relativeMovement( final BranchSpot branchSpot, final int numberOfNeighbors, final Model model )
	{
		Function< Spot, double[] > spotRelativeMovementProvider =
				spot -> SpotFeatureUtils.relativeMovement( spot, numberOfNeighbors, model );
		return relativeMovement( branchSpot, spotRelativeMovementProvider, model );
	}

	/**
	 * Computes the amount of movement of a branch spot relative to its nearest neighbours during its life cycle.
	 * <p>
	 * The relative movement of a branch spot is equivalent to the average of the relative movements of the branch spot's spots.
	 *
	 * @param branchSpot the branch spot
	 * @param feature the relative movement feature
	 * @param model the model, which contains the branch spot
	 * @return the relative movement of the branch spot
	 */
	public static double relativeMovement( final BranchSpot branchSpot, final SpotRelativeMovementFeature feature, final Model model )
	{
		Function< Spot, double[] > spotRelativeMovementProvider =
				spot -> new double[] { feature.x.get( spot ), feature.y.get( spot ), feature.z.get( spot ) };
		return relativeMovement( branchSpot, spotRelativeMovementProvider, model );
	}

	/**
	 * Computes the amount of movement of a branch spot relative to its nearest neighbours during its life cycle.
	 * <p>
	 * The relative movement of a branch spot is equivalent to the average of the relative movements of the branch spot's spots.
	 *
	 * @param branchSpot the branch spot
	 * @param spotRelativeMovementProvider the function that provides the relative movement for a specific spot
	 * @param model the model, which contains the branch spot
	 * @return the relative movement of the branch spot
	 */
	static double relativeMovement( final BranchSpot branchSpot, final Function< Spot, double[] > spotRelativeMovementProvider,
			final Model model )
	{
		final Iterator< Spot > spotIterator = getSpotIterator( model, branchSpot );
		if ( !spotIterator.hasNext() )
			return Double.NaN;
		double cumulatedRelativeMovement = 0d;
		int numSpots = 0;
		Spot spot;
		while ( spotIterator.hasNext() )
		{
			spot = spotIterator.next();
			if ( spot.incomingEdges().isEmpty() )
				continue;
			double[] relativeMovement = spotRelativeMovementProvider.apply( spot );
			cumulatedRelativeMovement += LinAlgHelpers.length( relativeMovement );
			numSpots++;
		}
		if ( numSpots > 0 )
			return cumulatedRelativeMovement / numSpots;
		return Double.NaN;
	}
}
