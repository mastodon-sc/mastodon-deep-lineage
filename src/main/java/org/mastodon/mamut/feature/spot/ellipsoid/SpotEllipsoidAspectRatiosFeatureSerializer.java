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

import static org.mastodon.mamut.feature.spot.ellipsoid.SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC;

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
 * De-/serializes {@link SpotEllipsoidAspectRatiosFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class SpotEllipsoidAspectRatiosFeatureSerializer implements FeatureSerializer< SpotEllipsoidAspectRatiosFeature, Spot >
{
	@Override
	public FeatureSpec< SpotEllipsoidAspectRatiosFeature, Spot > getFeatureSpec()
	{
		return SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC;
	}

	@Override
	public void serialize( SpotEllipsoidAspectRatiosFeature feature, ObjectToFileIdMap< Spot > objectToFileIdMap,
			ObjectOutputStream objectOutputStream ) throws IOException
	{
		final DoublePropertyMapSerializer< Spot > shortToMiddleSerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioShortToMiddle );
		final DoublePropertyMapSerializer< Spot > shortToLongSerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioShortToLong );
		final DoublePropertyMapSerializer< Spot > middleToLongSerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioMiddleToLong );

		shortToMiddleSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
		shortToLongSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
		middleToLongSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
	}

	@Override
	public SpotEllipsoidAspectRatiosFeature deserialize( FileIdToObjectMap< Spot > fileIdToObjectMap,
			RefCollection< Spot > pool, ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > shortToMiddleMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > shortToLongMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > middleToLongMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( shortToMiddleMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );
		new DoublePropertyMapSerializer<>( shortToLongMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );
		new DoublePropertyMapSerializer<>( middleToLongMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );

		return new SpotEllipsoidAspectRatiosFeature( shortToMiddleMap, shortToLongMap, middleToLongMap );
	}
}
