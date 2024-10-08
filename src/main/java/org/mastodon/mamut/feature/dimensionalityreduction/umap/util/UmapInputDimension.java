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
package org.mastodon.mamut.feature.dimensionalityreduction.umap.util;

import net.imglib2.util.Cast;
import net.imglib2.util.Util;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.LinkTargetIdFeature;
import org.mastodon.mamut.feature.SpotTrackIDFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.feature.spot.SpotBranchIDFeature;
import org.mastodon.util.FeatureUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;

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
	private final Feature< ? > feature;

	private final FeatureProjection< ? > featureProjection;

	private final ToDoubleFunction< V > vertexValueFunction;

	/**
	 * Creates a new UMAP input dimension for the given vertex feature and projection.
	 * <br>
	 * The vertex value function is set to the value of the vertex feature projection for a given vertex.
	 * @param vertexFeature the vertex feature
	 * @param vertexProjection the projection of the vertex feature
	 * @return a new UMAP input dimension
	 * @param <V> the type of vertex
	 * @param <E> the type of edge
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > UmapInputDimension< V >
			fromVertexFeature( final Feature< V > vertexFeature, final FeatureProjection< V > vertexProjection )
	{
		return new UmapInputDimension<>( vertexFeature, vertexProjection, vertexProjection::value );
	}

	/**
	 * Creates a new UMAP input dimension for the given edge feature and projection.
	 * <br>
	 * The vertex value function is set to the average of the edge feature projection values for the incoming edges of a given vertex.
	 * @param edgeFeature the edge feature
	 * @param edgeProjection the projection of the edge feature
	 * @return a new UMAP input dimension
	 * @param <V> the type of vertex
	 * @param <E> the type of edge
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > UmapInputDimension< V > fromEdgeFeature( final Feature< E > edgeFeature,
			final FeatureProjection< E > edgeProjection )
	{
		return new UmapInputDimension<>( edgeFeature, edgeProjection, edgeProjectionFunction( edgeProjection ) );
	}

	/**
	 * Creates a new UMAP input dimension for the given feature and projection.
	 * @param feature the vertex feature
	 * @param projection the projection of the vertex feature
	 */
	private UmapInputDimension( final Feature< ? > feature, final FeatureProjection< ? > projection,
			final ToDoubleFunction< V > vertexValueFunction
	)
	{
		this.feature = feature;
		this.featureProjection = projection;
		this.vertexValueFunction = vertexValueFunction;
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

	/**
	 * Returns the value of the UMAP input dimension for the given vertex.
	 * <br>
	 * The value is determined by the given vertex value function.
	 * @param vertex the vertex
	 * @return the value of the UMAP input dimension
	 */
	public double getValue( final V vertex )
	{
		return vertexValueFunction.applyAsDouble( vertex );
	}

	/**
	 * Returns a list of UMAP input dimensions for the given feature model, vertex type and edge type.
	 * <br>
	 * This method collects all features of the given vertex type or edge type from the feature model and
	 * creates a UMAP input dimension for each feature and projection.
	 * @param featureModel the feature model
	 * @param vertexType the vertex type, e.g. {@link org.mastodon.mamut.model.Spot} or {{@link org.mastodon.mamut.model.branch.BranchSpot}}
	 * @param edgeType the edge type, e.g. {@link org.mastodon.mamut.model.Link} or {{@link org.mastodon.mamut.model.branch.BranchLink}}
	 * @return a list of UMAP input dimensions
	 * @param <V> the type of vertex
	 * @param <E> the type of edge
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > List< UmapInputDimension< V > > getListFromFeatureModel(
			final FeatureModel featureModel, final Class< V > vertexType, final Class< E > edgeType )
	{
		List< UmapInputDimension< V > > umapInputDimensions = getVertexDimensions( featureModel, vertexType );
		umapInputDimensions.addAll( getEdgeDimensions( featureModel, edgeType ) );
		getEdgeDimensions( featureModel, edgeType );
		return umapInputDimensions;
	}

	private static < V extends Vertex< E >, E extends Edge< V > > List< UmapInputDimension< V > >
			getVertexDimensions( final FeatureModel featureModel, final Class< V > vertexType )
	{
		List< UmapInputDimension< V > > umapInputDimensions = new ArrayList<>();
		Collection< Feature< V > > vertexFeatures = FeatureUtils.collectFeatureMap( featureModel, vertexType ).values();
		Collection< Class< ? extends Feature< V > > > excludedVertexFeatures = new ArrayList<>();
		excludedVertexFeatures.add( Cast.unchecked( SpotTrackIDFeature.class ) );
		excludedVertexFeatures.add( Cast.unchecked( SpotBranchIDFeature.class ) );
		for ( Feature< V > feature : vertexFeatures )
		{
			if ( excludedVertexFeatures.contains( feature.getClass() ) || feature instanceof AbstractUmapFeature )
				continue;
			for ( FeatureProjection< V > projection : feature.projections() )
				umapInputDimensions.add( UmapInputDimension.fromVertexFeature( feature, projection ) );
		}
		return umapInputDimensions;
	}

	private static < V extends Vertex< E >, E extends Edge< V > > List< UmapInputDimension< V > >
			getEdgeDimensions( final FeatureModel featureModel, final Class< E > edgeType )
	{
		List< UmapInputDimension< V > > umapInputDimensions = new ArrayList<>();
		Collection< Feature< E > > edgeFeatures = FeatureUtils.collectFeatureMap( featureModel, edgeType ).values();
		Collection< Class< ? extends Feature< E > > > excludedEdgeFeatures = new ArrayList<>();
		excludedEdgeFeatures.add( Cast.unchecked( LinkTargetIdFeature.class ) );
		for ( Feature< E > feature : edgeFeatures )
		{
			if ( excludedEdgeFeatures.contains( feature.getClass() ) )
				continue;
			for ( FeatureProjection< E > projection : feature.projections() )
				umapInputDimensions.add( UmapInputDimension.fromEdgeFeature( feature, projection ) );
		}
		return umapInputDimensions;
	}

	private static < V extends Vertex< E >, E extends Edge< V > > ToDoubleFunction< V > edgeProjectionFunction(
			final FeatureProjection< E > edgeProjection )
	{
		return vertex -> {
			Edges< E > incomingEdges = vertex.incomingEdges();
			if ( incomingEdges.isEmpty() )
				return Double.NaN;
			else if ( incomingEdges.size() == 1 )
				return edgeProjection.value( incomingEdges.iterator().next() );
			List< Double > edgeValues = new ArrayList<>( incomingEdges.size() );
			incomingEdges.iterator().forEachRemaining( edge -> edgeValues.add( edgeProjection.value( edge ) ) );
			return Util.average( edgeValues.stream().mapToDouble( Double::doubleValue ).toArray() );
		};
	}
}
