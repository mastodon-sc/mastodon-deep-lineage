package org.mastodon.mamut.feature.branch.exampleGraph;

import java.util.Random;

import org.mastodon.mamut.model.Spot;

public class ExampleGraph7 extends AbstractExampleGraph
{
	private int counter = 0;

	private final Random random = new Random( 42 );

	public ExampleGraph7()
	{
		for ( int i = 0; i < 50; i++ )
		{
			addBranchSpot();
		}
	}

	private void addBranchSpot()
	{

		Spot spot0 = addSpot( 0 );
		counter++;
		Spot spot1 = addSpot( 1 );
		counter++;
		Spot spot2 = addSpot( 2 );
		counter++;

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );

		getBranchSpot( spot0 );
	}

	private Spot addSpot( int timepoint )
	{
		int range = 20;
		return addNode( String.valueOf( counter ), timepoint,
				new double[] { random.nextInt( range ), random.nextInt( range ), random.nextInt( range ) } );
	}
}
