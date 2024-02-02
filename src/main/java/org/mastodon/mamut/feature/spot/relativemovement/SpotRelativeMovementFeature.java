/*-
 * #%L
 * mastodon-deep-lineage
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
package org.mastodon.mamut.feature.spot.relativemovement;

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
 * 
 * <br>
 * @author Stefan Hahmann
 */
public class SpotRelativeMovementFeature implements Feature< Spot >, ValueIsSetEvaluator< Spot >
{
	public static final String KEY = "Spot relative movement feature";

	private static final String HELP_STRING =
			"Computes the movement of a spot relative to its n nearest neighbours in the x, y, and z direction, as well as the norm of the movement.";

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	public static final FeatureProjectionSpec X_PROJECTION_SPEC = new FeatureProjectionSpec( "Relative movement x", Dimension.LENGTH );

	public static final FeatureProjectionSpec Y_PROJECTION_SPEC = new FeatureProjectionSpec( "Relative movement y", Dimension.LENGTH );

	public static final FeatureProjectionSpec Z_PROJECTION_SPEC = new FeatureProjectionSpec( "Relative movement z", Dimension.LENGTH );

	public static final FeatureProjectionSpec NORM_PROJECTION_SPEC = new FeatureProjectionSpec( "Relative movement norm", Dimension.NONE );

	final DoublePropertyMap< Spot > x;

	final DoublePropertyMap< Spot > y;

	final DoublePropertyMap< Spot > z;

	final DoublePropertyMap< Spot > norm;

	final String lengthUnits;

	SpotRelativeMovementFeatureSettings settings = new SpotRelativeMovementFeatureSettings();

	@Plugin( type = FeatureSpec.class )
	public static class SpotRelativeMovementFeatureSpec
			extends FeatureSpec< SpotRelativeMovementFeature, Spot >
	{
		public SpotRelativeMovementFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotRelativeMovementFeature.class, Spot.class, Multiplicity.SINGLE, X_PROJECTION_SPEC,
					Y_PROJECTION_SPEC, Z_PROJECTION_SPEC, NORM_PROJECTION_SPEC );
		}
	}

	public static final SpotRelativeMovementFeatureSpec SPEC = new SpotRelativeMovementFeatureSpec();

	SpotRelativeMovementFeature( final DoublePropertyMap< Spot > x, final DoublePropertyMap< Spot > y, final DoublePropertyMap< Spot > z,
			final DoublePropertyMap< Spot > norm, final String lengthUnits, final SpotRelativeMovementFeatureSettings settings )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.norm = norm;
		this.lengthUnits = lengthUnits;
		this.settings = settings;
		this.projectionMap = new LinkedHashMap<>( 4 );

		final FeatureProjectionKey keyShortSemiAxis = key( X_PROJECTION_SPEC );
		final FeatureProjectionKey keyMiddleSemiAxis = key( Y_PROJECTION_SPEC );
		final FeatureProjectionKey keyLongSemiAxis = key( Z_PROJECTION_SPEC );
		final FeatureProjectionKey keyVolume = key( NORM_PROJECTION_SPEC );

		projectionMap.put( keyShortSemiAxis, FeatureProjections.project( keyShortSemiAxis, norm, lengthUnits ) );
		projectionMap.put( keyMiddleSemiAxis, FeatureProjections.project( keyMiddleSemiAxis, x, lengthUnits ) );
		projectionMap.put( keyLongSemiAxis, FeatureProjections.project( keyLongSemiAxis, y, lengthUnits ) );
		projectionMap.put( keyVolume, FeatureProjections.project( keyVolume, z, Dimension.NONE_UNITS ) );
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
	public SpotRelativeMovementFeatureSpec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		x.remove( spot );
		y.remove( spot );
		z.remove( spot );
		norm.remove( spot );
	}

	@Override
	public boolean valueIsSet( final Spot spot )
	{
		return x.isSet( spot ) && y.isSet( spot ) && z.isSet( spot ) && norm.isSet( spot );
	}
}
