/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature.branch.timepoints;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.mamut.feature.branch.BranchFeatureSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class BranchTimepointsFeatureSerializer
		implements BranchFeatureSerializer< BranchTimepointsFeature, BranchSpot, Spot >
{

	@Override
	public BranchTimepointsFeature.Spec getFeatureSpec()
	{
		return BranchTimepointsFeature.SPEC;
	}

	@Override
	public BranchTimepointsFeature deserialize(
			final FileIdToObjectMap< Spot > idmap,
			final ObjectInputStream ois,
			final ModelBranchGraph branchGraph,
			final ModelGraph graph ) throws ClassNotFoundException, IOException
	{
		// Read the map link -> val.
		final IntPropertyMap< Spot > startTimepointMap = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMap< Spot > endTimepointMap = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMapSerializer< Spot > startTimepointSerializer = new IntPropertyMapSerializer<>( startTimepointMap );
		final IntPropertyMapSerializer< Spot > endTimepointSerializer = new IntPropertyMapSerializer<>( endTimepointMap );
		final String units = ois.readUTF();
		startTimepointSerializer.readPropertyMap( idmap, ois );
		endTimepointSerializer.readPropertyMap( idmap, ois );

		// Map to branch-link -> val.
		return new BranchTimepointsFeature( BranchFeatureSerializer.mapToBranchSpotMap( startTimepointMap, branchGraph ),
				BranchFeatureSerializer.mapToBranchSpotMap( endTimepointMap, branchGraph ), units );
	}

	@Override
	public void serialize(
			final BranchTimepointsFeature feature,
			final ObjectToFileIdMap< Spot > idmap,
			final ObjectOutputStream oos,
			final ModelBranchGraph branchGraph,
			final ModelGraph graph ) throws IOException
	{
		final IntPropertyMap< Spot > startTimepointMap =
				BranchFeatureSerializer.branchSpotMapToMap( feature.startTimepointsMap, branchGraph, graph );
		final IntPropertyMap< Spot > endTimepointsMap =
				BranchFeatureSerializer.branchSpotMapToMap( feature.endTimepointsMap, branchGraph, graph );
		final IntPropertyMapSerializer< Spot > startTimepointSerializer = new IntPropertyMapSerializer<>( startTimepointMap );
		final IntPropertyMapSerializer< Spot > entTimepointsSerializer = new IntPropertyMapSerializer<>( endTimepointsMap );
		oos.writeUTF( feature.lengthUnits );
		startTimepointSerializer.writePropertyMap( idmap, oos );
		entTimepointsSerializer.writePropertyMap( idmap, oos );
	}
}
