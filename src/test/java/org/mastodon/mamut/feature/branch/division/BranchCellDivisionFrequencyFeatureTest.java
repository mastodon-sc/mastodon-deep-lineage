package org.mastodon.mamut.feature.branch.division;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.divisions.BranchCellDivisionFrequencyFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BranchCellDivisionFrequencyFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private Feature< BranchSpot > branchNormalizedCellDivisionsFeature;

	private final ExampleGraph2 graph = new ExampleGraph2();

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			branchNormalizedCellDivisionsFeature = FeatureComputerTestUtils.getFeature( context, graph.getModel(),
					BranchCellDivisionFrequencyFeature.FEATURE_SPEC );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		FeatureProjection< BranchSpot > projection =
				getProjection( branchNormalizedCellDivisionsFeature, BranchCellDivisionFrequencyFeature.PROJECTION_SPEC );

		assertEquals( 2d / 14d, projection.value( graph.branchSpotA ), 0d );
		assertEquals( 1d / 8d, projection.value( graph.branchSpotB ), 0d );
		assertEquals( 0d, projection.value( graph.branchSpotC ), 0d );
		assertEquals( 0d, projection.value( graph.branchSpotD ), 0d );
		assertEquals( 0d, projection.value( graph.branchSpotE ), 0d );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchCellDivisionFrequencyFeature branchCellDivisionFrequencyFeatureReloaded;
		try (Context context = new Context())
		{
			branchCellDivisionFrequencyFeatureReloaded =
					( BranchCellDivisionFrequencyFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(),
							branchNormalizedCellDivisionsFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchNormalizedCellDivisionsFeature,
				branchCellDivisionFrequencyFeatureReloaded,
				Collections.singleton( graph.branchSpotA ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN(
				getProjection( branchNormalizedCellDivisionsFeature, BranchCellDivisionFrequencyFeature.PROJECTION_SPEC )
						.value( graph.branchSpotA ) ) );

		// invalidate feature
		branchNormalizedCellDivisionsFeature.invalidate( graph.branchSpotA );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN(
				getProjection( branchNormalizedCellDivisionsFeature, BranchCellDivisionFrequencyFeature.PROJECTION_SPEC )
						.value( graph.branchSpotA ) ) );
	}
}
