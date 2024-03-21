/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.feature.branch.movement.relative;

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
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Compute relative movement in relation to nearest neighbors
 * <p>
 * The average relative movement in relation to neighbors of the spots of the branch is computed. The average relative movement is expressed by a normalized direction vector in x, y, and z direction, and the norm of the movement.
 *
 * <br>
 * @author Stefan Hahmann
 */
public class BranchRelativeMovementFeature implements Feature< BranchSpot >, ValueIsSetEvaluator< BranchSpot >
{
	public static final String KEY = "Branch relative movement";

	private static final String HELP_STRING =
			"Computes the average speed of a branch relative to its n nearest neighbours in the x, y, and z direction, and its norm.";

	private static final String PROJECTION_NAME_TEMPLATE = "Average speed%s relative to %d nearest neighbors";

	private final Map< FeatureProjectionKey, FeatureProjection< BranchSpot > > projectionMap;

	final DoublePropertyMap< BranchSpot > xMap;

	final DoublePropertyMap< BranchSpot > yMap;

	final DoublePropertyMap< BranchSpot > zMap;

	final DoublePropertyMap< BranchSpot > normMap;

	final String lengthUnits;

	public final RelativeMovementFeatureSettings settings;

	public static final BranchRelativeMovementFeatureSpec GENERIC_SPEC = new BranchRelativeMovementFeatureSpec();

	private final BranchRelativeMovementFeatureSpec adaptedSpec;

	public String getProjectionName( final String name )
	{
		return String.format( PROJECTION_NAME_TEMPLATE, name, settings.numberOfNeighbors );
	}

	@Plugin( type = FeatureSpec.class )
	public static class BranchRelativeMovementFeatureSpec extends FeatureSpec< BranchRelativeMovementFeature, BranchSpot >
	{
		public BranchRelativeMovementFeatureSpec()
		{
			super( KEY, HELP_STRING, BranchRelativeMovementFeature.class, BranchSpot.class, Multiplicity.SINGLE );
		}

		public BranchRelativeMovementFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, BranchRelativeMovementFeature.class, BranchSpot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	public BranchRelativeMovementFeature( final DoublePropertyMap< BranchSpot > xMap, final DoublePropertyMap< BranchSpot > yMap,
			final DoublePropertyMap< BranchSpot > zMap, final DoublePropertyMap< BranchSpot > normMap, final String lengthUnits,
			final RelativeMovementFeatureSettings settings )
	{
		this.xMap = xMap;
		this.yMap = yMap;
		this.zMap = zMap;
		this.normMap = normMap;
		this.lengthUnits = lengthUnits;
		this.settings = settings;
		FeatureProjectionSpec projectionSpecX = new FeatureProjectionSpec( getProjectionName( " x-component" ), Dimension.NONE );
		FeatureProjectionSpec projectionSpecY = new FeatureProjectionSpec( getProjectionName( " y-component" ), Dimension.NONE );
		FeatureProjectionSpec projectionSpecZ = new FeatureProjectionSpec( getProjectionName( " z-component" ), Dimension.NONE );
		FeatureProjectionSpec projectionSpecNorm = new FeatureProjectionSpec( getProjectionName( "" ), Dimension.LENGTH );
		this.adaptedSpec = new BranchRelativeMovementFeatureSpec( projectionSpecX, projectionSpecY, projectionSpecZ, projectionSpecNorm );
		this.projectionMap = new LinkedHashMap<>( 4 );

		final FeatureProjectionKey keyX = key( projectionSpecX );
		final FeatureProjectionKey keyY = key( projectionSpecY );
		final FeatureProjectionKey keyZ = key( projectionSpecZ );
		final FeatureProjectionKey keyNorm = key( projectionSpecNorm );

		projectionMap.put( keyX, FeatureProjections.project( keyX, xMap, lengthUnits ) );
		projectionMap.put( keyY, FeatureProjections.project( keyY, yMap, lengthUnits ) );
		projectionMap.put( keyZ, FeatureProjections.project( keyZ, zMap, lengthUnits ) );
		projectionMap.put( keyNorm, FeatureProjections.project( keyNorm, normMap, lengthUnits ) );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public BranchRelativeMovementFeatureSpec getSpec()
	{
		return adaptedSpec;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		xMap.remove( branchSpot );
		yMap.remove( branchSpot );
		zMap.remove( branchSpot );
		normMap.remove( branchSpot );
	}

	@Override
	public boolean valueIsSet( final BranchSpot branchSpot )
	{
		return xMap.isSet( branchSpot ) && yMap.isSet( branchSpot ) && zMap.isSet( branchSpot ) && normMap.isSet( branchSpot );
	}
}
