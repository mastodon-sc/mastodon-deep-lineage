package org.mastodon.mamut.feature.exampleGraph;

import org.mastodon.mamut.feature.branch.exampleGraph.AbstractExampleGraph;
import org.mastodon.mamut.model.Spot;

public class ExampleGraph3 extends AbstractExampleGraph
{

	public final Spot spot0;

	public ExampleGraph3()
	{
		super();
		spot0 = addNode( "0", 0, new double[] { 1d, 2d, 3d } );
	}
}
