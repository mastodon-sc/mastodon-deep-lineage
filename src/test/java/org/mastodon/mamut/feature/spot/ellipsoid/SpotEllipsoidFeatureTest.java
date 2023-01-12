package org.mastodon.mamut.feature.spot.ellipsoid;

import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.branch.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.exampleGraph.ExampleGraph3;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;

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

			// compute features
			FeatureProjection< Spot > shortAxis = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3,
					SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC );
			FeatureProjection< Spot > middleAxis = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3,
					SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC );
			FeatureProjection< Spot > longAxis = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3,
					SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC );
			FeatureProjection< Spot > volume = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3,
					SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC );

			// eigenvalues for given covariance matrix
			// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
			final double[] eigenValues = new double[] { 3.270d, 4.442d, 13.288d };

			// compute semi-axes from eigenvalues
			double shortSemiAxis = Math.sqrt( eigenValues[ 0 ] );
			double middleSemiAxis = Math.sqrt( eigenValues[ 1 ] );
			double longSemiAxis = Math.sqrt( eigenValues[ 2 ] );

			assertEquals( shortSemiAxis, shortAxis.value( exampleGraph3.spot0 ), 0.001d );
			assertEquals( middleSemiAxis, middleAxis.value( exampleGraph3.spot0 ), 0.001d );
			assertEquals( longSemiAxis, longAxis.value( exampleGraph3.spot0 ), 0.001d );
			// volume of ellipsoid https://en.wikipedia.org/wiki/Ellipsoid#Volume
			assertEquals( shortSemiAxis * middleSemiAxis * longSemiAxis * 4d / 3d * Math.PI,
					volume.value( exampleGraph3.spot0 ), 0.01d );
		}
	}
}
