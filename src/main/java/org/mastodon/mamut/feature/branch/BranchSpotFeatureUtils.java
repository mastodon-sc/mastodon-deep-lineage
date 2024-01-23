package org.mastodon.mamut.feature.branch;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.util.DepthFirstIteration;

import java.util.Arrays;
import java.util.Iterator;

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
	 * Returns the number of leaves of a branch spot.
	 * <p>
	 * A leaf is a branch spot that has no children.
	 * </p>
	 * Example:
	 * <pre>
	 *                      branchSpotA
	 * 	       ┌──────────────┴─────────────────┐
	 * 	       │                                │
	 * 	   branchSpotB                      branchSpotC
	 * 	                                 ┌──────┴───────┐
	 * 	                                 │              │
	 * 	                             branchSpotD branchSpotE
	 * </pre>
	 * In this example,
	 * <ul>
	 *     <li>branchSpotA has 3 leaves (branchSpotC, branchSpotD, branchSpotE)</li>
	 *     <li>branchSpotB has 1 leaf (branchSpotB)</li>
	 *     <li>branchSpotC has 2 leaves (branchSpotD, branchSpotE)</li>
	 *     <li>branchSpotD has 1 leaf (branchSpotD)</li>
	 *     <li>branchSpotE has 1 leaf (branchSpotE)</li>
	 * <ul>
	 *
	 * @param branchGraph the model branch graph, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the number of leaves of the branch spot
	 */
	public static int countLeaves( final ModelBranchGraph branchGraph, final BranchSpot branchSpot )
	{
		int n = 0;
		for ( DepthFirstIteration.Step< BranchSpot > step : DepthFirstIteration.forRoot( branchGraph, branchSpot ) )
		{
			if ( step.isLeaf() )
				n++;
		}
		return n;
	}

	/**
	 * Returns the total duration of the given branch spot and all its successors.
	 A leaf is a branch spot that has no children.
	 * </p>
	 * Example:
	 * <pre>
	 *                  branchSpotA (duration=3)
	 * 	       ┌──────────────┴─────────────┐
	 * 	       │                            │
	 * 	 branchSpotB (duration=2)     branchSpotC (duration=1)
	 * 	                             ┌──────┴───────────────────┐
	 * 	                             │                          │
	 * 	                       branchSpotD (duration=2)   branchSpotE (duration=3)
	 * </pre>
	 * In this example,
	 * <ul>
	 *     <li>branchSpotA = 11 (3+2+1+2+3)</li>
	 *     <li>branchSpotB = 2 (2)</li>
	 *     <li>branchSpotC = 6 (1+2+3)</li>
	 *     <li>branchSpotD = 2 (2)</li>
	 *     <li>branchSpotE = 3 (3)</li>
	 * <ul>
	 * @param branchGraph the model branch graph, which contains the branch spot
	 * @param branchSpot the branch spot
	 * @return the total duration of the branch spot and all its successors
	 */
	public static int totalBranchDurations( final ModelBranchGraph branchGraph, final BranchSpot branchSpot )
	{
		int totalDuration = 0;
		DepthFirstIterator< BranchSpot, BranchLink > it = new DepthFirstIterator<>( branchGraph );
		it.reset( branchSpot );
		while ( it.hasNext() )
		{
			BranchSpot node = it.next();
			int branchDuration = node.getTimepoint() - node.getFirstTimePoint();
			totalDuration += branchDuration;
		}
		return totalDuration;
	}
}
