package org.mastodon.mamut.feature.branch.exampleGraph;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

/**
 * Represents a {@link AbstractExampleGraph} with the following {@link ModelGraph} and {@link ModelBranchGraph}:
 *
 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
 * <pre>
 * Spot( 0, X=1, Y=1, tp=0 )		Spot( 3, X=0, Y=1, tp=0 )		Spot( 0, X=2, Y=1, tp=0 )
 *              │								 │								 │
 * Spot( 1, X=1, Y=2, tp=1 )		Spot( 4, X=0, Y=0, tp=1 )	    Spot( 1, X=2, Y=0, tp=1 )
 *              │								 │								 │
 * Spot( 2, X=1, Y=3, tp=2 )        Spot( 5, X=0, Y=-1, tp=2 )		Spot( 2, X=2, Y=-1, tp=2 )
 * </pre>
 * <h1>Branch-Graph (i.e. Graph of BranchSpots)</h1>
 * <pre>
 * branchSpotA						branchSpotB						branchSpotC
 * </pre>
 */
public class ExampleGraph3 extends AbstractExampleGraph
{

	public final BranchSpot branchSpotA;

	public ExampleGraph3()
	{
		Spot spot0 = addNode( "0", 0, new double[] { 1d, 1d, 0d } );
		Spot spot1 = addNode( "1", 1, new double[] { 1d, 2d, 0d } );
		Spot spot2 = addNode( "2", 2, new double[] { 1d, 3d, 0d } );
		Spot spot3 = addNode( "3", 0, new double[] { 0d, 1d, 0d } );
		Spot spot4 = addNode( "4", 1, new double[] { 0d, 0d, 0d } );
		Spot spot5 = addNode( "5", 2, new double[] { 0d, -1d, 0d } );
		Spot spot6 = addNode( "6", 0, new double[] { 2d, 1d, 0d } );
		Spot spot7 = addNode( "7", 1, new double[] { 2d, 0d, 0d } );
		Spot spot8 = addNode( "8", 2, new double[] { 2d, -1d, 0d } );

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );
		addEdge( spot3, spot4 );
		addEdge( spot4, spot5 );
		addEdge( spot6, spot7 );
		addEdge( spot7, spot8 );

		branchSpotA = getBranchSpot( spot0 );
		BranchSpot branchSpotB = getBranchSpot( spot3 );
		BranchSpot branchSpotC = getBranchSpot( spot6 );
	}
}
