package org.mastodon.mamut.feature.branch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.scijava.Context;

public class BranchNSubBranchSpotsFeatureComputerTest
{

	/**
	 * <h1>Spot-Graph</h1>
	 * <pre>
	 * 					Spot( 0, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                                                               │
	 *                                                               │
	 *                                            Spot( 1, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                                                               │
	 *                                                               │
	 *                                            Spot( 2, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                      ┌────────────────────────────────────────┴────────────────────┐
	 *                      │                                                             │
	 *  Spot( 11, X=0,00, Y=0,00, Z=0,00, tp=0 )                       Spot( 3, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                      │                                                             │
	 *                      │                                                             │
	 *  Spot( 12, X=0,00, Y=0,00, Z=0,00, tp=0 )                       Spot( 4, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                      │                                         ┌───────────────────┴────────────────────┐
	 *                      │                                         │                                        │
	 *  Spot( 13, X=0,00, Y=0,00, Z=0,00, tp=0 )   Spot( 8, X=0,00, Y=0,00, Z=0,00, tp=0 )  Spot( 5, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                                                                │                                        │
	 *                                                                │                                        │
	 *                                             Spot( 9, X=0,00, Y=0,00, Z=0,00, tp=0 )  Spot( 6, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                                                                │                                        │
	 *                                                                │                                        │
	 *                                            Spot( 10, X=0,00, Y=0,00, Z=0,00, tp=0 )  Spot( 7, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 * </pre>
	 * <h1>BranchSpot-Graph</h1>
	 * <pre>
	 *                        branchSpot0
	 * 	       ┌──────────────┴─────────────────┐
	 * 	       │                                │
	 * 	   branchspot1                      branchSpot2
	 * 	┌──────┴───────┐
	 * 	│              │
	 * branchspot3 branchSpot4
	 * </pre>
	 */
	@Test
	public void testComputeNumberOfSubtreeNodes1()
	{
		try(Context context = new Context())
		{
			final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
			Model model = new Model();
			ModelGraph modelGraph = model.getGraph();

			Spot spot0 = addNode( modelGraph, "0" );
			Spot spot1 = addNode( modelGraph, "1" );
			Spot spot2 = addNode( modelGraph, "2" );
			Spot spot3 = addNode( modelGraph, "3" );
			Spot spot4 = addNode( modelGraph, "4" );
			Spot spot5 = addNode( modelGraph, "5" );
			Spot spot6 = addNode( modelGraph, "6" );
			Spot spot7 = addNode( modelGraph, "7" );
			Spot spot8 = addNode( modelGraph, "8" );
			Spot spot9 = addNode( modelGraph, "9" );
			Spot spot10 = addNode( modelGraph, "10" );
			Spot spot11= addNode( modelGraph, "11" );
			Spot spot12 = addNode( modelGraph, "12" );
			Spot spot13 = addNode( modelGraph, "13" );

			modelGraph.addEdge( spot0, spot1 );
			modelGraph.addEdge( spot1, spot2 );
			modelGraph.addEdge( spot2, spot3 );
			modelGraph.addEdge( spot3, spot4 );
			modelGraph.addEdge( spot4, spot5 );
			modelGraph.addEdge( spot5, spot6 );
			modelGraph.addEdge( spot6, spot7 );
			modelGraph.addEdge( spot4, spot8 );
			modelGraph.addEdge( spot8, spot9 );
			modelGraph.addEdge( spot9, spot10 );
			modelGraph.addEdge( spot2, spot11 );
			modelGraph.addEdge( spot11, spot12 );
			modelGraph.addEdge( spot12, spot13 );

			model.getBranchGraph().graphRebuilt();
			featureComputerService.setModel( model );
			Feature<BranchSpot> feature = (Feature< BranchSpot> ) featureComputerService.compute( BranchNSubBranchSpotsFeature.SPEC ).get(BranchNSubBranchSpotsFeature.SPEC);

			ModelBranchGraph modelBranchGraph = model.getBranchGraph();
			BranchSpot ref = modelBranchGraph.vertexRef();
			assertEquals( 5, modelBranchGraph.vertices().size() );

			BranchSpot branchSpot0 = modelBranchGraph.getBranchVertex( spot0, modelBranchGraph.vertexRef() );
			assertEquals( 2, branchSpot0.outgoingEdges().size());
			assertEquals(4, feature.project( FeatureProjectionKey.key( new FeatureProjectionSpec( BranchNSubBranchSpotsFeature.KEY ) ) ).value( branchSpot0 ), 0);

			BranchSpot branchSpot1 = modelBranchGraph.getBranchVertex( spot4, modelBranchGraph.vertexRef() );
			assertEquals( 1, branchSpot1.incomingEdges().size());
			assertEquals( 2, branchSpot1.outgoingEdges().size());
			assertEquals(2, feature.project( FeatureProjectionKey.key( new FeatureProjectionSpec( BranchNSubBranchSpotsFeature.KEY ) ) ).value( branchSpot1 ), 0);

			BranchSpot branchSpot2 = modelBranchGraph.getBranchVertex( spot11, modelBranchGraph.vertexRef() );
			assertEquals( 1, branchSpot2.incomingEdges().size());
			assertEquals( 0, branchSpot2.outgoingEdges().size());
			assertEquals(0, feature.project( FeatureProjectionKey.key( new FeatureProjectionSpec( BranchNSubBranchSpotsFeature.KEY ) ) ).value( branchSpot2 ), 0);

			BranchSpot branchSpot3 = modelBranchGraph.getBranchVertex( spot8, modelBranchGraph.vertexRef() );
			assertEquals( 1, branchSpot3.incomingEdges().size());
			assertEquals( 0, branchSpot3.outgoingEdges().size());
			assertEquals(0, feature.project( FeatureProjectionKey.key( new FeatureProjectionSpec( BranchNSubBranchSpotsFeature.KEY ) ) ).value( branchSpot3 ), 0);

			BranchSpot branchSpot4 = modelBranchGraph.getBranchVertex( spot5, modelBranchGraph.vertexRef() );
			assertEquals( 1, branchSpot4.incomingEdges().size());
			assertEquals( 0, branchSpot4.outgoingEdges().size());
			assertEquals(0, feature.project( FeatureProjectionKey.key( new FeatureProjectionSpec( BranchNSubBranchSpotsFeature.KEY ) ) ).value( branchSpot4 ), 0);

			modelBranchGraph.releaseRef( ref );
		}
	}

