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
package org.mastodon.mamut.feature.spot.movement.relative;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementFeatureSettings;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * De-/serializes {@link SpotRelativeMovementFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class SpotRelativeMovementFeatureSerializer implements FeatureSerializer< SpotRelativeMovementFeature, Spot >
{
	@Override
	public FeatureSpec< SpotRelativeMovementFeature, Spot > getFeatureSpec()
	{
		return SpotRelativeMovementFeature.GENERIC_SPEC;
	}

	@Override
	public void serialize( SpotRelativeMovementFeature feature, ObjectToFileIdMap< Spot > idMap,
			ObjectOutputStream objectOutputStream )
			throws IOException
	{
		final DoublePropertyMapSerializer< Spot > xSerializer = new DoublePropertyMapSerializer<>( feature.x );
		final DoublePropertyMapSerializer< Spot > ySerializer = new DoublePropertyMapSerializer<>( feature.y );
		final DoublePropertyMapSerializer< Spot > zSerializer = new DoublePropertyMapSerializer<>( feature.z );
		final DoublePropertyMapSerializer< Spot > normSerializer = new DoublePropertyMapSerializer<>( feature.norm );

		xSerializer.writePropertyMap( idMap, objectOutputStream );
		ySerializer.writePropertyMap( idMap, objectOutputStream );
		zSerializer.writePropertyMap( idMap, objectOutputStream );
		normSerializer.writePropertyMap( idMap, objectOutputStream );

		objectOutputStream.writeUTF( feature.lengthUnits );
		objectOutputStream.writeInt( feature.settings.numberOfNeighbors );
	}

	@Override
	public SpotRelativeMovementFeature deserialize( FileIdToObjectMap< Spot > idMap, RefCollection< Spot > pool,
			ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > xMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > yMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > zMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > normMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( xMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( yMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( zMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( normMap ).readPropertyMap( idMap, objectInputStream );

		String lengthUnits = objectInputStream.readUTF();
		int numberOfNeighbours = objectInputStream.readInt();
		return new SpotRelativeMovementFeature( xMap, yMap, zMap, normMap, lengthUnits,
				new RelativeMovementFeatureSettings( numberOfNeighbours ) );
	}
}
