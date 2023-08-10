package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.ClusterRootNodesController;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.scijava.ItemVisibility;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

import static org.mastodon.mamut.clustering.ClusterRootNodesController.ComputeParams;
import static org.mastodon.mamut.clustering.ClusterRootNodesController.InputParams;

@Plugin(type = InteractiveCommand.class, visible = false, label = "Classification of Lineage Trees", initializer = "update")
public class ClusterRootNodesView extends InteractiveCommand
{

	@SuppressWarnings("unused")
	@Parameter
	private ClusterRootNodesController controller;

	@SuppressWarnings("all")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false)
	private String documentation = "<html>\n"
			+ "<body width=15cm align=left>\n"
			+ "<h1>Classification of Lineage Trees</h1>\n"
			+ "<p>This plugin is capable in grouping similar lineage trees together. This is done by creating a tag set an assigning subtrees that are similar to each other with the same tag.</p>\n"
			+ "<p>The similarity between two subtrees is computed based on the Zhang edit distance for unordered trees <a href=\"https://doi.org/10.1007/BF01975866\">(Zhang, K. Algorithmica 15, 205–222, 1996)</a>. The similarity measure uses the attribute the cell lifetime, which is computed as a difference of timepoints between to subsequent divisions. It is possible to apply the <i>absolute difference</i>, <i>average difference</i> or the <i>normalized difference</i> of cell lifetimes.</p>\n"
			+ "<p>The similarity is computed between all possible combinations of subtrees leading to a two-dimensional similarity matrix. This matrix is then used to perform a <a href=\"https://en.wikipedia.org/wiki/Hierarchical_clustering\">agglomerative hierarchical clustering</a> into a specifiable number of classes. For the clustering three different <a href=\"https://en.wikipedia.org/wiki/Hierarchical_clustering#Cluster_Linkage\">linkage methods</a> can be chosen.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@SuppressWarnings("unused")
	@Parameter(label = "Crop criterion", choices = { "Timepoint", "Number of cells" })
	private String cropCriterion;

	@SuppressWarnings("unused")
	@Parameter(label = "Crop start", min = "0", callback = "update")
	private int start;

	@SuppressWarnings("unused")
	@Parameter(label = "Crop end", min = "0", callback = "update")
	private int end;

	@SuppressWarnings("unused")
	@Parameter(label = "Number of classes", min = "1", callback = "update")
	private int numberOfClasses;

	@SuppressWarnings("unused")
	@Parameter(label = "Minimum number of cell divisions", min = "0", callback = "update")
	private int numberOfCellDivisions;

	@SuppressWarnings("unused")
	@Parameter(label = "Similarity measure", choices = { "Normalized difference", "Average difference", "Absolute difference" })
	private String similarityMeasure;

	@SuppressWarnings("unused")
	@Parameter(
			label = "Linkage strategy for hierarchical clustering", choices = { "Average linkage", "Single Linkage", "Complete Linkage" }
	)
	private String clusteringMethod;

	@SuppressWarnings("unused")
	@Parameter(label = "Feature", choices = "Cell lifetime")
	private String cellLifeTime;
	// NB: dynamic choices: https://github.com/imagej/tutorials/blob/c78764438d774295d00fc8a4273e4c4f25c8ad46/maven-projects/dynamic-commands/src/main/java/DynamicCallbacks.java

	@SuppressWarnings("unused")
	@Parameter(label = "Show dendrogram of clustering")
	private boolean showDendrogram;

	@SuppressWarnings("unused")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false, label = " ")
	private String paramFeedback;

	@SuppressWarnings("unused")
	@Parameter(visibility = ItemVisibility.MESSAGE, required = false, persist = false, label = " ")
	private String computeFeedback;

	@SuppressWarnings("unused")
	@Parameter(label = "Create tag set", callback = "createTagSet", persist = false)
	private Button createTagSet;

	/**
	 * This method is executed whenever a parameter changes
	 */
	@Override
	public void run()
	{
		// NB: not implemented. Update method is called via callback on parameter change and in initializer.
	}

	@SuppressWarnings("unused")
	private void update()
	{
		InputParams inputParams = null;
		if ( cropCriterion != null )
			inputParams = new InputParams( CropCriteria.getByName( cropCriterion ), start, end, numberOfCellDivisions );
		ComputeParams computeParams = null;
		if ( similarityMeasure != null && clusteringMethod != null )
			computeParams =
					new ComputeParams( SimilarityMeasure.getByName( similarityMeasure ), ClusteringMethod.getByName( clusteringMethod ),
							numberOfClasses
					);
		if ( inputParams != null && computeParams != null )
			controller.setParams( inputParams, computeParams, showDendrogram );

		if ( controller.isValidParams() )
			paramFeedback = "<html><body><font color=\"green\">Parameters are valid.</font></body></html>";
		else
		{
			paramFeedback = String.join( "<p>", controller.getFeedback() );
			paramFeedback = "<html><body><font color=\"red\">" + paramFeedback + "\n<p>Please change settings.</font></body></html>";
		}
	}

	@SuppressWarnings("unused")
	private void createTagSet()
	{
		if ( controller.isValidParams() )
		{
			try
			{
				controller.createTagSet();
				computeFeedback = "<html><body><font color=\"green\">Tag set created.</font></body></html>";
			}
			catch ( IllegalArgumentException e )
			{
				computeFeedback = "<html><body><font color=\"red\">" + e.getMessage() + "</font></body></html>";
			}
		}
	}
}
