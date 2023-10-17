package org.mastodon.mamut.treesimilarity.tree;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

public class BranchSpotTreeExamples
{
	public static BranchSpotTree emptyTree()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 0 );
	}

	/**
	 * <pre>
	 *                           branchSpot1(lifespan=20)
	 *                                 │
	 *                                 │
	 *                    ┌-───────────┴─────────────┐
	 *                    │                          │
	 *              branchSpot2(lifespan=10)         │
	 *                                               │
	 *                                         branchSpot3(lifespan=30)
	 * </pre>
	 */
	public static BranchSpotTree tree1()
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
	 *                             branchSpot1(lifespan=30)
	 *                                   │
	 *                                   │
	 *                                   │
	 *                      ┌-───────────┴─────────────┐
	 *                      │                          │
	 *                branchSpot2(lifespan=10)         │
	 *                                           branchSpot3(lifespan=20)
	 * </pre>
	 */
	public static BranchSpotTree tree2()
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

	/**
	 * <pre>
	 *                              branchSpot1(lifespan=1)
	 *                     ┌-─────────┴─────────────┐
	 *                     │                        │
	 *                   branchSpot2(lifespan=1)  branchSpot3(lifespan=1)
	 *          ┌-─────────┴─────────────┐
	 *          │                        │
	 *        branchSpot4(lifespan=1)  branchSpot5(lifespan=100)
	 * </pre>
	 */
	public static BranchSpotTree tree3()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 1, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 1, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 2, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 1, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 2, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex();
		spot7.init( 2, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex();
		spot8.init( 3, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex();
		spot9.init( 2, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex();
		spot10.init( 102, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 200 );
	}

	/**
	 * <pre>
	 *                            branchSpot1(lifespan=1)
	 *                   ┌-─────────┴──────────────┐
	 *                   │                         │
	 *                 branchSpot2(lifespan=100) branchSpot3(lifespan=1)
	 *        ┌-─────────┴─────────────┐
	 *        │                        │
	 *      branchSpot4(lifespan=1)  branchSpot5(lifespan=1)
	 * </pre>
	 */
	public static BranchSpotTree tree4()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 1, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 1, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 2, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 1, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 101, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex();
		spot7.init( 102, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex();
		spot8.init( 103, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex();
		spot9.init( 102, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex();
		spot10.init( 103, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 200 );
	}

	/**
	 * <pre>
	 *                      branchSpot1(lifespan=13)
	 *             ┌-─────────┴──────────────┐
	 *             │                         │
	 *           branchSpot2(lifespan=203) branchSpot3(lifespan=203)
	 * </pre>
	 */
	public static BranchSpotTree tree5()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 13, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 13, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 216, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 13, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 216, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 300 );
	}

	/**
	 * <pre>
	 *                      branchSpot1(lifespan=12)
	 *             ┌-─────────┴──────────────┐
	 *             │                         │
	 *           branchSpot2(lifespan=227) branchSpot3(lifespan=227)
	 * </pre>
	 */
	public static BranchSpotTree tree6()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 12, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 12, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 239, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 12, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 239, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 300 );
	}

	/**
	 * <pre>
	 *                       branchSpot1(lifespan=12)
	 *              ┌-─────────┴───────────────┐
	 *              │                          │
	 *            branchSpot2(lifespan=227)  branchSpot3(lifespan=227)
	 *                            ┌-───────────┴─────────────┐
	 *                          branchSpot4(lifespan=10)   branchSpot5(lifespan=10)
	 * </pre>
	 */
	public static BranchSpotTree tree7()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 12, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 12, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 239, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 12, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 239, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex();
		spot7.init( 239, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex();
		spot8.init( 249, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex();
		spot9.init( 239, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex();
		spot10.init( 249, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 300 );
	}

	/**
	 * <pre>
	 *                       branchSpot1(lifespan=3)
	 *              ┌-─────────┴─────────────────────────────────────────┐
	 *              │                                                    │
	 *            branchSpot2(lifespan=8)                     branchSpot3(lifespan=8)
	 *  ┌-───────────┴─────────────┐                        ┌-───────────┴─────────────┐
	 * branchSpot4(lifespan=4)   branchSpot5(lifespan=4)  branchSpot6(lifespan=1)   branchSpot7(lifespan=2)
	 *
	 * </pre>
	 */
	public static BranchSpotTree tree8()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 3, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 3, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 11, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 3, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 11, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex();
		spot7.init( 11, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex();
		spot8.init( 15, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex();
		spot9.init( 11, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex();
		spot10.init( 15, new double[ 3 ], 0 );

		Spot spot11 = modelGraph.addVertex();
		spot11.init( 11, new double[ 3 ], 0 );
		Spot spot12 = modelGraph.addVertex();
		spot12.init( 12, new double[ 3 ], 0 );
		Spot spot13 = modelGraph.addVertex();
		spot13.init( 11, new double[ 3 ], 0 );
		Spot spot14 = modelGraph.addVertex();
		spot14.init( 13, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelGraph.addEdge( spot4, spot11 );
		modelGraph.addEdge( spot4, spot13 );
		modelGraph.addEdge( spot11, spot12 );
		modelGraph.addEdge( spot13, spot14 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 300 );
	}

	/**
	 * <pre>
	 *                       branchSpot1(lifespan=3)
	 *              ┌-─────────┴─────────────────────────────────────────┐
	 *              │                                                    │
	 *            branchSpot2(lifespan=8)                     branchSpot3(lifespan=8)
	 *  ┌-───────────┴─────────────┐                        ┌-───────────┴─────────────┐
	 * branchSpot4(lifespan=5)   branchSpot5(lifespan=4)  branchSpot6(lifespan=1)   branchSpot7(lifespan=2)
	 *
	 * </pre>
	 */
	public static BranchSpotTree tree9()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		final ModelBranchGraph modelBranchGraph = model.getBranchGraph();

		Spot spot1 = modelGraph.addVertex();
		spot1.init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex();
		spot2.init( 3, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex();
		spot3.init( 3, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex();
		spot4.init( 11, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex();
		spot5.init( 3, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex();
		spot6.init( 11, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex();
		spot7.init( 11, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex();
		spot8.init( 15, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex();
		spot9.init( 11, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex();
		spot10.init( 16, new double[ 3 ], 0 );

		Spot spot11 = modelGraph.addVertex();
		spot11.init( 11, new double[ 3 ], 0 );
		Spot spot12 = modelGraph.addVertex();
		spot12.init( 12, new double[ 3 ], 0 );
		Spot spot13 = modelGraph.addVertex();
		spot13.init( 11, new double[ 3 ], 0 );
		Spot spot14 = modelGraph.addVertex();
		spot14.init( 13, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );

		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );

		modelGraph.addEdge( spot4, spot11 );
		modelGraph.addEdge( spot4, spot13 );
		modelGraph.addEdge( spot11, spot12 );
		modelGraph.addEdge( spot13, spot14 );

		modelBranchGraph.graphRebuilt();

		BranchSpot branchSpot = modelBranchGraph.getBranchVertex( spot1, modelBranchGraph.vertexRef() );

		return new BranchSpotTree( branchSpot, 300 );
	}
}
