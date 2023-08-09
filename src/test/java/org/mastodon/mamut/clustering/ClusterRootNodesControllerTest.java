package org.mastodon.mamut.clustering;

import org.junit.Test;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;

import java.util.Collection;
import java.util.List;


import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClusterRootNodesControllerTest
{
	@Test
	public void testCreateTagSet()
	{
		final Model model = new Model();

		final ModelGraph modelGraph = model.getGraph();

		addLineageTree1( modelGraph );
		addLineageTree2( modelGraph );
		addLineageTree3( modelGraph );
		addLineageTree4( modelGraph );
		addLineageTree5( modelGraph );
		addEmptyTree( modelGraph );

		ClusterRootNodesController controller = new ClusterRootNodesController( model );
		controller.setParams(
				new ClusterRootNodesController.InputParams( CropCriteria.TIMEPOINT, 0, 100, 1 ),
				new ClusterRootNodesController.ComputeParams( SimilarityMeasure.NORMALIZED_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 3 )
		);
		controller.createTagSet();

		List< TagSetStructure.TagSet > tagSets = model.getTagSetModel().getTagSetStructure().getTagSets();
		TagSetStructure.TagSet tagSet0 = model.getTagSetModel().getTagSetStructure().getTagSets().get( 0 );
		List< TagSetStructure.Tag > tags = tagSet0.getTags();
		TagSetStructure.Tag tag0 = tags.get( 0 );
		TagSetStructure.Tag tag1 = tags.get( 1 );
		TagSetStructure.Tag tag2 = tags.get( 2 );

		Collection< Spot > tag0Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag0 );
		Collection< Spot > tag1Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag1 );
		Collection< Spot > tag2Spots = model.getTagSetModel().getVertexTags().getTaggedWith( tag2 );

		assertNotNull( controller.getClassification() );
		assertTrue( controller.isValidParams() );
		assertEquals( 1, tagSets.size() );
		assertEquals( 3, tags.size() );
		assertEquals( "Class 1", tag0.label() );
		assertEquals( "Class 2", tag1.label() );
		assertEquals( "Class 3", tag2.label() );
		assertEquals( 9, tag0Spots.size() );
		assertEquals( 14, tag1Spots.size() );
		assertEquals( 12, tag2Spots.size() );
	}

	@Test
	public void testGetFeedback()
	{
		Model model = new Model();
		ClusterRootNodesController controller = new ClusterRootNodesController( model );
		controller.setParams(
				new ClusterRootNodesController.InputParams( CropCriteria.TIMEPOINT, 1, 0, 1 ),
				new ClusterRootNodesController.ComputeParams( SimilarityMeasure.NORMALIZED_DIFFERENCE, ClusteringMethod.AVERAGE_LINKAGE, 1 )
		);
		assertEquals( 2, controller.getFeedback().size() );
		assertFalse( controller.isValidParams() );
	}

	/**
	 * <pre>
	 *                             branchSpot1(lifespan=20)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  branchSpot2(lifespan=10)    branchSpot3(lifespan=30)
	 * </pre>
	 */
	private static void addLineageTree1( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 20, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 20, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 20, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 50, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	/**
	 * <pre>
	 *                               branchSpot1(lifespan=30)
	 *                      ┌-─────────┴─────────────┐
	 *                      │                        │
	 *                    branchSpot2(lifespan=10) branchSpot3(lifespan=20)
	 * </pre>
	 */
	public static void addLineageTree2( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 40, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 30, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 50, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
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
	private static void addLineageTree3( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 1, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex().init( 2, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex().init( 102, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
		modelGraph.addEdge( spot6, spot7 );
		modelGraph.addEdge( spot6, spot9 );
		modelGraph.addEdge( spot7, spot8 );
		modelGraph.addEdge( spot9, spot10 );
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
	private static void addLineageTree4( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 3, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot7 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot8 = modelGraph.addVertex().init( 15, new double[ 3 ], 0 );
		Spot spot9 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot10 = modelGraph.addVertex().init( 15, new double[ 3 ], 0 );

		Spot spot11 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot12 = modelGraph.addVertex().init( 12, new double[ 3 ], 0 );
		Spot spot13 = modelGraph.addVertex().init( 11, new double[ 3 ], 0 );
		Spot spot14 = modelGraph.addVertex().init( 13, new double[ 3 ], 0 );

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
	}

	/**
	 * <pre>
	 *                             branchSpot1(lifespan=20)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  branchSpot2(lifespan=10)    branchSpot3(lifespan=30)
	 * </pre>
	 */
	private static void addLineageTree5( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 101, new double[ 3 ], 0 );
		Spot spot2 = modelGraph.addVertex().init( 121, new double[ 3 ], 0 );
		Spot spot3 = modelGraph.addVertex().init( 121, new double[ 3 ], 0 );
		Spot spot4 = modelGraph.addVertex().init( 131, new double[ 3 ], 0 );
		Spot spot5 = modelGraph.addVertex().init( 121, new double[ 3 ], 0 );
		Spot spot6 = modelGraph.addVertex().init( 151, new double[ 3 ], 0 );

		modelGraph.addEdge( spot1, spot2 );
		modelGraph.addEdge( spot2, spot3 );
		modelGraph.addEdge( spot2, spot5 );
		modelGraph.addEdge( spot3, spot4 );
		modelGraph.addEdge( spot5, spot6 );
	}

	private static void addEmptyTree( final ModelGraph modelGraph )
	{
		Spot spot1 = modelGraph.addVertex().init( 0, new double[ 3 ], 0 );
	}

}
