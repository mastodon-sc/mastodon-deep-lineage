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

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
@Plugin( type = FeatureSerializer.class )
public class SpotEllipsoidFeature implements Feature< Spot >, FeatureSerializer< SpotEllipsoidFeature, Spot >
{

	public static final String KEY = "Spot ellipsoid parameters";

	private static final String HELP_STRING =
			"Computes spot ellipsoid parameters, i.e. the 3 semi axes and the volume. ";

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	public static final FeatureProjectionSpec AXIS_A_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Semi-axis a", Dimension.LENGTH );

	public static final FeatureProjectionSpec AXIS_B_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Semi-axis b", Dimension.LENGTH );

	public static final FeatureProjectionSpec AXIS_C_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Semi-axis c", Dimension.LENGTH );

	public static final FeatureProjectionSpec VOLUME_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Volume", Dimension.NONE );

	final DoublePropertyMap< Spot > semiAxisA;

	final DoublePropertyMap< Spot > semiAxisB;

	final DoublePropertyMap< Spot > semiAxisC;

	final DoublePropertyMap< Spot > volume;

	@Plugin( type = FeatureSpec.class )
	public static class SpotEllipsoidFeatureSpec extends FeatureSpec< SpotEllipsoidFeature, Spot >
	{
		public SpotEllipsoidFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotEllipsoidFeature.class, Spot.class, Multiplicity.SINGLE,
					AXIS_A_PROJECTION_SPEC, AXIS_B_PROJECTION_SPEC, AXIS_C_PROJECTION_SPEC, VOLUME_PROJECTION_SPEC );
		}
	}

	public static final SpotEllipsoidFeatureSpec SPOT_ELLIPSOID_FEATURE_SPEC = new SpotEllipsoidFeatureSpec();

	SpotEllipsoidFeature(
			final DoublePropertyMap< Spot > semiAxisA, final DoublePropertyMap< Spot > semiAxisB,
			final DoublePropertyMap< Spot > semiAxisC, final DoublePropertyMap< Spot > volume )
	{
		this.semiAxisA = semiAxisA;
		this.semiAxisB = semiAxisB;
		this.semiAxisC = semiAxisC;
		this.volume = volume;
		this.projectionMap = new LinkedHashMap<>( 4 );
		{
			final FeatureProjectionKey featureProjectionKeyAxisA = key( AXIS_A_PROJECTION_SPEC );
			final FeatureProjectionKey featureProjectionKeyAxisB = key( AXIS_B_PROJECTION_SPEC );
			final FeatureProjectionKey featureProjectionKeyAxisC = key( AXIS_C_PROJECTION_SPEC );
			final FeatureProjectionKey featureProjectionKeyVolume = key( VOLUME_PROJECTION_SPEC );

			projectionMap.put( featureProjectionKeyAxisA,
					FeatureProjections.project( featureProjectionKeyAxisA, semiAxisA, Dimension.NONE_UNITS ) );
			projectionMap.put( featureProjectionKeyAxisB,
					FeatureProjections.project( featureProjectionKeyAxisB, semiAxisB, Dimension.NONE_UNITS ) );
			projectionMap.put( featureProjectionKeyAxisC,
					FeatureProjections.project( featureProjectionKeyAxisC, semiAxisC, Dimension.NONE_UNITS ) );
			projectionMap.put( featureProjectionKeyVolume,
					FeatureProjections.project( featureProjectionKeyVolume, volume, Dimension.NONE_UNITS ) );
		}
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
		semiAxisA.remove( spot );
		semiAxisB.remove( spot );
		semiAxisC.remove( spot );
		volume.remove( spot );
	}

	@Override
	public FeatureSpec< SpotEllipsoidFeature, Spot > getFeatureSpec()
	{
		return SPOT_ELLIPSOID_FEATURE_SPEC;
	}

	@Override
	public void serialize( SpotEllipsoidFeature feature, ObjectToFileIdMap< Spot > idMap,
			ObjectOutputStream objectOutputStream )
			throws IOException
	{
		final DoublePropertyMapSerializer< Spot > semiAxisAPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.semiAxisA );
		final DoublePropertyMapSerializer< Spot > semiAxisBPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.semiAxisB );
		final DoublePropertyMapSerializer< Spot > semiAxisCPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.semiAxisC );
		final DoublePropertyMapSerializer< Spot > volumePropertySerializer =
				new DoublePropertyMapSerializer<>( feature.volume );

		semiAxisAPropertySerializer.writePropertyMap( idMap, objectOutputStream );
		semiAxisBPropertySerializer.writePropertyMap( idMap, objectOutputStream );
		semiAxisCPropertySerializer.writePropertyMap( idMap, objectOutputStream );
		volumePropertySerializer.writePropertyMap( idMap, objectOutputStream );
	}

	@Override
	public SpotEllipsoidFeature deserialize( FileIdToObjectMap< Spot > idMap, RefCollection< Spot > pool,
			ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > semiAxisAMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > semiAxisBMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > semiAxisCMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > volumeMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( semiAxisAMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( semiAxisBMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( semiAxisCMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( volumeMap ).readPropertyMap( idMap, objectInputStream );

		return new SpotEllipsoidFeature( semiAxisAMap, semiAxisBMap, semiAxisCMap, volumeMap );
	}
}
