package org.mastodon.mamut.treesimilarity.tree;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

public class BranchSpotTreeExamples
{
	/**
	 * <pre>
	 *        					   node1(lifespan=20)
	 * 		              ┌-─────────┴─────────────┐
	 * 		              │                        │
	 * 		            node2(lifespan=10)    node3(lifespan=30)
	 * </pre>
	 */
	public static Tree< Integer > tree1()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 20, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 20, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 30, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 20, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 50, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 60 );
	}

	/**
	 * <pre>
	 *    		  				     node1(lifespan=30)
	 * 		                ┌-─────────┴─────────────┐
	 * 		                │                        │
	 * 		              node2(lifespan=10)    node3(lifespan=20)
	 * </pre>
	 */
	public static Tree< Integer > tree2()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 30, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 30, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 40, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 30, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 50, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 60 );
	}
}
