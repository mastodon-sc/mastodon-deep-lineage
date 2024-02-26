package org.mastodon.mamut.feature.spot;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.spatial.SpatialIndex;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class with methods for spot features.
 */
public class SpotFeatureUtils
{
	private SpotFeatureUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Returns the movement vector of a spot with respect to its predecessor.
	 * <p>
	 * The movement vector is the difference between the position of the spot and the position of its predecessor.
	 * If the spot has no predecessor, {@code null} is returned.
	 * @param spot the spot. If null, an empty array is returned.
	 * @return the relative movement vector of the spot
	 */
	@SuppressWarnings( "squid:S1168" )
	public static double[] spotMovement( final Spot spot )
	{
		if ( spot == null )
			throw new IllegalArgumentException( "Spot is null." );
		double[] spotPosition = new double[ spot.numDimensions() ];
		if ( spot.incomingEdges() == null || spot.incomingEdges().isEmpty() )
			return null;
		spot.localize( spotPosition );
		double[] predecessorPosition = new double[ spot.numDimensions() ];
		spot.incomingEdges().get( 0 ).getSource().localize( predecessorPosition );
		double[] relativeMovement = new double[ spot.numDimensions() ];
		LinAlgHelpers.subtract( spotPosition, predecessorPosition, relativeMovement );
		return relativeMovement;
	}

	/**
	 * Returns the relative movement vector of a spot with respect to its n nearest neighbors at the same timepoint.
	 * <p>
	 * Some edge cases:
	 * <ul>
	 *     <li>If the spot is null, an IllegalArgumentException is thrown.</li>
	 *     <li>If the number of neighbors is less than 1, an IllegalArgumentException is thrown.</li>
	 *     <li>If the spot has no (moving) neighbors, an empty array is returned.</li>
	 *     <li>If the spot has no predecessor, an empty array is returned.</li>
	 * </ul>
	 * @param spot the spot. If null, an IllegalArgumentException is thrown.
	 * @param n the number of neighbors to consider. Must be at least 1. If less than 1, an IllegalArgumentException is thrown.
	 * @param model the model
	 * @return the relative movement vector of the spot
	 */
	public static double[] relativeMovement( final Spot spot, final int n, final Model model )
	{
		Function< Spot, double[] > movementProvider = SpotFeatureUtils::spotMovement;
		return relativeMovement( spot, n, model, movementProvider );
	}

	/**
	 * Returns the relative movement vector of a spot with respect to its n nearest neighbors at the same timepoint.
	 * <p>
	 * Some edge cases:
	 * <ul>
	 *     <li>If the spot is null, an IllegalArgumentException is thrown.</li>
	 *     <li>If the number of neighbors is less than 1, an IllegalArgumentException is thrown.</li>
	 *     <li>If the spot has no (moving) neighbors, an empty array is returned.</li>
	 *     <li>If the spot has no predecessor, an empty array is returned.</li>
	 * </ul>
	 * @param spot the spot. If null, an IllegalArgumentException is thrown.
	 * @param n the number of neighbors to consider. Must be at least 1. If less than 1, an IllegalArgumentException is thrown.
	 * @param model the model
	 * @param movementProvider the function that provides the movement vector of a spot
	 * @return the relative movement vector of the spot
	 */
	@SuppressWarnings( "squid:S1168" )
	public static double[] relativeMovement( final Spot spot, final int n, final Model model,
			final Function< Spot, double[] > movementProvider )
	{
		if ( spot == null )
			throw new IllegalArgumentException( "Spot is null." );
		if ( n < 1 )
			throw new IllegalArgumentException( "Number of neighbors must be at least 1." );
		Predicate< Spot > excludeRootNeighbors = neighbor -> neighbor.incomingEdges().isEmpty();
		List< Spot > neighbors = getNNearestNeighbors( model, spot, n, excludeRootNeighbors );
		if ( neighbors.isEmpty() )
			return null;
		double[] spotMovement = movementProvider.apply( spot );
		if ( spotMovement == null )
			return null;
		List< double[] > neighborMovements = new ArrayList<>();
		for ( Spot neighbor : neighbors )
		{
			double[] neighborMovement = movementProvider.apply( neighbor );
			if ( neighborMovement == null )
				continue;
			neighborMovements.add( neighborMovement );
		}
		if ( neighborMovements.isEmpty() )
			return null;
		double[] cumulatedNeighborMovement = new double[ spotMovement.length ];
		neighborMovements
				.forEach( neighborMovement -> LinAlgHelpers.add( cumulatedNeighborMovement, neighborMovement, cumulatedNeighborMovement ) );
		double[] averageNeighborMovement = new double[ spotMovement.length ];
		LinAlgHelpers.scale( cumulatedNeighborMovement, 1d / neighborMovements.size(), averageNeighborMovement );
		double[] relativeMovement = new double[ spotMovement.length ];
		LinAlgHelpers.subtract( spotMovement, averageNeighborMovement, relativeMovement );
		return relativeMovement;
	}

	/**
	 * Returns the n nearest neighbors of a spot at the same timepoint. The spot itself is not included in the list.
	 * @param model the model
	 * @param spot the spot
	 * @param n the number of neighbors
	 * @param neighborFilter an optional filter to apply to the neighbors of the spot (can be null)
	 * @return the n nearest neighbors of the spot
	 */
	public static List< Spot > getNNearestNeighbors( final Model model, final Spot spot, final int n,
			@Nullable final Predicate< Spot > neighborFilter )
	{
		// Spatial search.
		final SpatialIndex< Spot > spatialIndex = model.getSpatioTemporalIndex().getSpatialIndex( spot.getTimepoint() );
		final IncrementalNearestNeighborSearch< Spot > search = spatialIndex.getIncrementalNearestNeighborSearch();
		search.search( spot );
		final RefList< Spot > neighbors = RefCollections.createRefList( model.getGraph().vertices() );
		while ( search.hasNext() && neighbors.size() < n )
		{
			Spot neighbor = search.next();
			if ( neighbor.equals( spot ) || ( neighborFilter != null && neighborFilter.test( neighbor ) ) )
				continue;
			neighbors.add( neighbor );
		}
		return neighbors;
	}
}
