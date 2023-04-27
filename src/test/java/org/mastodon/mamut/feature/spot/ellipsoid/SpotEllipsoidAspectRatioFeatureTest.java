package org.mastodon.mamut.feature.spot.ellipsoid;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.FeatureComputerTestUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpotEllipsoidAspectRatioFeatureTest extends AbstractEllipsoidFeatureTest
{
	protected Feature< Spot > ellipsoidAspectRatioFeature;

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			ellipsoidAspectRatioFeature = FeatureComputerTestUtils.getSpotFeature( context, model,
					SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC );
		}
	}

	@Test
	public void testFeatureComputation()
	{
		// check that the features are computed correctly
		FeatureProjection< Spot > shortToMiddleProjection =
				getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC );
		assertEquals( expectedShortAxis / expectedMiddleAxis, shortToMiddleProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > shortToLongProjection =
				getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_LONG_SPEC );
		assertEquals( expectedShortAxis / expectedLongAxis, shortToLongProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > middleToLongProjection =
				getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_MIDDLE_TO_LONG_SPEC );
		assertEquals( expectedMiddleAxis / expectedLongAxis, middleToLongProjection.value( spot ), 0.00001d );
	}

	@Test
	public void testFeatureSerialization()
	{
		try (Context context = new Context())
		{
			SpotEllipsoidAspectRatiosFeature ellipsoidAspectRatiosFeatureReloaded = null;
			try
			{
				ellipsoidAspectRatiosFeatureReloaded = ( SpotEllipsoidAspectRatiosFeature ) FeatureSerializerTestUtils
						.saveAndReload( context, model, ellipsoidAspectRatioFeature );
			}
			catch ( IOException e )
			{
				fail( "Could not save and reload feature: " + e.getMessage() );

			}
			// check that the feature has correct values after saving and reloading
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( ellipsoidAspectRatioFeature,
					ellipsoidAspectRatiosFeatureReloaded, spot ) );
		}
	}

	@Test
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double
				.isNaN( getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC )
						.value( spot ) ) );
		assertFalse( Double
				.isNaN( getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_LONG_SPEC )
						.value( spot ) ) );
		assertFalse( Double
				.isNaN( getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_MIDDLE_TO_LONG_SPEC )
						.value( spot ) ) );

		// invalidate feature
		ellipsoidAspectRatioFeature.invalidate( spot );

		// test, if features are NaN after invalidation
		assertTrue( Double
				.isNaN( getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC )
						.value( spot ) ) );
		assertTrue( Double
				.isNaN( getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_LONG_SPEC )
						.value( spot ) ) );
		assertTrue( Double
				.isNaN( getProjection( ellipsoidAspectRatioFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_MIDDLE_TO_LONG_SPEC )
						.value( spot ) ) );
	}
}
