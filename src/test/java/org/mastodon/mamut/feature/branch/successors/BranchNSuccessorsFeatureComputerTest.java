package org.mastodon.mamut.feature.branch.successors;

import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;

public class BranchNSuccessorsFeatureComputerTest
{
	@Test
	public void testComputeNumberOfSubtreeNodes1()
	{
		try (Context context = new Context())
		{
			ExampleGraph1 exampleGraph1 = new ExampleGraph1();
			FeatureProjection< BranchSpot > featureProjection =
					FeatureComputerTestUtils.getFeatureProjection( context, exampleGraph1.getModel(),
							BranchNSuccessorsFeature.BRANCH_N_SUCCESSORS_FEATURE,
							BranchNSuccessorsFeature.PROJECTION_SPEC );
			assertEquals( 0, featureProjection.value( exampleGraph1.branchSpotA ), 0 );
		}
	}

	@Test
	public void testComputeNumberOfSubtreeNodes2()
	{
		try (Context context = new Context())
		{
			ExampleGraph2 exampleGraph2 = new ExampleGraph2();
			FeatureProjection< BranchSpot > featureProjection =
					FeatureComputerTestUtils.getFeatureProjection( context, exampleGraph2.getModel(),
							BranchNSuccessorsFeature.BRANCH_N_SUCCESSORS_FEATURE,
							BranchNSuccessorsFeature.PROJECTION_SPEC );

			assertEquals( 4, featureProjection.value( exampleGraph2.branchSpotA ), 0 );
			assertEquals( 2, featureProjection.value( exampleGraph2.branchSpotB ), 0 );
			assertEquals( 0, featureProjection.value( exampleGraph2.branchSpotC ), 0 );
			assertEquals( 0, featureProjection.value( exampleGraph2.branchSpotD ), 0 );
			assertEquals( 0, featureProjection.value( exampleGraph2.branchSpotE ), 0 );
		}
	}
}
