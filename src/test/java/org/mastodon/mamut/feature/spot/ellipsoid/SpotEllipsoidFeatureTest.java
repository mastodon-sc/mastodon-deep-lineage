package org.mastodon.mamut.feature.spot.ellipsoid;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpotEllipsoidFeatureTest
{
	private final Model model = new Model();

	private final ModelGraph graph = model.getGraph();

	private final Spot spot = initEllipsoidSpot( graph );

	private static Spot initEllipsoidSpot( ModelGraph graph )
	{
		Spot spot = graph.addVertex();
		spot.init( 1, new double[] { 1, 2, 3 }, 0 );
		spot.setCovariance( new double[][] { { 6, 2, 3 }, { 2, 7, 4 }, { 3, 4, 8 } } );
		return spot;
	}

	private Feature< Spot > ellipsoidFeature;

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			ellipsoidFeature = FeatureComputerTestUtils.getFeature( context, model, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC );
		}
	}

	@Test
	public void testEllipsoidFeatureComputation()
	{
		// eigenvalues for given covariance matrix
		// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
		final double[] eigenValues = new double[] { 3.2695842d, 4.4422001d, 13.2882158d };

		// compute semi-axes from eigenvalues
		final double expectedShortAxis = Math.sqrt( eigenValues[ 0 ] );
		final double expectedMiddleAxis = Math.sqrt( eigenValues[ 1 ] );
		final double expectedLongAxis = Math.sqrt( eigenValues[ 2 ] );
		final double expectedVolume = expectedShortAxis * expectedMiddleAxis * expectedLongAxis * 4d / 3d * Math.PI;

		// check that the features are computed correctly
		FeatureProjection< Spot > shortProjection =
				getProjection( ellipsoidFeature, SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC );
		assertEquals( expectedShortAxis, shortProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > middleProjection =
				getProjection( ellipsoidFeature, SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC );
		assertEquals( expectedMiddleAxis, middleProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > longProjection =
				getProjection( ellipsoidFeature, SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC );
		assertEquals( expectedLongAxis, longProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > volumeProjection = getProjection( ellipsoidFeature, SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC );
		// volume of ellipsoid https://en.wikipedia.org/wiki/Ellipsoid#Volume
		assertEquals( expectedVolume, volumeProjection.value( spot ), 0.0001d );
	}

	@Test
	public void testEllipsoidFeatureSerialization()
	{
		try (Context context = new Context())
		{
			SpotEllipsoidFeature ellipsoidFeatureReloaded = null;
			try
			{
				ellipsoidFeatureReloaded =
						( SpotEllipsoidFeature ) FeatureSerializerTestUtils.saveAndReload( context, model, ellipsoidFeature );
			}
			catch ( IOException e )
			{
				fail( "Could not save and reload feature: " + e.getMessage() );

			}
			// check that the feature has correct values after saving and reloading
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( ellipsoidFeature, ellipsoidFeatureReloaded, spot ) );
		}
	}

	@Test
	public void testEllipsoidFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse(
				Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC ).value( spot ) ) );
		assertFalse(
				Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC ).value( spot ) ) );
		assertFalse( Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC ).value( spot ) ) );
		assertFalse( Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC ).value( spot ) ) );

		// invalidate feature
		ellipsoidFeature.invalidate( spot );

		// test, if features are NaN after invalidation
		assertTrue( Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC ).value( spot ) ) );
		assertTrue(
				Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC ).value( spot ) ) );
		assertTrue( Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC ).value( spot ) ) );
		assertTrue( Double.isNaN( getProjection( ellipsoidFeature, SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC ).value( spot ) ) );
	}

	private static FeatureProjection< Spot > getProjection( Feature< Spot > ellipsoidFeature,
			FeatureProjectionSpec featureProjectionSpec )
	{
		return ellipsoidFeature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}
}
