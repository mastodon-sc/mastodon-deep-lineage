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

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Feature that computes the aspect ratios between the three semi-axes of the ellipsoid that best fits the spot.
 * @author Stefan Hahmann
 */
public class SpotEllipsoidAspectRatiosFeature implements Feature< Spot >, ValueIsSetEvaluator< Spot >
{

	public static final String KEY = "Spot ellipsoid aspect ratios";

	private static final String HELP_STRING = "Computes the aspect ratios between the spot ellipsoid semi axes.";

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	public static final FeatureProjectionSpec ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC =
			new FeatureProjectionSpec( "Aspect ratio between short and middle semi-axes", Dimension.NONE );

	public static final FeatureProjectionSpec ASPECT_RATIO_SHORT_TO_LONG_SPEC =
			new FeatureProjectionSpec( "Aspect ratio between short and long semi-axes", Dimension.NONE );

	public static final FeatureProjectionSpec ASPECT_RATIO_MIDDLE_TO_LONG_SPEC =
			new FeatureProjectionSpec( "Aspect ratio between middle and long semi-axes", Dimension.NONE );

	final DoublePropertyMap< Spot > aspectRatioShortToMiddle;

	final DoublePropertyMap< Spot > aspectRatioShortToLong;

	final DoublePropertyMap< Spot > aspectRatioMiddleToLong;

	@Override
	public boolean valueIsSet( final Spot spot )
	{
		return aspectRatioShortToMiddle.isSet( spot ) && aspectRatioShortToLong.isSet( spot ) && aspectRatioMiddleToLong.isSet( spot );
	}

	@Plugin( type = FeatureSpec.class )
	public static class SpotEllipsoidAspectRatiosFeatureSpec extends FeatureSpec< SpotEllipsoidAspectRatiosFeature, Spot >
	{
		public SpotEllipsoidAspectRatiosFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotEllipsoidAspectRatiosFeature.class, Spot.class, Multiplicity.SINGLE,
					ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC, ASPECT_RATIO_SHORT_TO_LONG_SPEC, ASPECT_RATIO_MIDDLE_TO_LONG_SPEC );
		}
	}

	public static final SpotEllipsoidAspectRatiosFeatureSpec SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC =
			new SpotEllipsoidAspectRatiosFeatureSpec();

	SpotEllipsoidAspectRatiosFeature(
			final DoublePropertyMap< Spot > aspectRatioShortToMiddle,
			final DoublePropertyMap< Spot > aspectRatioShortToLong,
			final DoublePropertyMap< Spot > aspectRatioMiddleToLong )
	{
		this.aspectRatioShortToMiddle = aspectRatioShortToMiddle;
		this.aspectRatioShortToLong = aspectRatioShortToLong;
		this.aspectRatioMiddleToLong = aspectRatioMiddleToLong;

		this.projectionMap = new LinkedHashMap<>( 3 );

		final FeatureProjectionKey keyShortToMiddle = key( ASPECT_RATIO_SHORT_TO_MIDDLE_SPEC );
		final FeatureProjectionKey keyShortToLong = key( ASPECT_RATIO_SHORT_TO_LONG_SPEC );
		final FeatureProjectionKey keyMiddleToLong = key( ASPECT_RATIO_MIDDLE_TO_LONG_SPEC );

		projectionMap.put( keyShortToMiddle,
				FeatureProjections.project( keyShortToMiddle, aspectRatioShortToMiddle, Dimension.NONE_UNITS ) );
		projectionMap.put( keyShortToLong, FeatureProjections.project( keyShortToLong, aspectRatioShortToLong, Dimension.NONE_UNITS ) );
		projectionMap.put( keyMiddleToLong, FeatureProjections.project( keyMiddleToLong, aspectRatioMiddleToLong, Dimension.NONE_UNITS ) );
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
		aspectRatioShortToMiddle.remove( spot );
		aspectRatioShortToLong.remove( spot );
		aspectRatioMiddleToLong.remove( spot );
	}
}
