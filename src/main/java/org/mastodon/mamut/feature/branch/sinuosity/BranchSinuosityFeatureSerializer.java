package org.mastodon.mamut.feature.branch.sinuosity;

import static org.mastodon.mamut.feature.branch.sinuosity.BranchSinuosityFeature.BRANCH_SINUOSITY_FEATURE_SPEC;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

/**
 * De-/serializes the {@link BranchSinuosityFeature}.
 */
@Plugin( type = FeatureSerializer.class )
public class BranchSinuosityFeatureSerializer
		implements BranchFeatureSerializer< BranchSinuosityFeature, BranchSpot, Spot >
{
	@Override
	public BranchSinuosityFeature.Spec getFeatureSpec()
	{
		return BRANCH_SINUOSITY_FEATURE_SPEC;
	}

	@Override
	public BranchSinuosityFeature deserialize( FileIdToObjectMap< Spot > fileIdToObjectMap,
			ObjectInputStream objectInputStream, ModelBranchGraph branchGraph, ModelGraph graph )
			throws ClassNotFoundException, IOException
	{
		// Read the map link -> value
		final DoublePropertyMap< Spot > map = new DoublePropertyMap<>( graph.vertices(), Double.NaN );
		final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( map );
		propertyMapSerializer.readPropertyMap( fileIdToObjectMap, objectInputStream );

		// Map to branch-link -> value
		return new BranchSinuosityFeature( BranchFeatureSerializer.mapToBranchSpotMap( map, branchGraph ) );
	}

	@Override
	public void serialize( BranchSinuosityFeature feature, ObjectToFileIdMap< Spot > objectToFileIdMap,
			ObjectOutputStream objectOutputStream, ModelBranchGraph branchGraph, ModelGraph graph ) throws IOException
	{
		final DoublePropertyMap< Spot > map =
				BranchFeatureSerializer.branchSpotMapToMap( feature.map, branchGraph, graph );
		final DoublePropertyMapSerializer< Spot > propertyMapSerializer = new DoublePropertyMapSerializer<>( map );
		propertyMapSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
	}
}
