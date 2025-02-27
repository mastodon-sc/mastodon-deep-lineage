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
package org.mastodon.mamut.feature.spot.movement.relative;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementFeatureSettings;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Computes the relative movement of a Spot in relation to nearest neighbors.
 * <p>
 * The feature computes the movement of a spot relative to its n nearest neighbours in the x, y, and z direction, as well as the norm of the movement.<p>
 * For each spot, the movement from the last timepoint to the current timepoint is computed and compared to the average movement of the nearest neighbors.
 * A vector representing the relative movement in x, y, and z direction and the norm of it is computed.
 *
 * @author Stefan Hahmann
 */
public class SpotRelativeMovementFeature implements Feature< Spot >, ValueIsSetEvaluator< Spot >
{
	public static final String KEY = "Spot relative movement";

	private static final String HELP_STRING =
			"Computes the movement of a spot relative to its n nearest neighbours in the x, y, and z direction, as well as the norm (i.e. distance) of the movement.";

	private static final String PROJECTION_NAME_TEMPLATE = "%s relative to %d nearest neighbors";

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	public final DoublePropertyMap< Spot > x;

	public final DoublePropertyMap< Spot > y;

	public final DoublePropertyMap< Spot > z;

	final DoublePropertyMap< Spot > norm;

	final String lengthUnits;

	public final RelativeMovementFeatureSettings settings;

	public static final SpotRelativeMovementFeatureSpec GENERIC_SPEC = new SpotRelativeMovementFeatureSpec();

	private final SpotRelativeMovementFeatureSpec adaptedSpec;

	public String getProjectionName( final String name )
	{
		return String.format( PROJECTION_NAME_TEMPLATE, name, settings.numberOfNeighbors );
	}

	@Plugin( type = FeatureSpec.class )
	public static class SpotRelativeMovementFeatureSpec extends FeatureSpec< SpotRelativeMovementFeature, Spot >
	{
		public SpotRelativeMovementFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotRelativeMovementFeature.class, Spot.class, Multiplicity.SINGLE );
		}

		public SpotRelativeMovementFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, SpotRelativeMovementFeature.class, Spot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	public SpotRelativeMovementFeature( final DoublePropertyMap< Spot > x, final DoublePropertyMap< Spot > y,
			final DoublePropertyMap< Spot > z,
			final DoublePropertyMap< Spot > norm, final String lengthUnits, final RelativeMovementFeatureSettings settings )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.norm = norm;
		this.lengthUnits = lengthUnits;
		this.settings = settings;
		FeatureProjectionSpec projectionSpecX = new FeatureProjectionSpec( getProjectionName( "X-component" ), Dimension.LENGTH );
		FeatureProjectionSpec projectionSpecY = new FeatureProjectionSpec( getProjectionName( "Y-component" ), Dimension.LENGTH );
		FeatureProjectionSpec projectionSpecZ = new FeatureProjectionSpec( getProjectionName( "Z-component" ), Dimension.LENGTH );
		FeatureProjectionSpec projectionSpecNorm = new FeatureProjectionSpec( getProjectionName( "Distance" ), Dimension.LENGTH );
		this.adaptedSpec = new SpotRelativeMovementFeatureSpec( projectionSpecX, projectionSpecY, projectionSpecZ, projectionSpecNorm );
		this.projectionMap = new LinkedHashMap<>( 4 );

		final FeatureProjectionKey keyX = key( projectionSpecX );
		final FeatureProjectionKey keyY = key( projectionSpecY );
		final FeatureProjectionKey keyZ = key( projectionSpecZ );
		final FeatureProjectionKey keyNorm = key( projectionSpecNorm );

		projectionMap.put( keyX, FeatureProjections.project( keyX, x, lengthUnits ) );
		projectionMap.put( keyY, FeatureProjections.project( keyY, y, lengthUnits ) );
		projectionMap.put( keyZ, FeatureProjections.project( keyZ, z, lengthUnits ) );
		projectionMap.put( keyNorm, FeatureProjections.project( keyNorm, norm, lengthUnits ) );
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
		return adaptedSpec;
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
