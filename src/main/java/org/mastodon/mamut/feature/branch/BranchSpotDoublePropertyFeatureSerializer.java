package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.FeatureSpec;
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

public abstract class BranchSpotDoublePropertyFeatureSerializer< T extends DoublePropertyFeature< BranchSpot > >
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
