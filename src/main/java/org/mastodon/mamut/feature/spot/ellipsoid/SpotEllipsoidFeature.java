/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Feature that computes the 3 semi-axes and the volume from the covariance matrix of {@link Spot}s.
 * <p>
 * @author Stefan Hahmann
 */
public class SpotEllipsoidFeature implements Feature< Spot >
{
	public static final String KEY = "Spot ellipsoid properties";

	private static final String HELP_STRING = "Computes spot ellipsoid parameters, i.e. the 3 semi axes and the volume. ";

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	public static final FeatureProjectionSpec SHORT_SEMI_AXIS_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Short semi-axis", Dimension.LENGTH );

	public static final FeatureProjectionSpec MIDDLE_SEMI_AXIS_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Middle semi-axis", Dimension.LENGTH );

	public static final FeatureProjectionSpec LONG_SEMI_AXIS_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Long semi-axis", Dimension.LENGTH );

	public static final FeatureProjectionSpec VOLUME_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Volume", Dimension.NONE );

	final DoublePropertyMap< Spot > shortSemiAxis;

	final DoublePropertyMap< Spot > middleSemiAxis;

	final DoublePropertyMap< Spot > longSemiAxis;

	final DoublePropertyMap< Spot > volume;

	@Plugin( type = FeatureSpec.class )
	public static class SpotEllipsoidFeatureSpec extends FeatureSpec< SpotEllipsoidFeature, Spot >
	{
		public SpotEllipsoidFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotEllipsoidFeature.class, Spot.class, Multiplicity.SINGLE,
					SHORT_SEMI_AXIS_PROJECTION_SPEC, MIDDLE_SEMI_AXIS_PROJECTION_SPEC, LONG_SEMI_AXIS_PROJECTION_SPEC,
					VOLUME_PROJECTION_SPEC );
		}
	}

	public static final SpotEllipsoidFeatureSpec SPOT_ELLIPSOID_FEATURE_SPEC = new SpotEllipsoidFeatureSpec();

	SpotEllipsoidFeature( final DoublePropertyMap< Spot > shortSemiAxis, final DoublePropertyMap< Spot > middleSemiAxis,
			final DoublePropertyMap< Spot > longSemiAxis, final DoublePropertyMap< Spot > volume )
	{
		this.shortSemiAxis = shortSemiAxis;
		this.middleSemiAxis = middleSemiAxis;
		this.longSemiAxis = longSemiAxis;
		this.volume = volume;
		this.projectionMap = new LinkedHashMap<>( 4 );

		final FeatureProjectionKey keyShortSemiAxis = key( SHORT_SEMI_AXIS_PROJECTION_SPEC );
		final FeatureProjectionKey keyMiddleSemiAxis = key( MIDDLE_SEMI_AXIS_PROJECTION_SPEC );
		final FeatureProjectionKey keyLongSemiAxis = key( LONG_SEMI_AXIS_PROJECTION_SPEC );
		final FeatureProjectionKey keyVolume = key( VOLUME_PROJECTION_SPEC );

		projectionMap.put( keyShortSemiAxis, FeatureProjections.project( keyShortSemiAxis, shortSemiAxis, Dimension.NONE_UNITS ) );
		projectionMap.put( keyMiddleSemiAxis, FeatureProjections.project( keyMiddleSemiAxis, middleSemiAxis, Dimension.NONE_UNITS ) );
		projectionMap.put( keyLongSemiAxis, FeatureProjections.project( keyLongSemiAxis, longSemiAxis, Dimension.NONE_UNITS ) );
		projectionMap.put( keyVolume, FeatureProjections.project( keyVolume, volume, Dimension.NONE_UNITS ) );
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public SpotEllipsoidFeatureSpec getSpec()
	{
		return SPOT_ELLIPSOID_FEATURE_SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		shortSemiAxis.remove( spot );
		middleSemiAxis.remove( spot );
		longSemiAxis.remove( spot );
		volume.remove( spot );
	}
}
