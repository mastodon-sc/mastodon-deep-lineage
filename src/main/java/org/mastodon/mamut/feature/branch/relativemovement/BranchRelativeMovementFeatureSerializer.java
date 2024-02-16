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
package org.mastodon.mamut.feature.branch.relativemovement;

import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.branch.BranchFeatureSerializer;
import org.mastodon.mamut.feature.relativemovement.RelativeMovementFeatureSettings;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * De-/serializes {@link BranchRelativeMovementFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class BranchRelativeMovementFeatureSerializer implements BranchFeatureSerializer< BranchRelativeMovementFeature, BranchSpot, Spot >
{
	@Override
	public FeatureSpec< BranchRelativeMovementFeature, BranchSpot > getFeatureSpec()
	{
		return BranchRelativeMovementFeature.GENERIC_SPEC;
	}

	@Override
	public BranchRelativeMovementFeature deserialize( FileIdToObjectMap< Spot > idmap, ObjectInputStream ois, ModelBranchGraph branchGraph,
			ModelGraph graph ) throws ClassNotFoundException, IOException
	{

		// Read the map link -> val.
		final DoublePropertyMap< Spot > xMap = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
		final DoublePropertyMap< Spot > yMap = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
		final DoublePropertyMap< Spot > zMap = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
		final DoublePropertyMap< Spot > normMap = new DoublePropertyMap<>( graph.vertices(), Double.NaN );

		final DoublePropertyMapSerializer< Spot > serializerX = new DoublePropertyMapSerializer<>( xMap );
		final DoublePropertyMapSerializer< Spot > serializerY = new DoublePropertyMapSerializer<>( yMap );
		final DoublePropertyMapSerializer< Spot > serializerZ = new DoublePropertyMapSerializer<>( zMap );
		final DoublePropertyMapSerializer< Spot > serializerNorm = new DoublePropertyMapSerializer<>( normMap );

		serializerX.readPropertyMap( idmap, ois );
		serializerY.readPropertyMap( idmap, ois );
		serializerZ.readPropertyMap( idmap, ois );
		serializerNorm.readPropertyMap( idmap, ois );

		String lengthUnits = ois.readUTF();
		int numberOfNeighbours = ois.readInt();

		// Map to branch-link -> val.
		return new BranchRelativeMovementFeature(
				BranchFeatureSerializer.mapToBranchSpotMap( xMap, branchGraph ),
				BranchFeatureSerializer.mapToBranchSpotMap( yMap, branchGraph ),
				BranchFeatureSerializer.mapToBranchSpotMap( zMap, branchGraph ),
				BranchFeatureSerializer.mapToBranchSpotMap( normMap, branchGraph ),
				lengthUnits, new RelativeMovementFeatureSettings( numberOfNeighbours ) );
	}

	@Override
	public void serialize( BranchRelativeMovementFeature feature, ObjectToFileIdMap< Spot > idmap, ObjectOutputStream oos,
			ModelBranchGraph branchGraph, ModelGraph graph ) throws IOException
	{

		final DoublePropertyMap< Spot > xMap = BranchFeatureSerializer.branchSpotMapToMap( feature.xMap, branchGraph, graph );
		final DoublePropertyMap< Spot > yMap = BranchFeatureSerializer.branchSpotMapToMap( feature.yMap, branchGraph, graph );
		final DoublePropertyMap< Spot > zMap = BranchFeatureSerializer.branchSpotMapToMap( feature.zMap, branchGraph, graph );
		final DoublePropertyMap< Spot > normMap = BranchFeatureSerializer.branchSpotMapToMap( feature.normMap, branchGraph, graph );

		final DoublePropertyMapSerializer< Spot > serializerX = new DoublePropertyMapSerializer<>( xMap );
		final DoublePropertyMapSerializer< Spot > serializerY = new DoublePropertyMapSerializer<>( yMap );
		final DoublePropertyMapSerializer< Spot > serializerZ = new DoublePropertyMapSerializer<>( zMap );
		final DoublePropertyMapSerializer< Spot > serializerNorm = new DoublePropertyMapSerializer<>( normMap );

		serializerX.writePropertyMap( idmap, oos );
		serializerY.writePropertyMap( idmap, oos );
		serializerZ.writePropertyMap( idmap, oos );
		serializerNorm.writePropertyMap( idmap, oos );

		oos.writeUTF( feature.lengthUnits );
		oos.writeInt( feature.settings.numberOfNeighbors );
	}
}
