/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Feature that computes the aspect ratios between the three semi-axes of the ellipsoid that best fits the spot.
 */
public class SpotEllipsoidAspectRatiosFeature
		implements Feature< Spot >
{

	public static final String KEY = "Spot ellipsoid aspect ratios";

	private static final String HELP_STRING =
			"Computes the aspect ratios between the spot ellipsoid semi axes a, b and c.";

	private final Map< FeatureProjectionKey,
			FeatureProjection< Spot > > projectionMap;

	public static final FeatureProjectionSpec ASPECT_RATIO_A_TO_B_SPEC =
			new FeatureProjectionSpec( "Aspect ratio semi-axes a/b",
					Dimension.NONE );

	public static final FeatureProjectionSpec ASPECT_RATIO_A_TO_C_SPEC =
			new FeatureProjectionSpec( "Aspect ratio semi-axes a/c",
					Dimension.NONE );

	public static final FeatureProjectionSpec ASPECT_RATIO_B_TO_C_SPEC =
			new FeatureProjectionSpec( "Aspect ratio semi-axes b/c",
					Dimension.NONE );

	final DoublePropertyMap< Spot > aspectRatioAToB;

	final DoublePropertyMap< Spot > aspectRatioAToC;

	final DoublePropertyMap< Spot > aspectRatioBToC;

	@Plugin( type = FeatureSpec.class )
	public static class SpotEllipsoidAspectRatiosFeatureSpec
			extends FeatureSpec< SpotEllipsoidAspectRatiosFeature, Spot >
	{
		public SpotEllipsoidAspectRatiosFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotEllipsoidAspectRatiosFeature.class,
					Spot.class, Multiplicity.SINGLE, ASPECT_RATIO_A_TO_B_SPEC,
					ASPECT_RATIO_A_TO_C_SPEC, ASPECT_RATIO_B_TO_C_SPEC );
		}
	}

	public final static SpotEllipsoidAspectRatiosFeatureSpec SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC =
			new SpotEllipsoidAspectRatiosFeatureSpec();

	SpotEllipsoidAspectRatiosFeature(
			final DoublePropertyMap< Spot > aspectRatioAToB,
			final DoublePropertyMap< Spot > aspectRatioAToC,
			final DoublePropertyMap< Spot > aspectRatioBToC )
	{
		this.aspectRatioAToB = aspectRatioAToB;
		this.aspectRatioAToC = aspectRatioAToC;
		this.aspectRatioBToC = aspectRatioBToC;

		this.projectionMap = new LinkedHashMap<>( 3 );
		{
			final FeatureProjectionKey featureProjectionKeyAxisA =
					key( ASPECT_RATIO_A_TO_B_SPEC );
			final FeatureProjectionKey featureProjectionKeyAxisB =
					key( ASPECT_RATIO_A_TO_C_SPEC );
			final FeatureProjectionKey featureProjectionKeyAxisC =
					key( ASPECT_RATIO_B_TO_C_SPEC );

			projectionMap.put( featureProjectionKeyAxisA,
					FeatureProjections.project( featureProjectionKeyAxisA,
							aspectRatioAToB, Dimension.NONE_UNITS ) );
			projectionMap.put( featureProjectionKeyAxisB,
					FeatureProjections.project( featureProjectionKeyAxisB,
							aspectRatioAToC, Dimension.NONE_UNITS ) );
			projectionMap.put( featureProjectionKeyAxisC,
					FeatureProjections.project( featureProjectionKeyAxisC,
							aspectRatioBToC, Dimension.NONE_UNITS ) );
		}
	}

	@Override
	public FeatureProjection< Spot > project( FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
	{
		return SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC;
	}

	@Override
	public void invalidate( Spot spot )
	{
		aspectRatioAToB.remove( spot );
		aspectRatioAToC.remove( spot );
		aspectRatioBToC.remove( spot );
	}
}
