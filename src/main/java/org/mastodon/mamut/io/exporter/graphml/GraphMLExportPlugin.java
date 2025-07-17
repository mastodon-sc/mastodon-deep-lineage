/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.io.exporter.graphml;

import org.apache.commons.io.FilenameUtils;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Graph;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@SuppressWarnings( "unused" )
@Plugin( type = MamutPlugin.class )
public class GraphMLExportPlugin extends AbstractContextual implements MamutPlugin
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private ProjectModel projectModel;

	private static final String EXPORT_BRANCH_GRAPH = "Export all branches to GraphML (one file)";

	private static final String EXPORT_SELECTED_BRANCH_GRAPH = "Export selected branches to GraphML (one file)";

	private static final String EXPORT_SELECTED_GRAPH = "Export selected spots to GraphML (one file)";

	private static final String EXPORT_LINEAGES = "Export tracks to GraphML (one file per track)";

	private static final String[] EXPORT_BRANCH_GRAPH_KEYS = { "ctrl G" };

	private static final String[] EXPORT_SELECTED_BRANCH_GRAPH_KEYS = { "ctrl shift G" };

	private static final String[] EXPORT_SELECTED_GRAPH_KEYS = { "ctrl shift S" };

	private static final String[] EXPORT_LINEAGES_KEYS = { "ctrl alt G" };

	private final AbstractNamedAction exportBranchGraph;

	private final AbstractNamedAction exportSelectedBranchGraph;

	private final AbstractNamedAction exportSelectedGraph;

	private final AbstractNamedAction exportLineages;

	@SuppressWarnings( "unused" )
	public GraphMLExportPlugin()
	{
		exportBranchGraph = new RunnableAction( EXPORT_BRANCH_GRAPH, this::exportBranchGraph );
		exportSelectedBranchGraph = new RunnableAction( EXPORT_SELECTED_BRANCH_GRAPH, this::exportSelectedBranchGraph );
		exportSelectedGraph = new RunnableAction( EXPORT_SELECTED_GRAPH, this::exportSelectedGraph );
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
		return Collections.singletonList( MamutMenuBuilder.fileMenu(
				menu( "Export",
						menu( "Export to GraphML (branches)",
								item( EXPORT_BRANCH_GRAPH ), item( EXPORT_SELECTED_BRANCH_GRAPH ), item( EXPORT_LINEAGES ) ),
						menu( "Export to GraphML (individual spots)", item( EXPORT_SELECTED_GRAPH ) ) ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( exportBranchGraph, EXPORT_BRANCH_GRAPH_KEYS );
		actions.namedAction( exportSelectedBranchGraph, EXPORT_SELECTED_BRANCH_GRAPH_KEYS );
		actions.namedAction( exportSelectedGraph, EXPORT_SELECTED_GRAPH_KEYS );
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

		// Export the selected branch spots and their links to a GraphML file
		GraphMLUtils.exportBranches( selectedBranchSpots, selectedBranchLinks, file );
	}

	private void exportLineages( final File folder )
	{
		File projectRoot = projectModel.getProject().getProjectRoot();
		String projectName = FilenameUtils.getBaseName( projectRoot.getName() );
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

	private void exportSelectedGraph()
	{
		chooseFileAndExport( file -> GraphMLUtils.exportSelectedSpotsAndLinks( projectModel, file ),
				GraphMLExportPlugin::getFileFromFileChooser );
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

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( EXPORT_BRANCH_GRAPH, EXPORT_BRANCH_GRAPH_KEYS, "Exports all branches of branch graph to one graphml file." );
			descriptions.add( EXPORT_SELECTED_BRANCH_GRAPH, EXPORT_SELECTED_BRANCH_GRAPH_KEYS,
					"Exports selected branches of branch graph to one graphml file." );
			descriptions.add( EXPORT_LINEAGES, EXPORT_LINEAGES_KEYS,
					"Export all tracks (i.e. roots and downward lineage) to one GraphML file per track." );
			descriptions.add( EXPORT_SELECTED_GRAPH, EXPORT_SELECTED_GRAPH_KEYS, "Exports selected spots and links to one graphml file." );
		}
	}
}
