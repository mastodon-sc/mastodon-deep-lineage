package org.mastodon.mamut.feature.spot.ellipsoid;

import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.exampleGraph.ExampleGraph3;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpotEllipsoidFeatureTest
{

	@Test
	public void testEllipsoidFeature()
	{
		try (Context context = new Context())
		{
			ExampleGraph3 exampleGraph3 = new ExampleGraph3();

			// set an example covariance matrix
			double[][] covarianceMatrix = new double[][] { { 6, 2, 3 }, { 2, 7, 4 }, { 3, 4, 8 } };
			exampleGraph3.spot0.setCovariance( covarianceMatrix );

			// compute feature
			Feature< Spot > ellipsoidFeature =
					FeatureComputerTestUtils.getSpotFeature( context, exampleGraph3, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC );

			// eigenvalues for given covariance matrix
			// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
			final double[] eigenValues = new double[] { 3.270d, 4.442d, 13.288d };

			// compute semi-axes from eigenvalues
			double expectedShortAxis = Math.sqrt( eigenValues[ 0 ] );
			double expectedMiddleAxis = Math.sqrt( eigenValues[ 1 ] );
			double expectedLongAxis = Math.sqrt( eigenValues[ 2 ] );
			double expectedVolume = expectedShortAxis * expectedMiddleAxis * expectedLongAxis * 4d / 3d * Math.PI;

			// check that the features are computed correctly
			assertEquals( expectedShortAxis,
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( expectedMiddleAxis,
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( expectedLongAxis,
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ),
					0.001d );
			// volume of ellipsoid https://en.wikipedia.org/wiki/Ellipsoid#Volume
			assertEquals( expectedVolume, ellipsoidFeature
					.project( FeatureProjectionKey.key( SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC ) ).value( exampleGraph3.spot0 ),
					0.01d );

			Model model = exampleGraph3.getModel();
			FeatureSerializerTestUtils.saveAndReload( context, exampleGraph3.getModel(), ellipsoidFeature );

			// check that the feature has correct values after saving and reloading
			SpotEllipsoidFeature ellipsoidFeatureReloaded = ( SpotEllipsoidFeature ) model.getFeatureModel()
					.getFeature( SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC );
			assertEquals( expectedShortAxis,
					ellipsoidFeatureReloaded.project( FeatureProjectionKey.key( SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( expectedMiddleAxis, ellipsoidFeatureReloaded
					.project( FeatureProjectionKey.key( SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC ) )
					.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( expectedLongAxis,
					ellipsoidFeatureReloaded.project( FeatureProjectionKey.key( SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( expectedVolume,
					ellipsoidFeatureReloaded.project( FeatureProjectionKey.key( SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ),
					0.01d );

			// test, if features are NaN after invalidation
			ellipsoidFeature.invalidate( exampleGraph3.spot0 );
			assertTrue( Double.isNaN(
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ) ) );
			assertTrue( Double.isNaN(
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ) ) );
			assertTrue( Double.isNaN(
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ) ) );
			assertTrue( Double.isNaN(
					ellipsoidFeature.project( FeatureProjectionKey.key( SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC ) )
							.value( exampleGraph3.spot0 ) ) );

		}
	}
}
