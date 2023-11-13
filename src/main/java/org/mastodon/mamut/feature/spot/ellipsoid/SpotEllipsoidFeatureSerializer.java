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
package org.mastodon.mamut.feature.spot.ellipsoid;

import static org.mastodon.mamut.feature.spot.ellipsoid.SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes {@link SpotEllipsoidFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class SpotEllipsoidFeatureSerializer implements FeatureSerializer< SpotEllipsoidFeature, Spot >
{
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
		final DoublePropertyMapSerializer< Spot > shortAxisSerializer = new DoublePropertyMapSerializer<>( feature.shortSemiAxis );
		final DoublePropertyMapSerializer< Spot > middleAxisSerializer = new DoublePropertyMapSerializer<>( feature.middleSemiAxis );
		final DoublePropertyMapSerializer< Spot > longAxisSerializer = new DoublePropertyMapSerializer<>( feature.longSemiAxis );
		final DoublePropertyMapSerializer< Spot > volumeSerializer = new DoublePropertyMapSerializer<>( feature.volume );

		shortAxisSerializer.writePropertyMap( idMap, objectOutputStream );
		middleAxisSerializer.writePropertyMap( idMap, objectOutputStream );
		longAxisSerializer.writePropertyMap( idMap, objectOutputStream );
		volumeSerializer.writePropertyMap( idMap, objectOutputStream );
	}

	@Override
	public SpotEllipsoidFeature deserialize( FileIdToObjectMap< Spot > idMap, RefCollection< Spot > pool,
			ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > shortAxisMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > middleAxisMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > longAxisMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > volumeMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( shortAxisMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( middleAxisMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( longAxisMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( volumeMap ).readPropertyMap( idMap, objectInputStream );

		return new SpotEllipsoidFeature( shortAxisMap, middleAxisMap, longAxisMap, volumeMap );
	}
}
