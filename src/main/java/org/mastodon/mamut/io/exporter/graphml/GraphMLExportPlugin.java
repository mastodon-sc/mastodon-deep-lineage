package org.mastodon.mamut.io.exporter.graphml;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@SuppressWarnings( "unused" )
@Plugin( type = MamutPlugin.class )
public class GraphMLExportPlugin implements MamutPlugin
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private ProjectModel projectModel;

	public static final String NOT_MAPPED = "not mapped";

	private static final String EXPORT_BRANCH_GRAPH = "Export all branches to GraphML (one file)";

	private static final String EXPORT_SELECTED_BRANCH_GRAPH = "Export selected branches to GraphML (one file)";

	private static final String EXPORT_LINEAGES = "Export tracks to GraphML (one file per track)";

	private static final String[] EXPORT_BRANCH_GRAPH_KEYS = { NOT_MAPPED };

	private static final String[] EXPORT_SELECTED_BRANCH_GRAPH_KEYS = { NOT_MAPPED };

	private static final String[] EXPORT_LINEAGES_KEYS = { NOT_MAPPED };

	private final AbstractNamedAction exportBranchGraph;

	private final AbstractNamedAction exportSelectedBranchGraph;

	private final AbstractNamedAction exportLineages;

	@SuppressWarnings( "unused" )
	public GraphMLExportPlugin()
	{
		exportBranchGraph = new RunnableAction( EXPORT_BRANCH_GRAPH, this::exportBranchGraph );
		exportSelectedBranchGraph = new RunnableAction( EXPORT_SELECTED_BRANCH_GRAPH, this::exportSelectedBranchGraph );
		exportLineages = new RunnableAction( EXPORT_LINEAGES, this::exportLineages );
	}

	@Override
	public void setAppPluginModel( ProjectModel projectModel )
	{
		this.projectModel = projectModel;
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Collections.singletonList(
				menu( "Plugins",
						menu( "Exports",
								menu( "Export GraphML (Branches)",
										item( EXPORT_BRANCH_GRAPH ),
										item( EXPORT_SELECTED_BRANCH_GRAPH ),
										item( EXPORT_LINEAGES ) )
						)
				) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( exportBranchGraph, EXPORT_BRANCH_GRAPH_KEYS );
		actions.namedAction( exportSelectedBranchGraph, EXPORT_SELECTED_BRANCH_GRAPH_KEYS );
		actions.namedAction( exportLineages, EXPORT_LINEAGES_KEYS );
	}

	private void exportModelBranchGraph( final File file )
	{
		RefSet< BranchSpot > branchSpots = RefCollections.createRefSet( projectModel.getModel().getBranchGraph().vertices() );
		RefSet< BranchLink > branchLinks = RefCollections.createRefSet( projectModel.getModel().getBranchGraph().edges() );
		branchSpots.addAll( projectModel.getModel().getBranchGraph().vertices() );
		branchLinks.addAll( projectModel.getModel().getBranchGraph().edges() );

		// Export the selected spots and their links to a GraphML file
		GraphMLUtils.exportBranches( branchSpots, branchLinks, file );
	}

	private void exportSelectedModelBranchGraph( final File file )
	{
		// Get the selected spots from the UI
		RefSet< BranchSpot > selectedBranchSpots = LineageTreeUtils.getBranchSpots( projectModel.getModel(),
				projectModel.getSelectionModel().getSelectedVertices(), projectModel.getSelectionModel().getSelectedEdges() );
		RefSet< BranchLink > selectedBranchLinks = LineageTreeUtils.getBranchLinks( projectModel.getModel(),
				projectModel.getSelectionModel().getSelectedEdges() );

		logger.debug( "Selected branch spots: {}", selectedBranchSpots.size() );
		logger.debug( "Selected branch links: {}", selectedBranchLinks.size() );

		// Export the selected spots and their links to a GraphML file
		GraphMLUtils.exportBranches( selectedBranchSpots, selectedBranchLinks, file );
	}

	private void exportLineages( final File folder )
	{
		File projectRoot = projectModel.getProject().getProjectRoot();
		String projectName = getFileNameWithoutExtension( projectRoot ).replace( File.separator, "_" );
		GraphMLUtils.exportAllTracks( projectModel.getModel().getBranchGraph(), folder, projectName );
	}

	private void chooseFileAndExport( final Consumer< File > fileConsumer, final Supplier< File > fileSupplier )
	{
		File file = fileSupplier.get();
		if ( file != null )
			fileConsumer.accept( file );
		else
			logger.info( "No export target selected." );
	}

	private void exportBranchGraph()
	{
		chooseFileAndExport( this::exportModelBranchGraph, GraphMLExportPlugin::getFileFromFileChooser );
	}

	private void exportSelectedBranchGraph()
	{
		chooseFileAndExport( this::exportSelectedModelBranchGraph, GraphMLExportPlugin::getFileFromFileChooser );
	}

	private void exportLineages()
	{
		chooseFileAndExport( this::exportLineages, GraphMLExportPlugin::getFolderFromFileChooser );
	}

	private static File getFileFromFileChooser()
	{
		return FileChooser.chooseFile( true,
				null,
				null,
				new ExtensionFileFilter( "graphml" ),
				"Save Branch Graph to GraphML",
				FileChooser.DialogType.SAVE,
				FileChooser.SelectionMode.FILES_ONLY,
				SAVE_ICON_MEDIUM.getImage() );
	}

	private static File getFolderFromFileChooser()
	{
		return FileChooser.chooseFile( true,
				null,
				null,
				null,
				"Save Lineages to folder",
				FileChooser.DialogType.SAVE,
				FileChooser.SelectionMode.DIRECTORIES_ONLY,
				SAVE_ICON_MEDIUM.getImage() );
	}

	private static String getFileNameWithoutExtension( final File file )
	{
		String fileName = file.getName();
		int lastDotIndex = fileName.lastIndexOf( '.' );
		if ( lastDotIndex > 0 )
			return fileName.substring( 0, lastDotIndex );
		else
			return fileName;
	}
}
