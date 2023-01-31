package org.mastodon.mamut.feature.branch;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = MamutPlugin.class )
public class ModelBranchGraphExportPlugin implements MamutPlugin
{
	@Nullable
	private MamutPluginAppModel pluginAppModel;

	private static final String EXPORT_MODEL_BRANCH_GRAPH = "Export selected tracks downwards to GraphML (branch graph)";

	private static final String EXPORT_MODEL_BRANCH_GRAPHS_FROM_ROOT_NODES =
			"Export all tracks to GraphML (branch graph, one file per root node)";

	private static final String[] EXPORT_MODEL_BRANCH_GRAPH_KEYS = { "not mapped" };

	private static final String[] EXPORT_MODEL_BRANCH_GRAPHS_FROM_ROOT_NODES_KEYS = { "not mapped" };

	private final AbstractNamedAction exportModelBranchGraphAction;

	private final AbstractNamedAction exportRootNodesAction;

	private final static int NUMBER_OF_SPOTS = 250;

	public ModelBranchGraphExportPlugin()
	{
		exportModelBranchGraphAction = new RunnableAction( EXPORT_MODEL_BRANCH_GRAPH, this::chooseFileAndExportSelectedModelBranchGraph );
		exportRootNodesAction =
				new RunnableAction( EXPORT_MODEL_BRANCH_GRAPHS_FROM_ROOT_NODES, () -> {
					if ( pluginAppModel == null )
						throw new IllegalStateException( "Plugin not initialized." );
					File projectRoot = pluginAppModel.getWindowManager().getProjectManager().getProject().getProjectRoot();
					String projectName = projectRoot.getName().replace( "\\", "_" );
					int cutoffTimePoint =
							LineageTreeUtils.getTimePointWithNSpots( pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex(),
									pluginAppModel.getAppModel().getMinTimepoint(), pluginAppModel.getAppModel().getMaxTimepoint(),
									NUMBER_OF_SPOTS );
					System.out.println( "Number of spots: " + NUMBER_OF_SPOTS + ", cutoffTimePoint: " + cutoffTimePoint );
					LineageTreeUtils.exportAllModelBranchGraphsPerRootNode( pluginAppModel.getAppModel().getModel().getBranchGraph(),
							cutoffTimePoint, new File( System.getProperty( "user.dir" ) ), projectName );
				} );
		updateEnabledActions();
	}

	@Override
	public void setAppPluginModel( MamutPluginAppModel pluginAppModel )
	{
		this.pluginAppModel = pluginAppModel;
		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		exportModelBranchGraphAction.setEnabled( pluginAppModel != null && pluginAppModel.getAppModel() != null );
		exportRootNodesAction.setEnabled( pluginAppModel != null && pluginAppModel.getAppModel() != null );
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList(
				menu( "Plugins",
						menu( "Model Branch Graph Export",
								item( EXPORT_MODEL_BRANCH_GRAPH ),
								item( EXPORT_MODEL_BRANCH_GRAPHS_FROM_ROOT_NODES ) ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( exportModelBranchGraphAction, EXPORT_MODEL_BRANCH_GRAPH_KEYS );
		actions.namedAction( exportRootNodesAction, EXPORT_MODEL_BRANCH_GRAPHS_FROM_ROOT_NODES_KEYS );
	}

	private void exportSelectedModelBranchGraph( @Nonnull File file ) throws IllegalStateException
	{
		if ( pluginAppModel == null )
			throw new IllegalStateException( "Plugin not initialized." );

		// Get the selected spots from the UI
		RefSet< Spot > selectedSpots = pluginAppModel.getAppModel().getSelectionModel().getSelectedVertices();

		ModelBranchGraph modelBranchGraph = pluginAppModel.getAppModel().getModel().getBranchGraph();

		Set< BranchSpot > selectedBranchSpots = new HashSet<>();
		selectedSpots.forEach( spot -> selectedBranchSpots.add( modelBranchGraph.getBranchVertex( spot, modelBranchGraph.vertexRef() ) ) );

		// Check, if any links are selected, if no spots are selected
		if ( selectedSpots.isEmpty() )
		{
			RefSet< Link > selectedEdges = pluginAppModel.getAppModel().getSelectionModel().getSelectedEdges();
			// Add the source and target spots of the selected links to the selected spots
			selectedEdges.forEach( link -> {
				selectedSpots.add( link.getSource( pluginAppModel.getAppModel().getModel().getGraph().vertexRef() ) );
				selectedSpots.add( link.getTarget( pluginAppModel.getAppModel().getModel().getGraph().vertexRef() ) );
			} );
		}

		System.out.println( selectedSpots.size() + " spots are selected" );

		// Export the selected spots and their links to a GraphML file
		LineageTreeUtils.writeModelBranchGraphOfBranchSpotsToFile( pluginAppModel.getAppModel().getModel().getBranchGraph(),
				selectedBranchSpots, file );
	}

	private void chooseFileAndExportSelectedModelBranchGraph()
	{
		final File file = FileChooser.chooseFile( true,
				null,
				null,
				new ExtensionFileFilter( "graphml" ),
				"Save Model Branch Graph",
				FileChooser.DialogType.SAVE,
				FileChooser.SelectionMode.FILES_ONLY,
				SAVE_ICON_MEDIUM.getImage() );
		if ( file != null )
		{
			exportSelectedModelBranchGraph( file );
		}
		else
		{
			System.out.println( "No file selected." );
		}
	}
}
