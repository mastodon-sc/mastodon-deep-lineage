package org.mastodon.mamut.feature;

import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.branch.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.exampleGraph.ExampleGraph3;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mockito.Mockito;
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

			// mock big data viewer with one timepoint
			SharedBigDataViewerData sharedBigDataViewerData = Mockito.mock( SharedBigDataViewerData.class );
			Mockito.when( sharedBigDataViewerData.getNumTimepoints() ).thenReturn( 1 );

			// compute features
			FeatureProjection< Spot > featureProjectionAxisA = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.AXIS_A_PROJECTION_SPEC, sharedBigDataViewerData );
			FeatureProjection< Spot > featureProjectionAxisB = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.AXIS_B_PROJECTION_SPEC, sharedBigDataViewerData );
			FeatureProjection< Spot > featureProjectionAxisC = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.AXIS_C_PROJECTION_SPEC, sharedBigDataViewerData );
			FeatureProjection< Spot > featureProjectionVolume = FeatureComputerTestUtils.getSpotFeatureProjection( context, exampleGraph3, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC, SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC, sharedBigDataViewerData );

			// eigenvalues for given covariance matrix
			// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
			final double[] eigenValues = new double[] { 3.270d, 4.442d, 13.288d };

			// compute semi-axes from eigenvalues
			double semiAxisA = Math.sqrt( eigenValues[ 0 ] );
			double semiAxisB = Math.sqrt( eigenValues[ 1 ] );
			double semiAxisC = Math.sqrt( eigenValues[ 2 ] );

			assertEquals( semiAxisA, featureProjectionAxisA.value( exampleGraph3.spot0 ), 0.001d );
			assertEquals( semiAxisB, featureProjectionAxisB.value( exampleGraph3.spot0 ), 0.001d );
			assertEquals( semiAxisC, featureProjectionAxisC.value( exampleGraph3.spot0 ), 0.001d );
			// volume of ellipsoid https://en.wikipedia.org/wiki/Ellipsoid#Volume
			assertEquals( semiAxisA * semiAxisB * semiAxisC * 4d / 3d * Math.PI, featureProjectionVolume.value( exampleGraph3.spot0 ), 0.01d );
		}
	}
}
