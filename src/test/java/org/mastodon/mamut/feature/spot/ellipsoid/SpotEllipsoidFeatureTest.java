/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature.spot.ellipsoid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpotEllipsoidFeatureTest extends AbstractEllipsoidFeatureTest
{

	private Feature< Spot > ellipsoidFeature;

	@BeforeEach
	void setUp()
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
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( ellipsoidFeature, ellipsoidFeatureReloaded,
				Collections.singleton( spot ) ) );
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
