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
package org.mastodon.mamut.feature.spot.dimensionalityreduction.umap;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
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
import java.util.ArrayList;
import java.util.List;

/**
 * De-/serializes {@link SpotUmapFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class SpotUmapFeatureSerializer implements FeatureSerializer< SpotUmapFeature, Spot >
{

	@Override
	public FeatureSpec< SpotUmapFeature, Spot > getFeatureSpec()
	{
		return SpotUmapFeature.GENERIC_SPEC;
	}

	@Override
	public void serialize( final SpotUmapFeature feature, final ObjectToFileIdMap< Spot > idMap, final ObjectOutputStream oos )
			throws IOException
	{
		oos.writeInt( feature.getUmapOutputMaps().size() );
		for ( DoublePropertyMap< Spot > umapOutput : feature.getUmapOutputMaps() )
		{
			final DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( umapOutput );
			serializer.writePropertyMap( idMap, oos );
		}
	}

	@Override
	public SpotUmapFeature deserialize( final FileIdToObjectMap< Spot > idMap, final RefCollection< Spot > pool,
			final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		int numDimensions = ois.readInt();
		List< DoublePropertyMap< Spot > > umapOutputs = new ArrayList<>( numDimensions );
		for ( int i = 0; i < numDimensions; i++ )
		{
			DoublePropertyMap< Spot > umapOutput = new DoublePropertyMap<>( pool, Double.NaN );
			DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( umapOutput );
			serializer.readPropertyMap( idMap, ois );
			umapOutputs.add( umapOutput );
		}
		return new SpotUmapFeature( umapOutputs );
	}
}
