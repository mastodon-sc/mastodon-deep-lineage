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
package org.mastodon.mamut.feature.branch;

import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class AbstractBranchSpotDoublePropertyFeatureSerializer< T extends AbstractDoublePropertyFeature< BranchSpot > >
		implements BranchFeatureSerializer< T, BranchSpot, Spot >
{

	protected abstract T createFeature( DoublePropertyMap< BranchSpot > map );

	protected abstract DoublePropertyMap< BranchSpot > extractPropertyMap( T feature );

	@Override
	public T deserialize( FileIdToObjectMap< Spot > idmap, ObjectInputStream ois, ModelBranchGraph branchGraph, ModelGraph graph )
			throws ClassNotFoundException, IOException
	{
		// Read the map link -> value
		final DoublePropertyMap< Spot > spotPropertyMap = new DoublePropertyMap<>( graph.vertices(), -1 );
		final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( spotPropertyMap );
		propertyMapSerializer.readPropertyMap( idmap, ois );

		// Map to branch-link -> value
		DoublePropertyMap< BranchSpot > branchPropertyMap = BranchFeatureSerializer.mapToBranchSpotMap( spotPropertyMap, branchGraph );
		return createFeature( branchPropertyMap );
	}

	@Override
	public void serialize( T feature, ObjectToFileIdMap< Spot > idmap, ObjectOutputStream oos, ModelBranchGraph branchGraph,
			ModelGraph graph ) throws IOException
	{
		DoublePropertyMap< BranchSpot > branchSpotMap = extractPropertyMap( feature );
		final DoublePropertyMap< Spot > spotMap =
				BranchFeatureSerializer.branchSpotMapToMap( branchSpotMap, branchGraph, graph );
		final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( spotMap );
		propertyMapSerializer.writePropertyMap( idmap, oos );
	}
}