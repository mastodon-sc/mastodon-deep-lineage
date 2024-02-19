package org.mastodon.mamut.feature.spot.movement.relative;

import net.imglib2.util.LinAlgHelpers;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementController;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementFeatureSettings;
import org.mastodon.mamut.feature.spot.SpotFeatureUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpotRelativeMovementFeatureTest extends AbstractFeatureTest< Spot >
{
	private SpotRelativeMovementFeature spotRelativeMovementFeature;

	private final ExampleGraph2 graph2 = new ExampleGraph2();

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
			RelativeMovementController relativeMovementController = new RelativeMovementController( graph2.getModel() );
			relativeMovementController.computeRelativeMovement( true, settings, context );
			spotRelativeMovementFeature =
					FeatureUtils.getFeature( graph2.getModel(), SpotRelativeMovementFeature.SpotRelativeMovementFeatureSpec.class );
			assertNotNull( spotRelativeMovementFeature );
			specX = new FeatureProjectionSpec( spotRelativeMovementFeature.getProjectionName( "x" ), Dimension.LENGTH );
			specY = new FeatureProjectionSpec( spotRelativeMovementFeature.getProjectionName( "y" ), Dimension.LENGTH );
			specZ = new FeatureProjectionSpec( spotRelativeMovementFeature.getProjectionName( "z" ), Dimension.LENGTH );
			specNorm = new FeatureProjectionSpec( spotRelativeMovementFeature.getProjectionName( "norm" ), Dimension.LENGTH );
		}
	}

	@Test
	@Override
	public void testFeatureComputation()
	{
		assertNotNull( spotRelativeMovementFeature );
		double actualX = getProjection( spotRelativeMovementFeature, specX ).value( graph2.spot13 );
		double actualY = getProjection( spotRelativeMovementFeature, specY ).value( graph2.spot13 );
		double actualZ = getProjection( spotRelativeMovementFeature, specZ ).value( graph2.spot13 );
		double actualNorm = getProjection( spotRelativeMovementFeature, specNorm ).value( graph2.spot13 );

		double[] expected = new double[ 3 ];
		double[] movementSpot8 = SpotFeatureUtils.spotMovement( graph2.spot8 );
		double[] movementSpot5 = SpotFeatureUtils.spotMovement( graph2.spot5 );
		LinAlgHelpers.add( movementSpot8, movementSpot5, movementSpot8 );
		LinAlgHelpers.scale( movementSpot8, 1 / 2d, movementSpot8 );
		double[] movementSpot13 = SpotFeatureUtils.spotMovement( graph2.spot13 );
		LinAlgHelpers.subtract( movementSpot13, movementSpot8, expected );

		assertEquals( expected[ 0 ], actualX, 0d );
		assertEquals( expected[ 1 ], actualY, 0d );
		assertEquals( expected[ 2 ], actualZ, 0d );
		assertEquals( LinAlgHelpers.length( expected ), actualNorm, 0d );
	}

	@Test
	@Override
	public void testFeatureSerialization() throws IOException
	{
		SpotRelativeMovementFeature spotRelativeMovementFeatureReloaded;
		try (Context context = new Context())
		{
			spotRelativeMovementFeatureReloaded = ( SpotRelativeMovementFeature ) FeatureSerializerTestUtils.saveAndReload( context,
					graph2.getModel(), spotRelativeMovementFeature );
		}
		assertNotNull( spotRelativeMovementFeatureReloaded );
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( spotRelativeMovementFeature,
				spotRelativeMovementFeatureReloaded, Collections.singleton( graph2.spot13 ) ) );
	}

	@Test
	@Override
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double.isNaN( getProjection( spotRelativeMovementFeature, specX ).value( graph2.spot13 ) ) );
		assertFalse( Double.isNaN( getProjection( spotRelativeMovementFeature, specY ).value( graph2.spot13 ) ) );
		assertFalse( Double.isNaN( getProjection( spotRelativeMovementFeature, specZ ).value( graph2.spot13 ) ) );
		assertFalse( Double.isNaN( getProjection( spotRelativeMovementFeature, specNorm ).value( graph2.spot13 ) ) );

		// invalidate feature
		spotRelativeMovementFeature.invalidate( graph2.spot13 );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( spotRelativeMovementFeature, specX ).value( graph2.spot13 ) ) );
		assertTrue( Double.isNaN( getProjection( spotRelativeMovementFeature, specY ).value( graph2.spot13 ) ) );
		assertTrue( Double.isNaN( getProjection( spotRelativeMovementFeature, specZ ).value( graph2.spot13 ) ) );
		assertTrue( Double.isNaN( getProjection( spotRelativeMovementFeature, specNorm ).value( graph2.spot13 ) ) );
	}
}
