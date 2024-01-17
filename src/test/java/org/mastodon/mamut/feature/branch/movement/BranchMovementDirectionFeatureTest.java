package org.mastodon.mamut.feature.branch.movement;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BranchMovementDirectionFeatureTest extends AbstractFeatureTest< BranchSpot >
{

	private Feature< BranchSpot > branchMovementDirectionFeature;

	private final ExampleGraph1 graph = new ExampleGraph1();

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			branchMovementDirectionFeature = FeatureComputerTestUtils.getFeature( context, graph.getModel(),
					BranchMovementDirectionFeature.BRANCH_MOVEMENT_DIRECTION_FEATURE_SPEC );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		double distance = Math.sqrt( 16 + 64 + 144 );
		double expectedX = 4 / distance;
		double expectedY = 8 / distance;
		double expectedZ = 12 / distance;

		FeatureProjection< BranchSpot > xProjection =
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_X_PROJECTION_SPEC );
		FeatureProjection< BranchSpot > yProjection =
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC );
		FeatureProjection< BranchSpot > zProjection =
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Z_PROJECTION_SPEC );

		assertEquals( expectedX, xProjection.value( graph.branchSpotA ), 0.0001d );
		assertEquals( expectedY, yProjection.value( graph.branchSpotA ), 0.0001d );
		assertEquals( expectedZ, zProjection.value( graph.branchSpotA ), 0.0001d );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchMovementDirectionFeature branchMovementDirectionFeatureReloaded;
		try (Context context = new Context())
		{
			branchMovementDirectionFeatureReloaded =
					( BranchMovementDirectionFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(),
							branchMovementDirectionFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchMovementDirectionFeature,
				branchMovementDirectionFeatureReloaded,
				Collections.singleton( graph.branchSpotA ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse(
				Double.isNaN(
						getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC )
								.value( graph.branchSpotA ) ) );
		assertFalse(
				Double.isNaN(
						getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC )
								.value( graph.branchSpotA ) ) );
		assertFalse(
				Double.isNaN(
						getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Z_PROJECTION_SPEC )
								.value( graph.branchSpotA ) ) );

		// invalidate feature
		branchMovementDirectionFeature.invalidate( graph.branchSpotA );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_X_PROJECTION_SPEC ).value(
						graph.branchSpotA ) ) );
		assertTrue( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Y_PROJECTION_SPEC ).value(
						graph.branchSpotA ) ) );
		assertTrue( Double.isNaN(
				getProjection( branchMovementDirectionFeature, BranchMovementDirectionFeature.MOVEMENT_DIRECTION_Z_PROJECTION_SPEC ).value(
						graph.branchSpotA ) ) );
	}
}
