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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpotEllipsoidFeatureTest extends AbstractEllipsoidFeatureTest
{

	private Feature< Spot > ellipsoidFeature;

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			ellipsoidFeature = FeatureComputerTestUtils.getFeature( context, model, SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC );
		}
	}

	@Override
	@Test
	public void testFeatureComputation()
	{
		double expectedVolume = expectedShortAxis * expectedMiddleAxis * expectedLongAxis * 4d / 3d * Math.PI;

		// check that the features are computed correctly
		FeatureProjection< Spot > shortProjection = getProjection( ellipsoidFeature, SpotEllipsoidFeature.SHORT_SEMI_AXIS_PROJECTION_SPEC );
		assertEquals( expectedShortAxis, shortProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > middleProjection =
				getProjection( ellipsoidFeature, SpotEllipsoidFeature.MIDDLE_SEMI_AXIS_PROJECTION_SPEC );
		assertEquals( expectedMiddleAxis, middleProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > longProjection = getProjection( ellipsoidFeature, SpotEllipsoidFeature.LONG_SEMI_AXIS_PROJECTION_SPEC );
		assertEquals( expectedLongAxis, longProjection.value( spot ), 0.00001d );
		FeatureProjection< Spot > volumeProjection = getProjection( ellipsoidFeature, SpotEllipsoidFeature.VOLUME_PROJECTION_SPEC );
		// volume of ellipsoid https://en.wikipedia.org/wiki/Ellipsoid#Volume
		assertEquals( expectedVolume, volumeProjection.value( spot ), 0.0001d );
	}

	@Override
	@Test
	public void testFeatureSerialization() throws IOException
	{
		SpotEllipsoidFeature ellipsoidFeatureReloaded;
		try (Context context = new Context())
		{
			ellipsoidFeatureReloaded =
					( SpotEllipsoidFeature ) FeatureSerializerTestUtils.saveAndReload( context, model, ellipsoidFeature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( ellipsoidFeature, ellipsoidFeatureReloaded, spot ) );
	}

	@Override
	@Test
	public void testFeatureInvalidate()
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
}
