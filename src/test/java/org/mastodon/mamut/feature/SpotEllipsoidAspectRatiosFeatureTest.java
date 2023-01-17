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

public class SpotEllipsoidAspectRatiosFeatureTest
{
	@Test
	public void testEllipsoidAspectRatios()
	{
		try (Context context = new Context())
		{
			ExampleGraph3 exampleGraph3 = new ExampleGraph3();

			// set an example covariance matrix
			double[][] covarianceMatrix =
					new double[][] { { 6, 2, 3 }, { 2, 7, 4 }, { 3, 4, 8 } };
			exampleGraph3.spot0.setCovariance( covarianceMatrix );

			// eigenvalues for given covariance matrix
			// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
			final double[] eigenValues =
					new double[] { 3.270d, 4.442d, 13.288d };

			// compute semi-axes from eigenvalues
			double semiAxisA = Math.sqrt( eigenValues[ 0 ] );
			double semiAxisB = Math.sqrt( eigenValues[ 1 ] );
			double semiAxisC = Math.sqrt( eigenValues[ 2 ] );

			// mock big data viewer with one timepoint
			SharedBigDataViewerData sharedBigDataViewerData =
					Mockito.mock( SharedBigDataViewerData.class );
			Mockito.when( sharedBigDataViewerData.getNumTimepoints() )
					.thenReturn( 1 );

			// compute features
			FeatureProjection< Spot > featureProjectionAspectRatioAToB =
					FeatureComputerTestUtils.getSpotFeatureProjection( context,
							exampleGraph3,
							SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC,
							SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_A_TO_B_SPEC,
							sharedBigDataViewerData );
			FeatureProjection< Spot > featureProjectionAspectRatioAToC =
					FeatureComputerTestUtils.getSpotFeatureProjection( context,
							exampleGraph3,
							SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC,
							SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_A_TO_C_SPEC,
							sharedBigDataViewerData );
			FeatureProjection< Spot > featureProjectionAspectRatioBToC =
					FeatureComputerTestUtils.getSpotFeatureProjection( context,
							exampleGraph3,
							SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC,
							SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_B_TO_C_SPEC,
							sharedBigDataViewerData );

			// check results
			assertEquals( semiAxisA / semiAxisB,
					featureProjectionAspectRatioAToB
							.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( semiAxisA / semiAxisC,
					featureProjectionAspectRatioAToC
							.value( exampleGraph3.spot0 ),
					0.001d );
			assertEquals( semiAxisB / semiAxisC,
					featureProjectionAspectRatioBToC
							.value( exampleGraph3.spot0 ),
					0.001d );
		}
	}
}
