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
package org.mastodon.mamut.classification.ui;

import org.mastodon.mamut.classification.config.ClusteringMethod;
import org.mastodon.mamut.classification.config.HasName;
import org.mastodon.mamut.classification.config.SimilarityMeasure;
import org.mastodon.mamut.classification.ClassifyLineagesController;
import org.mastodon.mamut.classification.config.CropCriteria;
import org.mastodon.mamut.io.project.MamutProject;
import org.scijava.ItemVisibility;
import org.scijava.command.InteractiveCommand;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Plugin( type = InteractiveCommand.class, visible = false, label = "Classification of Lineage Trees", initializer = "init" )
public class ClassifyLineagesCommand extends InteractiveCommand
{

	private static final float WIDTH = 18.5f;

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@SuppressWarnings("unused")
	@Parameter
	private ClassifyLineagesController controller;

	@SuppressWarnings("all")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false)
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Classification of Lineage Trees</h1>\n"
			+ "<p>This command is capable of grouping similar lineage trees together, i.e. trees that share a similar cell division pattern. This is realised by creating a new tag set and assigning the same tag to lineage trees that are similar to each other.</p>\n"
			+ "<p>Refer to the <a href=\"https://github.com/mastodon-sc/mastodon-deep-lineage/tree/master/doc/classification\">documentation</a> to learn how the similarity is computed.</p>"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings("all")
	@Parameter( label = "Crop criterion", initializer = "initCropCriterionChoices", callback = "update" )
	private String cropCriterion = CropCriteria.NUMBER_OF_SPOTS.getName();

	@SuppressWarnings("unused")
	@Parameter(label = "Crop start", min = "0", callback = "update")
	private int start;

	@SuppressWarnings("unused")
	@Parameter(label = "Crop end", min = "0", callback = "update")
	private int end;

	@SuppressWarnings("unused")
	@Parameter(label = "Number of classes", min = "2", callback = "update")
	private int numberOfClasses;

	@SuppressWarnings("unused")
	@Parameter( label = "<html><body>Minimum number<br>of cell divisions</body></html>", min = "0", description = "Only include lineage trees with at least the number of divisions specified here.", callback = "update" )
	private int numberOfCellDivisions;

	@SuppressWarnings("all")
	@Parameter( label = "Similarity measure", initializer = "initSimilarityMeasureChoices", callback = "update" )
	public String similarityMeasure = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE.getName();

	@SuppressWarnings("all")
	@Parameter( label = "<html><body>Linkage strategy for<br>hierarchical clustering</body></html>", initializer = "initClusteringMethodChoices", callback = "update" )
	private String clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE.getName();

	@SuppressWarnings("unused")
	@Parameter(label = "Feature", choices = "Branch duration", callback = "update")
	private String branchDuration;

	@SuppressWarnings( "unused" )
	@Parameter( label = "<html><body>List of projects<br>(Drag & Drop supported)</body></html>", style = "files,extensions:mastodon", persist = false, callback = "update", initializer = "initProjectsDefault" )
	private File[] projects = new File[ 0 ];

	@SuppressWarnings("unused")
	@Parameter( label = "<html><body>Show dendrogram<br>of clustering</body></html>", callback = "update" )
	private boolean showDendrogram = true;

	@SuppressWarnings("unused")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false, label = " ")
	private String paramFeedback;

	@SuppressWarnings("unused")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false, label = " ")
	private String computeFeedback;

	@SuppressWarnings("unused")
	@Parameter( label = "Classify lineage trees", callback = "createTagSet" )
	private Button createTagSet;

	/**
	 * This method is executed whenever a parameter changes
	 */
	@Override
	public void run()
	{
		// NB: not implemented. Update method is called via callback on each parameter change.
	}

	@SuppressWarnings("unused")
	private void update()
	{
		updateParams();
		updateFeedback();
	}

	private void updateParams()
	{
		controller.setInputParams( CropCriteria.getByName( cropCriterion ), start, end, numberOfCellDivisions );
		controller.setComputeParams(
				SimilarityMeasure.getByName( similarityMeasure ), ClusteringMethod.getByName( clusteringMethod ), numberOfClasses );
		controller.setVisualisationParams( showDendrogram );
		controller.setExternalProjects( projects );
	}

	private void updateFeedback()
	{
		paramFeedback = "<html><body width=" + WIDTH + "cm>";
		if ( controller.isValidParams() )
			paramFeedback += "<font color=green>Parameters are valid.";
		else
			paramFeedback += "<font color=red>" + String.join( "<p>", controller.getFeedback() );
		paramFeedback += "</font></body></html>";
	}

	@SuppressWarnings("unused")
	private void createTagSet()
	{
		update();
		if ( controller.isValidParams() )
		{
			String feedback;
			String color;
			try
			{
				controller.createTagSet();
				feedback = "Classified lineage trees.<p>";
				feedback += "Tag set created.";
				color = "green";
			}
			catch ( IllegalArgumentException e )
			{
				feedback = e.getMessage();
				color = "red";
				logger.error( "Error during lineage classification: {}", e.getMessage() );
			}
			computeFeedback = "<html><body width=" + WIDTH + "cm><font color=\"" + color + "\">" + feedback + "</font></body></html>";
		}
	}

	@SuppressWarnings( "unused" )
	private void initCropCriterionChoices()
	{
		getInfo().getMutableInput( "cropCriterion", String.class ).setChoices( enumNamesAsList( CropCriteria.values() ) );
	}

	@SuppressWarnings( "unused" )
	private void initSimilarityMeasureChoices()
	{
		getInfo().getMutableInput( "similarityMeasure", String.class ).setChoices( enumNamesAsList( SimilarityMeasure.values() ) );
	}

	@SuppressWarnings( "unused" )
	private void initClusteringMethodChoices()
	{
		getInfo().getMutableInput( "clusteringMethod", String.class ).setChoices( enumNamesAsList( ClusteringMethod.values() ) );
	}

	@SuppressWarnings( "unused" )
	private void initProjectsDefault()
	{
		MamutProject project = controller.getProjectModel().getProject();
		MutableModuleItem< File[] > projectsItem = getInfo().getMutableInput( "projects", File[].class );
		if ( project != null )
			projectsItem.setChoices( Collections.singletonList( new File[] { project.getProjectRoot() } ) );
		else
			projectsItem.setChoices( Collections.emptyList() );
	}

	static List< String > enumNamesAsList( final HasName[] values )
	{
		return Arrays.stream( values ).map( HasName::getName ).collect( Collectors.toList() );
	}
}
