package org.mastodon.mamut.feature.branch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.IntPropertyMapSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;

public abstract class BranchSpotIntPropertyFeatureSerializer< T extends IntPropertyFeature< BranchSpot > >
		implements BranchFeatureSerializer< T, BranchSpot, Spot >
{

	protected abstract T createFeature( IntPropertyMap< BranchSpot > map );

	protected abstract IntPropertyMap< BranchSpot > extractPropertyMap( T feature );

	@Override
	public T deserialize( FileIdToObjectMap< Spot > idmap, ObjectInputStream ois, ModelBranchGraph branchGraph, ModelGraph graph )
			throws ClassNotFoundException, IOException
	{
		// Read the map link -> value
		final IntPropertyMap< Spot > spotPropertyMap = new IntPropertyMap<>( graph.vertices(), -1 );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( spotPropertyMap );
		propertyMapSerializer.readPropertyMap( idmap, ois );

		// Map to branch-link -> value
		IntPropertyMap< BranchSpot > branchPropertyMap = BranchFeatureSerializer.mapToBranchSpotMap( spotPropertyMap, branchGraph );
		return createFeature( branchPropertyMap );
	}

	@Override
	public void serialize( T feature, ObjectToFileIdMap< Spot > idmap, ObjectOutputStream oos, ModelBranchGraph branchGraph,
			ModelGraph graph ) throws IOException
	{
		IntPropertyMap< BranchSpot > branchSpotMap = extractPropertyMap( feature );
		final IntPropertyMap< Spot > spotMap =
				BranchFeatureSerializer.branchSpotMapToMap( branchSpotMap, branchGraph, graph );
		final IntPropertyMapSerializer< Spot > propertyMapSerializer = new IntPropertyMapSerializer<>( spotMap );
		propertyMapSerializer.writePropertyMap( idmap, oos );
	}
}
