package org.mastodon.mamut.feature.branch.movement.relative;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph3;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementController;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementFeatureSettings;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BranchRelativeMovementFeatureTest extends AbstractFeatureTest< BranchSpot >
{
	private BranchRelativeMovementFeature branchRelativeMovementFeature;

	private final ExampleGraph3 graph3 = new ExampleGraph3();

	private final RelativeMovementFeatureSettings settings = new RelativeMovementFeatureSettings( 2 );

	private FeatureProjectionSpec specX;

	private FeatureProjectionSpec specY;

	private FeatureProjectionSpec specZ;

	private FeatureProjectionSpec specNorm;

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			RelativeMovementController relativeMovementController = new RelativeMovementController( graph3.getModel() );
			relativeMovementController.computeRelativeMovement( true, settings, context );
			branchRelativeMovementFeature =
					FeatureUtils.getFeature( graph3.getModel(), BranchRelativeMovementFeature.BranchRelativeMovementFeatureSpec.class );
			assertNotNull( branchRelativeMovementFeature );
			specX = new FeatureProjectionSpec( branchRelativeMovementFeature.getProjectionName( "x" ), Dimension.NONE );
			specY = new FeatureProjectionSpec( branchRelativeMovementFeature.getProjectionName( "y" ), Dimension.NONE );
			specZ = new FeatureProjectionSpec( branchRelativeMovementFeature.getProjectionName( "z" ), Dimension.NONE );
			specNorm = new FeatureProjectionSpec( branchRelativeMovementFeature.getProjectionName( "norm" ), Dimension.LENGTH );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( branchRelativeMovementFeature );
		double actualX = getProjection( branchRelativeMovementFeature, specX ).value( graph3.branchSpotA );
		double actualY = getProjection( branchRelativeMovementFeature, specY ).value( graph3.branchSpotA );
		double actualZ = getProjection( branchRelativeMovementFeature, specZ ).value( graph3.branchSpotA );
		double actualNorm = getProjection( branchRelativeMovementFeature, specNorm ).value( graph3.branchSpotA );

		double[] expectedDirection = new double[] { 0d, 1d, 0d };
		double expectedNorm = 2d;

		assertEquals( expectedDirection[ 0 ], actualX, 0d );
		assertEquals( expectedDirection[ 1 ], actualY, 0d );
		assertEquals( expectedDirection[ 2 ], actualZ, 0d );
		assertEquals( expectedNorm, actualNorm, 0d );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		BranchRelativeMovementFeature featureReloaded;
		try (Context context = new Context())
		{
			featureReloaded = ( BranchRelativeMovementFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph3.getModel(),
					branchRelativeMovementFeature );
		}
		assertNotNull( featureReloaded );
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchRelativeMovementFeature, featureReloaded,
				Collections.singleton( graph3.branchSpotA ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN( getProjection( branchRelativeMovementFeature, specX ).value( graph3.branchSpotA ) ) );
		assertFalse( Double.isNaN( getProjection( branchRelativeMovementFeature, specY ).value( graph3.branchSpotA ) ) );
		assertFalse( Double.isNaN( getProjection( branchRelativeMovementFeature, specZ ).value( graph3.branchSpotA ) ) );
		assertFalse( Double.isNaN( getProjection( branchRelativeMovementFeature, specNorm ).value( graph3.branchSpotA ) ) );

		// invalidate feature
		branchRelativeMovementFeature.invalidate( graph3.branchSpotA );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( branchRelativeMovementFeature, specX ).value( graph3.branchSpotA ) ) );
		assertTrue( Double.isNaN( getProjection( branchRelativeMovementFeature, specY ).value( graph3.branchSpotA ) ) );
		assertTrue( Double.isNaN( getProjection( branchRelativeMovementFeature, specZ ).value( graph3.branchSpotA ) ) );
		assertTrue( Double.isNaN( getProjection( branchRelativeMovementFeature, specNorm ).value( graph3.branchSpotA ) ) );
	}
}
