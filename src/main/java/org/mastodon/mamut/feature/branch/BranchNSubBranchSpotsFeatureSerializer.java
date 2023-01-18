package org.mastodon.mamut.feature.branch;

import static org.mastodon.mamut.feature.branch.BranchNSubBranchSpotsFeature.BRANCH_N_SUB_BRANCH_SPOTS_FEATURE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes the {@link BranchNSubBranchSpotsFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchNSubBranchSpotsFeatureSerializer
		implements BranchFeatureSerializer< BranchNSubBranchSpotsFeature, BranchSpot, Spot >
{
	@Override
	public BranchNSubBranchSpotsFeature.Spec getFeatureSpec()
	{
		return BRANCH_N_SUB_BRANCH_SPOTS_FEATURE;
	}

	@Override
	public BranchNSubBranchSpotsFeature deserialize( FileIdToObjectMap< Spot > fileIdToObjectMap,
			ObjectInputStream objectInputStream, ModelBranchGraph branchGraph, ModelGraph graph )
			throws ClassNotFoundException, IOException
	{
		// Read the map link -> value
		final IntPropertyMap< Spot > map = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( map );
		propertyMapSerializer.readPropertyMap( fileIdToObjectMap, objectInputStream );

		// Map to branch-link -> value
		return new BranchNSubBranchSpotsFeature( BranchFeatureSerializer.mapToBranchSpotMap( map, branchGraph ) );
	}

	@Override
	public void serialize( BranchNSubBranchSpotsFeature feature, ObjectToFileIdMap< Spot > objectToFileIdMap,
			ObjectOutputStream objectOutputStream, ModelBranchGraph branchGraph, ModelGraph graph ) throws IOException
	{
		final IntPropertyMap< Spot > map =
				BranchFeatureSerializer.branchSpotMapToMap( feature.map, branchGraph, graph );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( map );
		propertyMapSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
	}
}
