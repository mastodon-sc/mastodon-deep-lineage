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
package org.mastodon.mamut.lineagemodules.ui;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicInteger;

import org.mastodon.collection.RefSet;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.treesimilarity.TreeDistances;
import org.mastodon.mamut.clustering.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.ui.Notification;
import org.mastodon.mamut.lineagemodules.util.SelectedRootsFinder;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.util.DepthFirstIteration;
import org.scijava.ItemVisibility;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin( type = DynamicCommand.class, label = "Find Lineage Modules" )
public class FindLineageModulesCommand extends DynamicCommand
{

	private static final float WIDTH = 18.5f;

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@SuppressWarnings( "unused" )
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Find Lineage Modules</h1>\n"
			+ "<p>This command is capable of grouping similar lineage trees together, i.e. trees that share a similar cell division pattern. This is realised by creating a new tag set and assigning the same tag to lineage trees that are similar to each other.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	/**
	 * This method is executed whenever a parameter changes
	 */
	@Override
	public void run()
	{
		BranchSpotTree lineageModule = getSelectedModule();
		double distance =
				ZhangUnorderedTreeEditDistance.distance( lineageModule, lineageModule, TreeDistances.LOCAL_NORMALIZED_COST_FUNCTION );
		int moduleLength = lineageModule.getEndTimepoint() - lineageModule.getStartTimepoint();
		logger.info( "Module length: {}", moduleLength );
		logger.info( "Distance of the selected lineage tree to itself: {}", distance );
	}

	private BranchSpotTree getSelectedModule()
	{
		Model model = projectModel.getModel();
		RefSet< Spot > selectedRootSpots =
				SelectedRootsFinder.getRoots( projectModel.getModel().getGraph(), projectModel.getSelectionModel() );
		if ( selectedRootSpots.isEmpty() )
		{
			logger.warn( "No selected spots found. Cannot find lineage modules." );
			Notification.showError( "No spots selected.", "Please select spots to define a lineage module." );
		}
		if ( selectedRootSpots.size() > 1 )
		{
			logger.warn( "Multiple lineage modules ({}) found  spots found. Only one is allowed", selectedRootSpots.size() );
			Notification.showError( "Multiple modules selected.",
					"You have selected " + selectedRootSpots.size() + " lineage modules. Please select only one." );
			return null;
		}
		logger.info( "Running FindLineageModulesCommand. Selected roots: {}", selectedRootSpots.size() );
		Spot selectedRoot = selectedRootSpots.iterator().next();
		AtomicInteger highestTimepoint = new AtomicInteger( selectedRoot.getTimepoint() );
		Iterable< DepthFirstIteration.Step< Spot > > depthFirstIteration = DepthFirstIteration.forRoot( model.getGraph(), selectedRoot );
		depthFirstIteration.forEach( spotStep -> {
			Spot spot = spotStep.node();
			boolean isSelected = projectModel.getSelectionModel().isSelected( spot );
			if ( isSelected && spot.getTimepoint() > highestTimepoint.get() )
				highestTimepoint.set( spot.getTimepoint() );
			if ( !isSelected )
				spotStep.truncate(); // do not traverse further if the spot is not selected
		} );
		BranchSpot ref = model.getBranchGraph().vertexRef();
		BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( selectedRoot, ref );
		return new BranchSpotTree( branchSpot, selectedRoot.getTimepoint(), highestTimepoint.get(), model );
	}

	@Override
	public void cancel()
	{
		logger.info( "Canceled FindLineageModulesCommand" );
	}
}
