package org.mastodon.mamut.feature.branch.successors;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BranchNSuccessorsPredecessorsFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private Feature< BranchSpot > branchSuccessorsPredecessorsFeature;

	private final ExampleGraph2 graph = new ExampleGraph2();

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			branchSuccessorsPredecessorsFeature = FeatureComputerTestUtils.getFeature( context, graph.getModel(),
					BranchNSuccessorsPredecessorsFeature.BRANCH_N_SUCCESSORS_PREDECESSORS_FEATURE
			);
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		FeatureProjection< BranchSpot > successorsProjection =
				getProjection( branchSuccessorsPredecessorsFeature, BranchNSuccessorsPredecessorsFeature.SUCCESSORS_PROJECTION_SPEC );
		FeatureProjection< BranchSpot > predecessorsProjection =
				getProjection( branchSuccessorsPredecessorsFeature, BranchNSuccessorsPredecessorsFeature.PREDECESSORS_PROJECTION_SPEC );

		assertEquals( 4, successorsProjection.value( graph.branchSpotA ), 0 );
		assertEquals( 2, successorsProjection.value( graph.branchSpotB ), 0 );
		assertEquals( 0, successorsProjection.value( graph.branchSpotC ), 0 );
		assertEquals( 0, successorsProjection.value( graph.branchSpotD ), 0 );
		assertEquals( 0, successorsProjection.value( graph.branchSpotE ), 0 );

		assertEquals( 0, predecessorsProjection.value( graph.branchSpotA ), 0 );
		assertEquals( 1, predecessorsProjection.value( graph.branchSpotB ), 0 );
		assertEquals( 1, predecessorsProjection.value( graph.branchSpotC ), 0 );
		assertEquals( 2, predecessorsProjection.value( graph.branchSpotD ), 0 );
		assertEquals( 2, predecessorsProjection.value( graph.branchSpotE ), 0 );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchNSuccessorsPredecessorsFeature branchNSuccessorsPredecessorsFeatureReloaded;
		try (Context context = new Context())
		{
			branchNSuccessorsPredecessorsFeatureReloaded =
					( BranchNSuccessorsPredecessorsFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(),
							branchSuccessorsPredecessorsFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchSuccessorsPredecessorsFeature,
				branchNSuccessorsPredecessorsFeatureReloaded,
				Arrays.asList( graph.branchSpotA, graph.branchSpotB, graph.branchSpotC, graph.branchSpotD, graph.branchSpotE ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not -1 (i.e. default value) before invalidation
		assertNotEquals( -1, getProjection( branchSuccessorsPredecessorsFeature,
				BranchNSuccessorsPredecessorsFeature.SUCCESSORS_PROJECTION_SPEC )
						.value( graph.branchSpotA ) );
		assertNotEquals( -1, getProjection( branchSuccessorsPredecessorsFeature,
				BranchNSuccessorsPredecessorsFeature.PREDECESSORS_PROJECTION_SPEC )
						.value( graph.branchSpotA ) );

		// invalidate feature
		branchSuccessorsPredecessorsFeature.invalidate( graph.branchSpotA );

		// test, if features are -1 (i.e. default value) after invalidation
		assertEquals( -1, getProjection( branchSuccessorsPredecessorsFeature,
				BranchNSuccessorsPredecessorsFeature.SUCCESSORS_PROJECTION_SPEC )
						.value( graph.branchSpotA ),
				0d );
		assertEquals( -1, getProjection( branchSuccessorsPredecessorsFeature,
				BranchNSuccessorsPredecessorsFeature.PREDECESSORS_PROJECTION_SPEC )
						.value( graph.branchSpotA ),
				0d );
	}
}
