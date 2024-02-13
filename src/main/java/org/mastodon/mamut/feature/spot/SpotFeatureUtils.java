package org.mastodon.mamut.feature.spot;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.spatial.SpatialIndex;

import java.util.ArrayList;
import java.util.List;

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
	 * If the spot has no predecessor, an empty array is returned.
	 * @param spot the spot. If null, an empty array is returned.
	 * @return the relative movement vector of the spot
	 */
	public static double[] spotMovement( final Spot spot )
	{
		if ( spot == null )
			return new double[ 0 ];
		double[] spotPosition = new double[ spot.numDimensions() ];
		if ( spot.incomingEdges() == null || spot.incomingEdges().isEmpty() )
			return new double[ 0 ];
		spot.localize( spotPosition );
		double[] predecessorPosition = new double[ spot.numDimensions() ];
		spot.incomingEdges().get( 0 ).getSource().localize( predecessorPosition );
		double[] relativeMovement = new double[ spot.numDimensions() ];
		LinAlgHelpers.subtract( spotPosition, predecessorPosition, relativeMovement );
		return relativeMovement;
	}

	/**
	 * Returns the relative movement vector of a spot with respect to its n nearest neighbors at the same timepoint.
	 * @param spot the spot. If null, an IllegalArgumentException is thrown.
	 * @param n the number of neighbors to consider. Must be at least 1. If less than 1, an IllegalArgumentException is thrown.
	 * @param model the model
	 * @return the relative movement vector of the spot
	 */
	public static double[] relativeMovement( final Spot spot, final int n, final Model model )
	{
		if ( spot == null )
			throw new IllegalArgumentException( "Spot is null." );
		if ( n < 1 )
			throw new IllegalArgumentException( "Number of neighbors must be at least 1." );
		List< Spot > neighbors = getNearestNeighbors( model, spot, n );
		if ( neighbors.isEmpty() )
			return new double[ 0 ];
		double[] spotMovement = spotMovement( spot );
		if ( spotMovement.length == 0 )
			return new double[ 0 ];
		List< double[] > neighborMovements = new ArrayList<>();
		for ( Spot neighbor : neighbors )
		{
			double[] neighborMovement = spotMovement( neighbor );
			if ( neighborMovement.length == 0 )
				continue;
			neighborMovements.add( neighborMovement );
		}
		if ( neighborMovements.isEmpty() )
			return new double[ 0 ];
		double[] cumulatedNeighborMovement = new double[ spotMovement.length ];
		neighborMovements
				.forEach( neighborMovement -> LinAlgHelpers.add( cumulatedNeighborMovement, neighborMovement, cumulatedNeighborMovement ) );
		double[] averageNeighborMovement = new double[ spotMovement.length ];
		LinAlgHelpers.scale( cumulatedNeighborMovement, 1d / neighbors.size(), averageNeighborMovement );
		double[] relativeMovement = new double[ spotMovement.length ];
		LinAlgHelpers.subtract( spotMovement, averageNeighborMovement, relativeMovement );
		return relativeMovement;
	}

	/**
	 * Returns the n nearest neighbors of a spot at the same timepoint. The spot itself is not included in the list.
	 * @param model the model
	 * @param spot the spot
	 * @param n the number of neighbors
	 * @return the n nearest neighbors of the spot
	 */
	public static List< Spot > getNearestNeighbors( final Model model, final Spot spot, final int n )
	{
		// Spatial search.
		final SpatialIndex< Spot > spatialIndex = model.getSpatioTemporalIndex().getSpatialIndex( spot.getTimepoint() );
		final IncrementalNearestNeighborSearch< Spot > search = spatialIndex.getIncrementalNearestNeighborSearch();
		search.search( spot );
		final RefList< Spot > neighbors = RefCollections.createRefList( model.getGraph().vertices() );
		while ( search.hasNext() && neighbors.size() < n )
		{
			Spot neighbor = search.next();
			if ( neighbor.equals( spot ) || neighbor.incomingEdges().isEmpty() )
				continue;
			neighbors.add( neighbor );
		}
		return neighbors;
	}
}
