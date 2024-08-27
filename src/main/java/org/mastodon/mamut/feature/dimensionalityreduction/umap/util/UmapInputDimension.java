package org.mastodon.mamut.feature.dimensionalityreduction.umap.util;

import net.imglib2.util.Cast;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.SpotTrackIDFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.feature.spot.SpotBranchIDFeature;
import org.mastodon.util.FeatureUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a UMAP input dimension for a given feature and a projection of that feature.
 * <br>
 * This class encapsulates a feature and a projection of it, providing methods to
 * retrieve the projection and generate a string representation of them.
 *
 * @param <V> the type of vertex
 */
public class UmapInputDimension< V extends Vertex< ? > >
{
	private final Feature< V > feature;

	private final FeatureProjection< V > featureProjection;

	/**
	 * Creates a new UMAP input dimension for the given feature and projection.
	 * @param feature the feature
	 * @param featureProjection the projection of the feature
	 */
	public UmapInputDimension( final Feature< V > feature, final FeatureProjection< V > featureProjection )
	{
		this.feature = feature;
		this.featureProjection = featureProjection;
	}

	@Override
	public String toString()
	{
		String featureKey = feature.getSpec().getKey();
		String projectionKey = featureProjection.getKey().toString();
		if ( featureKey.equals( projectionKey ) )
			return featureKey;
		return featureKey + " - " + projectionKey;
	}

	public FeatureProjection< V > getFeatureProjection()
	{
		return featureProjection;
	}

	/**
	 * Returns a list of UMAP input dimensions for the given feature model and vertex type.
	 * <br>
	 * This method collects all features of the given vertex type from the feature model and
	 * creates a UMAP input dimension for each feature and projection.
	 * @param featureModel the feature model
	 * @param vertexType the vertex type, e.g. {@link org.mastodon.mamut.model.Spot} or {{@link org.mastodon.mamut.model.branch.BranchSpot}}
	 * @return a list of UMAP input dimensions
	 * @param <V> the type of vertex
	 */
	public static < V extends Vertex< ? > > List< UmapInputDimension< V > > getListFromFeatureModel( final FeatureModel featureModel,
			final Class< V > vertexType )
	{
		List< UmapInputDimension< V > > umapInputDimensions = new ArrayList<>();
		Collection< Feature< V > > features = FeatureUtils.collectFeatureMap( featureModel, vertexType ).values();
		Collection< Class< ? extends Feature< V > > > excludedFeatures = new ArrayList<>();
		excludedFeatures.add( Cast.unchecked( SpotTrackIDFeature.class ) );
		excludedFeatures.add( Cast.unchecked( SpotBranchIDFeature.class ) );
		for ( Feature< V > feature : features )
		{
			if ( excludedFeatures.contains( feature.getClass() ) || feature instanceof AbstractUmapFeature )
				continue;
			for ( FeatureProjection< V > projection : feature.projections() )
				umapInputDimensions.add( new UmapInputDimension<>( feature, projection ) );
		}
		return umapInputDimensions;
	}
}