	/**
	 * <h1>Spot-Graph</h1>
	 * <pre>
	 *  Spot( 0, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                     │
	 *                     │
	 *  Spot( 1, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                     │
	 *                     │
	 *  Spot( 2, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                     │
	 *                     │
	 *  Spot( 3, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 *                     │
	 *                     │
	 *  Spot( 4, X=0,00, Y=0,00, Z=0,00, tp=0 )
	 * </pre>
	 * <h1>BranchSpot-Graph</h1>
	 *
	 *     branchSpot0
	 *
	 */
	@Test
	public void testComputeNumberOfSubtreeNodes2()
	{
		try(Context context = new Context())
		{
			final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
			Model model = new Model();
			ModelGraph modelGraph = model.getGraph();

			Spot spot0 = addNode( modelGraph, "0" );
			Spot spot1 = addNode( modelGraph, "1" );
			Spot spot2 = addNode( modelGraph, "2" );
			Spot spot3 = addNode( modelGraph, "3" );
			Spot spot4 = addNode( modelGraph, "4" );


			modelGraph.addEdge( spot0, spot1 );
			modelGraph.addEdge( spot1, spot2 );
			modelGraph.addEdge( spot2, spot3 );
			modelGraph.addEdge( spot3, spot4 );

			// TreeOutputter<Spot, Link > treeOutputter = new TreeOutputter<>( modelGraph );
			// System.out.println(treeOutputter.get( spot0 ));

			model.getBranchGraph().graphRebuilt();

			featureComputerService.setModel( model );
			Feature<BranchSpot> feature = (Feature< BranchSpot> ) featureComputerService.compute( BranchNSubBranchSpotsFeature.SPEC ).get(BranchNSubBranchSpotsFeature.SPEC);

			ModelBranchGraph modelBranchGraph = model.getBranchGraph();
			BranchSpot ref = modelBranchGraph.vertexRef();
			assertEquals( 1, modelBranchGraph.vertices().size() );

			BranchSpot branchSpot0 = modelBranchGraph.getBranchVertex( spot0, modelBranchGraph.vertexRef() );
			assertEquals( 0, branchSpot0.outgoingEdges().size());
			assertEquals( 0, branchSpot0.incomingEdges().size());
			assertEquals(0, feature.project( FeatureProjectionKey.key( new FeatureProjectionSpec( BranchNSubBranchSpotsFeature.KEY ) ) ).value( branchSpot0 ), 0);

			modelBranchGraph.releaseRef( ref );
		}
	}

	private Spot addNode( ModelGraph graph, String label )
	{
		Spot a = graph.addVertex();
		a.setLabel( label );
		return a;
	}
}
