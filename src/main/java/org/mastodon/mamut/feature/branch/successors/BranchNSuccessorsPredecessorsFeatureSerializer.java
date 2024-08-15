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
package org.mastodon.mamut.feature.branch.successors;

import org.mastodon.feature.FeatureSpec;
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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * De-/serializes the {@link BranchNSuccessorsPredecessorsFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchNSuccessorsPredecessorsFeatureSerializer
		implements BranchFeatureSerializer< BranchNSuccessorsPredecessorsFeature, BranchSpot, Spot >
{
	@Override
	public FeatureSpec< BranchNSuccessorsPredecessorsFeature, BranchSpot > getFeatureSpec()
	{
		return BranchNSuccessorsPredecessorsFeature.BRANCH_N_SUCCESSORS_PREDECESSORS_FEATURE;
	}

	@Override
	public BranchNSuccessorsPredecessorsFeature deserialize( FileIdToObjectMap< Spot > idmap, ObjectInputStream ois,
			ModelBranchGraph branchGraph,
			ModelGraph graph ) throws ClassNotFoundException, IOException
	{
		final IntPropertyMap< Spot > mapSuccessors = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMapSerializer< Spot > serializerSuccessors = new IntPropertyMapSerializer<>( mapSuccessors );
		serializerSuccessors.readPropertyMap( idmap, ois );
		IntPropertyMap< BranchSpot > successors = BranchFeatureSerializer.mapToBranchSpotMap( mapSuccessors, branchGraph );

		// Initialize predecessors with -1.
		IntPropertyMap< BranchSpot > predecessors = new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 );
		try
		{
			final IntPropertyMap< Spot > mapPredecessors = new IntPropertyMap<>( graph.vertices(), -1 );
			final IntPropertyMapSerializer< Spot > serializerPredecessors = new IntPropertyMapSerializer<>( mapPredecessors );
			serializerPredecessors.readPropertyMap( idmap, ois );
			predecessors = BranchFeatureSerializer.mapToBranchSpotMap( mapPredecessors, branchGraph );
		}
		catch ( EOFException e )
		{
			// ignore if no predecessors were stored. the predecessors were added later.
		}

		return new BranchNSuccessorsPredecessorsFeature( successors, predecessors );
	}

	@Override
	public void serialize( BranchNSuccessorsPredecessorsFeature feature, ObjectToFileIdMap< Spot > idmap, ObjectOutputStream oos,
			ModelBranchGraph branchGraph, ModelGraph graph ) throws IOException
	{
		IntPropertyMap< BranchSpot > branchSpotMapSuccessors = feature.nSuccessors;
		IntPropertyMap< BranchSpot > branchSpotMapPredecessors = feature.nPredecessors;
		final IntPropertyMap< Spot > spotMapSuccessors =
				BranchFeatureSerializer.branchSpotMapToMap( branchSpotMapSuccessors, branchGraph, graph );
		final IntPropertyMap< Spot > spotMapPredecessors =
				BranchFeatureSerializer.branchSpotMapToMap( branchSpotMapPredecessors, branchGraph, graph );
		final IntPropertyMapSerializer< Spot > serializerSuccessors = new IntPropertyMapSerializer<>( spotMapSuccessors );
		final IntPropertyMapSerializer< Spot > serializerPredecessors = new IntPropertyMapSerializer<>( spotMapPredecessors );
		serializerSuccessors.writePropertyMap( idmap, oos );
		serializerPredecessors.writePropertyMap( idmap, oos );
	}
}
