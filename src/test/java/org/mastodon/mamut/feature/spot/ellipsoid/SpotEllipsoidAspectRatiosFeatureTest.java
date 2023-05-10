package org.mastodon.mamut.feature.spot.ellipsoid;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpotEllipsoidAspectRatiosFeatureTest extends AbstractEllipsoidFeatureTest
{
	protected Feature< Spot > ellipsoidAspectRatiosFeature;

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			ellipsoidAspectRatiosFeature = FeatureComputerTestUtils.getFeature( context, model,
					SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC );
		}
	}

	@Test
	public void testFeatureComputation()
	{
		// check that the features are computed correctly
		FeatureProjection< Spot > shortToMiddleProjection =
				getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC );
		assertEquals( expectedShortAxis / expectedMiddleAxis, shortToMiddleProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > shortToLongProjection =
				getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_LONG_SPEC );
		assertEquals( expectedShortAxis / expectedLongAxis, shortToLongProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > middleToLongProjection =
				getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_MIDDLE_TO_LONG_SPEC );
		assertEquals( expectedMiddleAxis / expectedLongAxis, middleToLongProjection.value( spot ), 0.00001d );
	}

	@Test
	public void testFeatureSerialization() throws IOException
	{
		SpotEllipsoidAspectRatiosFeature ellipsoidAspectRatiosFeatureReloaded;
		try (Context context = new Context())
		{
			ellipsoidAspectRatiosFeatureReloaded = ( SpotEllipsoidAspectRatiosFeature ) FeatureSerializerTestUtils.saveAndReload( context,
					model, ellipsoidAspectRatiosFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( ellipsoidAspectRatiosFeature,
				ellipsoidAspectRatiosFeatureReloaded, Collections.singleton( spot ) ) );
	}

	@Test
	public void testFeatureInvalidate()
	{
		// test, if features are not NaN before invalidation
		assertFalse( Double
				.isNaN( getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC )
						.value( spot ) ) );
		assertFalse( Double
				.isNaN( getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_LONG_SPEC )
						.value( spot ) ) );
		assertFalse( Double
				.isNaN( getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_MIDDLE_TO_LONG_SPEC )
						.value( spot ) ) );

		// invalidate feature
		ellipsoidAspectRatiosFeature.invalidate( spot );

		// test, if features are NaN after invalidation
		assertTrue( Double
				.isNaN( getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC )
						.value( spot ) ) );
		assertTrue( Double
				.isNaN( getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_SHORT_TO_LONG_SPEC )
						.value( spot ) ) );
		assertTrue( Double
				.isNaN( getProjection( ellipsoidAspectRatiosFeature, SpotEllipsoidAspectRatiosFeature.ASPECT_RATIO_MIDDLE_TO_LONG_SPEC )
						.value( spot ) ) );
	}
}
