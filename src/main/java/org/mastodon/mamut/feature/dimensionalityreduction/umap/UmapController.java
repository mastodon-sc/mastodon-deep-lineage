package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import net.imglib2.util.Cast;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.branch.dimensionalityreduction.umap.BranchUmapFeatureComputer;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeatureComputer;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.util.UmapInputDimension;
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

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Supplier;

/**
 * Controller for the UMAP feature computation.
 * <br>
 * This class provides methods to compute the UMAP feature for selected input dimensions.
 * It also handles the user preferences for the UMAP feature settings.
 */
public class UmapController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private boolean running;

	private final Context context;

	private final Model model;

	private final PrefService prefs;

	private boolean isModelGraph;

	private final UmapFeatureSettings settings;

	private static final String IS_MODEL_GRAPH = "IsModelGraph";

	private static final String NUMBER_OF_DIMENSIONS_SETTING = "NumberOfDimensions";

	private static final String NUMBER_OF_NEIGHBORS_SETTING = "NumberOfNeighbors";

	private static final String MINIMUM_DISTANCE_SETTING = "MinimumDistance";

	private static final String STANDARDIZE_FEATURES_SETTING = "StandardizeFeatures";

	public UmapController( final Model model, final Context context )
	{
		this.model = model;
		this.prefs = context.getService( PrefService.class );
		this.context = context;
		this.settings = loadSettingsFromPreferences();
	}

	/**
	 * Gets the UMAP feature settings.
	 * @return the UMAP feature settings
	 */
	public UmapFeatureSettings getFeatureSettings()
	{
		return settings;
	}

	/**
	 * Computes the UMAP feature for the selected input dimensions.
	 * <br>
	 * Since the UMAP computation is computationally expensive, this method prevents multiple executions of itself at the same time.
	 * @param inputDimensionsSupplier a supplier for the selected input dimensions
	 */
	public < V extends Vertex< E >, E extends Edge< V > > void
			computeFeature( final Supplier< List< UmapInputDimension< V, E > > > inputDimensionsSupplier )
	{
		if ( running )
		{
			logger.debug( "UMAP computation currently running." );
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
	 * Sets the graph type for the UMAP feature.
	 * @param isModelGraph {@code true} if the UMAP feature is to be computed for the model graph, {@code false} for the branch graph
	 */
	public void setModelGraph( final boolean isModelGraph )
	{
		this.isModelGraph = isModelGraph;
	}

	private < V extends Vertex< E >, E extends Edge< V >, G extends ReadOnlyGraph< V, E > > void
			updateFeature( final List< UmapInputDimension< V, E > > inputDimensions )
	{
		if ( inputDimensions.isEmpty() )
			throw new IllegalArgumentException( "No features selected." );
		if ( settings.getNumberOfOutputDimensions() >= inputDimensions.size() )
			throw new IllegalArgumentException( "Number of output dimensions (" + settings.getNumberOfOutputDimensions()
					+ ") must be smaller than the number of input features (" + inputDimensions.size() + ")." );
		G graph = getGraph( isModelGraph );
		AbstractUmapFeatureComputer< V, E, G > umapFeatureComputer =
				isModelGraph ? Cast.unchecked( new SpotUmapFeatureComputer( model, context ) )
						: Cast.unchecked( new BranchUmapFeatureComputer( model, context ) );
		umapFeatureComputer.computeFeature( settings, inputDimensions, graph );
	}

	private < V extends Vertex< E >, E extends Edge< V >, G extends ReadOnlyGraph< V, ? > > G getGraph( boolean isSpotGraph )
	{
		if ( isSpotGraph )
			return Cast.unchecked( model.getGraph() );
		return Cast.unchecked( model.getBranchGraph() );
	}

	/**
	 * Gets the vertex type for the UMAP feature, i.e. {@link Spot} or {@link BranchSpot}.
	 * @return the vertex type
	 */
	public < V extends Vertex< E >, E extends Edge< V > > Class< V > getVertexType()
	{
		if ( isModelGraph )
			return Cast.unchecked( Spot.class );
		return Cast.unchecked( BranchSpot.class );
	}

	/**
	 * Gets the edge type for the UMAP feature, i.e. {@link Link} or {@link BranchLink}.
	 * @return the vertex type
	 */
	public < V extends Vertex< E >, E extends Edge< V > > Class< E > getEdgeType()
	{
		if ( isModelGraph )
			return Cast.unchecked( Link.class );
		return Cast.unchecked( BranchLink.class );
	}

	/**
	 * Gets from the user preferences, whether the UMAP feature is to be computed for the model graph.
	 * If the preferences are not available, the default value is {@code true}.
	 * @return {@code true} if the UMAP feature is to be computed for the model graph, {@code false} otherwise
	 */
	public boolean isModelGraphPreferences()
	{
		return prefs == null || prefs.getBoolean( UmapController.class, IS_MODEL_GRAPH, true );
	}

	private UmapFeatureSettings loadSettingsFromPreferences()
	{
		isModelGraph = prefs == null || prefs.getBoolean( UmapController.class, IS_MODEL_GRAPH, true );
		boolean standardizeFeatures = prefs == null
				|| prefs.getBoolean( UmapController.class, STANDARDIZE_FEATURES_SETTING, UmapFeatureSettings.DEFAULT_STANDARDIZE_FEATURES );
		int numberOfDimensions = prefs == null ? UmapFeatureSettings.DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS
				: prefs.getInt( UmapController.class, NUMBER_OF_DIMENSIONS_SETTING,
						UmapFeatureSettings.DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS );
		int numberOfNeighbours = prefs == null ? UmapFeatureSettings.DEFAULT_NUMBER_OF_NEIGHBORS
				: prefs.getInt( UmapController.class, NUMBER_OF_NEIGHBORS_SETTING, UmapFeatureSettings.DEFAULT_NUMBER_OF_NEIGHBORS );
		double minimumDistance = prefs == null ? UmapFeatureSettings.DEFAULT_MINIMUM_DISTANCE
				: prefs.getDouble( UmapController.class, MINIMUM_DISTANCE_SETTING, UmapFeatureSettings.DEFAULT_MINIMUM_DISTANCE );
		return new UmapFeatureSettings( numberOfDimensions, numberOfNeighbours, minimumDistance, standardizeFeatures );
	}

	/**
	 * Saves the UMAP settings to the user preferences.
	 */
	public void saveSettingsToPreferences()
	{
		logger.debug( "Save UMAP settings." );
		if ( prefs == null )
			return;
		prefs.put( UmapController.class, IS_MODEL_GRAPH, isModelGraph );
		prefs.put( UmapController.class, STANDARDIZE_FEATURES_SETTING, settings.isStandardizeFeatures() );
		prefs.put( UmapController.class, NUMBER_OF_DIMENSIONS_SETTING, settings.getNumberOfOutputDimensions() );
		prefs.put( UmapController.class, NUMBER_OF_NEIGHBORS_SETTING, settings.getNumberOfNeighbors() );
		prefs.put( UmapController.class, MINIMUM_DISTANCE_SETTING, settings.getMinimumDistance() );
	}
}
