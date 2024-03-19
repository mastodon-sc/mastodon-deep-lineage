package org.mastodon.mamut.feature.branch.exampleGraph;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Represents a {@link AbstractExampleGraph} with the following {@link ModelGraph}:
 *
 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
 * <pre>
 * Spot( 0, X=1, Y=1, tp=0 )        Spot( 1, X=0, Y=1, tp=0 )       Spot( 2, X=2, Y=1, tp=0 )
 *
 * Spot( 0, X=1, Y=2, tp=1 )        Spot( 1, X=0, Y=0, tp=1 )       Spot( 2, X=2, Y=0, tp=1 )
 *
 * Spot( 0, X=1, Y=3, tp=2 )        Spot( 1, X=0, Y=-1, tp=2 )      Spot( 3, X=2, Y=-1, tp=2 )
 *
 * Spot( 0, X=1, Y=4, tp=3 )        Spot( 0, X=0, Y=-2, tp=3 )
 * </pre>
 */
public class ExampleGraph6 extends AbstractExampleGraph
{

	public final Spot spot0;

	public final Spot spot1;

	public final Spot spot2;

	public final Spot spot3;

	public final Spot spot4;

	public final Spot spot5;

	public final Spot spot6;

	public final Spot spot7;

	public final Spot spot8;

	public final Spot spot9;

	public final Spot spot10;

	public ExampleGraph6()
	{
		spot0 = addNode( "0", 0, new double[] { 1d, 1d, 0d } );
		spot1 = addNode( "0", 1, new double[] { 1d, 2d, 0d } );
		spot2 = addNode( "0", 2, new double[] { 1d, 3d, 0d } );
		spot3 = addNode( "1", 0, new double[] { 0d, 1d, 0d } );
		spot4 = addNode( "1", 1, new double[] { 0d, 0d, 0d } );
		spot5 = addNode( "1", 2, new double[] { 0d, -1d, 0d } );
		spot6 = addNode( "2", 0, new double[] { 2d, 1d, 0d } );
		spot7 = addNode( "2", 1, new double[] { 2d, 0d, 0d } );
		spot8 = addNode( "3", 2, new double[] { 2d, -1d, 0d } );
		spot9 = addNode( "0", 3, new double[] { 1d, 4d, 0d } );
		spot10 = addNode( "0", 3, new double[] { 0d, -2d, 0d } );
	}
}
