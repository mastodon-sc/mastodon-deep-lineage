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
package org.mastodon.mamut.feature.dimensionalityreduction;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Supplier;

import net.imglib2.util.Cast;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.branch.dimensionalityreduction.tsne.BranchTSneFeatureComputer;
import org.mastodon.mamut.feature.branch.dimensionalityreduction.umap.BranchUmapFeatureComputer;
import org.mastodon.mamut.feature.dimensionalityreduction.tsne.TSneSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.tsne.feature.AbstractTSneFeatureComputer;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeatureComputer;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.feature.spot.dimensionalityreduction.tsne.SpotTSneFeatureComputer;
import org.mastodon.mamut.feature.spot.dimensionalityreduction.umap.SpotUmapFeatureComputer;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;
import org.scijava.prefs.PrefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the dimensionality reduction.
 * <br>
 * This class provides methods to compute the features (such as UMAP, t-SNE, PCA) for selected input dimensions.
 * It also handles the user preferences for the dimensionality reduction parameters.
 */
public class DimensionalityReductionController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private boolean running;

	private final Context context;

	private final Model model;

	protected final PrefService prefs;

	protected boolean isModelGraph;

	protected DimensionalityReductionAlgorithm algorithm;

	private final CommonSettings commonSettings;

	private final UmapSettings umapSettings;

	private final TSneSettings tSneSettings;

	private static final String IS_MODEL_GRAPH = "IsModelGraph";

	private static final String DIMENSIONALITY_REDUCTION_ALGORITHM = "DimensionalityReductionAlgorithm";

	public DimensionalityReductionController( final Model model, final Context context )
	{
		this.model = model;
		this.prefs = context.getService( PrefService.class );
		this.context = context;
		this.commonSettings = CommonSettings.loadSettingsFromPreferences( prefs );
		this.umapSettings = UmapSettings.loadSettingsFromPreferences( prefs );
		this.tSneSettings = TSneSettings.loadSettingsFromPreferences( prefs );
		loadSettingsFromPreferences();
	}

	/**
	 * Gets the common dimensionality reduction settings.
	 * @return the the common dimensionality reduction settings
	 */
	public CommonSettings getCommonSettings()
	{
		return commonSettings;
	}

	/**
	 * Gets the UMAP specific settings.
	 * @return the UMAP specific settings
	 */
	public UmapSettings getUmapSettings()
	{
		return umapSettings;
	}

	/**
	 * Gets the t-SNE specific settings.
	 * @return the t-SNE specific settings
	 */
	public TSneSettings getTSneSettings()
	{
		return tSneSettings;
	}

	/**
	 * Computes the dimensionality reduction with the selected algorithm for the selected input dimensions.
	 * <br>
	 * Since the dimensionality reduction computations are computationally expensive, this method prevents multiple executions of itself at the same time.
	 * @param inputDimensionsSupplier a supplier for the selected input dimensions
	 */
	public < V extends Vertex< E >, E extends Edge< V > > void
			computeFeature( final Supplier< List< InputDimension< V > > > inputDimensionsSupplier )
	{
		if ( running )
		{
			logger.debug( "Dimensionality computation currently running." );
			return;
		}

		try
		{
			running = true;
			if ( inputDimensionsSupplier != null )
				updateFeature( inputDimensionsSupplier.get() );
		}
		finally
		{
			running = false;
		}
	}

	/**
	 * Sets the graph type for the input and output feature.
	 * @param isModelGraph {@code true} if the dimensionality reduction is to be computed for the model graph, {@code false} for the branch graph
	 */
	public void setModelGraph( final boolean isModelGraph )
	{
		this.isModelGraph = isModelGraph;
	}

	/**
	 * Sets the dimensionality reduction algorithm.
	 * @param algorithm the algorithm
	 */
	public void setAlgorithm( final DimensionalityReductionAlgorithm algorithm )
	{
		this.algorithm = algorithm;
	}

	private < V extends Vertex< E >, E extends Edge< V >, G extends ReadOnlyGraph< V, E > > void
			updateFeature( final List< InputDimension< V > > inputDimensions )
	{
		if ( inputDimensions.isEmpty() )
		{
			logger.error( "No features selected." );
			throw new IllegalArgumentException( "No features selected." );
		}
		if ( commonSettings.getNumberOfOutputDimensions() >= inputDimensions.size() )
		{
			logger.error( "Number of output dimensions ({}) must be smaller than the number of input features {}.",
					commonSettings.getNumberOfOutputDimensions(), inputDimensions.size() );
			throw new IllegalArgumentException( "Number of output dimensions (" + commonSettings.getNumberOfOutputDimensions()
					+ ") must be smaller than the number of input features (" + inputDimensions.size() + ")." );
		}

		G graph = getGraph( isModelGraph );
		switch ( algorithm )
		{
		case UMAP:
			AbstractUmapFeatureComputer< V, E, G > umapFeatureComputer =
					isModelGraph ? Cast.unchecked( new SpotUmapFeatureComputer( model, context ) )
							: Cast.unchecked( new BranchUmapFeatureComputer( model, context ) );
			umapFeatureComputer.computeFeature( commonSettings, umapSettings, inputDimensions, graph );
			break;
		case TSNE:
			AbstractTSneFeatureComputer< V, E, G > tSneFeatureComputer =
					isModelGraph ? Cast.unchecked( new SpotTSneFeatureComputer( model, context ) )
							: Cast.unchecked( new BranchTSneFeatureComputer( model, context ) );
			try
			{
				tSneFeatureComputer.computeFeature( commonSettings, tSneSettings, inputDimensions, graph );
			}
			catch ( ArrayIndexOutOfBoundsException e )
			{
				logger.error( "Not enough data for t-SNE computation. {}", e.getMessage() );
				throw new ArrayIndexOutOfBoundsException( "Not enough data for t-SNE computation." );
			}
			break;
		default:
			throw new IllegalArgumentException( "Unknown algorithm: " + algorithm );
		}
	}

	private < V extends Vertex< E >, E extends Edge< V >, G extends ReadOnlyGraph< V, ? > > G getGraph( boolean isSpotGraph )
	{
		if ( isSpotGraph )
			return Cast.unchecked( model.getGraph() );
		return Cast.unchecked( model.getBranchGraph() );
	}

	/**
	 * Gets the vertex and edge type for the dimensionality reduction, i.e. {@link Spot} or {@link BranchSpot}.
	 * @return the vertex type
	 */
	public Class< ? extends Vertex< ? > > getVertexType()
	{
		if ( isModelGraph )
			return Spot.class;
		return BranchSpot.class;
	}

	/**
	 * Gets the edge type for the dimensionality reduction, i.e. {@link Link} or {@link BranchLink}.
	 * @return the edge type
	 */
	public Class< ? extends Edge< ? > > getEdgeType()
	{
		if ( isModelGraph )
			return Link.class;
		return BranchLink.class;
	}

	/**
	 * Gets from the user preferences, whether the dimensionality reduction is to be computed for the model graph.
	 * If the preferences are not available, the default value is {@code true}.
	 * @return {@code true} if the dimensionality reduction is to be computed for the model graph, {@code false} otherwise
	 */
	public boolean isModelGraphPreferences()
	{
		return prefs == null || prefs.getBoolean( DimensionalityReductionController.class, IS_MODEL_GRAPH, true );
	}

	/**
	 * Gets the dimensionality reduction algorithm from the user preferences.
	 * If the preferences are not available, the default value is {@link DimensionalityReductionAlgorithm#UMAP}.
	 * @return the dimensionality reduction algorithm
	 */
	public DimensionalityReductionAlgorithm getAlgorithm()
	{
		return prefs == null ? DimensionalityReductionAlgorithm.UMAP
				: DimensionalityReductionAlgorithm
						.valueOf( prefs.get( DimensionalityReductionController.class, DIMENSIONALITY_REDUCTION_ALGORITHM,
								DimensionalityReductionAlgorithm.UMAP.name() ) );
	}

	private void loadSettingsFromPreferences()
	{
		isModelGraph = prefs == null || prefs.getBoolean( DimensionalityReductionController.class, IS_MODEL_GRAPH, true );
		algorithm = DimensionalityReductionAlgorithm.valueOf( prefs == null ? DimensionalityReductionAlgorithm.UMAP.name()
				: prefs.get( DimensionalityReductionController.class, DIMENSIONALITY_REDUCTION_ALGORITHM,
						DimensionalityReductionAlgorithm.UMAP.name() ) );
	}

	/**
	 * Saves the dimensionality reduction settings to the user preferences.
	 */
	public void saveSettingsToPreferences()
	{
		logger.debug( "Save dimensionality reduction settings." );
		if ( prefs == null )
			return;
		prefs.put( DimensionalityReductionController.class, IS_MODEL_GRAPH, isModelGraph );
		prefs.put( DimensionalityReductionController.class, DIMENSIONALITY_REDUCTION_ALGORITHM, algorithm.name() );
		commonSettings.saveSettingsToPreferences( prefs );
		umapSettings.saveSettingsToPreferences( prefs );
		tSneSettings.saveSettingsToPreferences( prefs );
	}
}
