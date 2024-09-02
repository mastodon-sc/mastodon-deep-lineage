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
package org.mastodon.mamut.feature.branch.dimensionalityreduction.umap;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.branch.BranchFeatureSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * De-/serializes {@link BranchUmapFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class BranchUmapFeatureSerializer implements BranchFeatureSerializer< BranchUmapFeature, BranchSpot, Spot >

{
	@Override
	public FeatureSpec< BranchUmapFeature, BranchSpot > getFeatureSpec()
	{
		return BranchUmapFeature.GENERIC_SPEC;
	}

	@Override
	public void serialize( final BranchUmapFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos,
			final ModelBranchGraph branchGraph, final ModelGraph graph ) throws IOException
	{
		oos.writeInt( feature.getUmapOutputMaps().size() );
		for ( DoublePropertyMap< BranchSpot > umapOutput : feature.getUmapOutputMaps() )
		{
			final DoublePropertyMap< Spot > spotMap =
					BranchFeatureSerializer.branchSpotMapToMap( umapOutput, branchGraph, graph );
			final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( spotMap );
			propertyMapSerializer.writePropertyMap( idmap, oos );
		}
	}

	@Override
	public BranchUmapFeature deserialize( final FileIdToObjectMap< Spot > idmap, final ObjectInputStream ois,
			final ModelBranchGraph branchGraph, final ModelGraph graph ) throws ClassNotFoundException, IOException
	{
		int numDimensions = ois.readInt();
		List< DoublePropertyMap< BranchSpot > > umapOutputs = new ArrayList<>( numDimensions );
		for ( int i = 0; i < numDimensions; i++ )
		{
			final DoublePropertyMap< Spot > spotMap = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
			DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( spotMap );
			serializer.readPropertyMap( idmap, ois );

			DoublePropertyMap< BranchSpot > umapOutput = BranchFeatureSerializer.mapToBranchSpotMap( spotMap, branchGraph );
			umapOutputs.add( umapOutput );
		}
		return new BranchUmapFeature( umapOutputs );
	}
}
