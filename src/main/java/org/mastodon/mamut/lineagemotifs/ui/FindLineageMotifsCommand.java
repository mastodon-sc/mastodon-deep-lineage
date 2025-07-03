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
package org.mastodon.mamut.lineagemotifs.ui;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.ui.Notification;
import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.lineagemotifs.util.InvalidLineageMotifSelection;
import org.mastodon.mamut.lineagemotifs.util.LineageMotifsUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.util.ColorRGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Plugin( type = Command.class, label = "Find similar lineage modules", visible = false )
public class FindLineageMotifsCommand extends CancelableImpl implements Command
{
	private static final String TAG_SET_NAME = "Lineage Modules similar to ";

	private static final float WIDTH = 15f;

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@SuppressWarnings( "unused" )
	@Parameter
	private ProjectModel projectModel;

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Find similar lineage modules</h1>\n"
			+ "<p>This commands finds a specifiable number of lineage modules that are similar to the currently selected module and assigns tags to them.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@Parameter( label = "Number of similar lineage", min = "1", max = "100", stepSize = "1" )
	private int numberOfSimilarLineage = 10;

	@Parameter( label = "Color" )
	private ColorRGB color = new ColorRGB( "red" );

	@Parameter
	private ThreadService threadService;

	/**
	 * This method is executed whenever a parameter changes
	 */
	@Override
	public void run()
	{
		threadService.run( () -> findModules( true ) );
	}

	private void findModules( boolean runOnBranchGraph )
	{
		Model model = projectModel.getModel();
		if ( runOnBranchGraph )
			projectModel.getBranchGraphSync().sync();
		Spot spotRef = model.getGraph().vertexRef();
		BranchSpot branchSpotRef = model.getBranchGraph().vertexRef();
		try
		{
			BranchSpotTree lineageMotif = LineageMotifsUtils.getSelectedMotif( model, projectModel.getSelectionModel() );
			String lineageModuleName = LineageMotifsUtils.getLineageMotifName( model, lineageMotif );
			List< Pair< BranchSpotTree, Double > > similarModules =
					LineageMotifsUtils.getMostSimilarMotifs( model, lineageMotif, numberOfSimilarLineage, spotRef, branchSpotRef,
							!runOnBranchGraph );
			int numberOfDivisions = LineageMotifsUtils.getNumberOfDivisions( lineageMotif, model );
			String tagSetName = TAG_SET_NAME + lineageModuleName + " (" + numberOfDivisions + " divisions)";
			LineageMotifsUtils.tagLineageMotifs( model, tagSetName, similarModules, new Color( color.getARGB() ) );
		}
		catch ( InvalidLineageMotifSelection e )
		{
			logger.warn( e.getLogMessage() );
			Notification.showError( e.getUiTitle(), e.getUiMessage() );
		}
		catch ( Exception e )
		{
			logger.error( "Error while finding similar lineage modules.", e );
			Notification.showError( "Error", "An error occurred while finding similar lineage modules: " + e.getMessage() );
		}
		finally
		{
			model.getGraph().releaseRef( spotRef );
			model.getBranchGraph().releaseRef( branchSpotRef );
		}
	}
}
