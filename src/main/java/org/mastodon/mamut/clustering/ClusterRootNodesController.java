/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.clustering;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.ui.DendrogramView;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClusterRootNodesController
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private final BranchGraphSynchronizer synchronizer;

	private SimilarityMeasure similarityMeasure = SimilarityMeasure.NORMALIZED_DIFFERENCE;

	private ClusteringMethod clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE;

	private CropCriteria cropCriterion;

	private int cropStart;

	private int cropEnd;

	private int numberOfClasses;

	private int minCellDivisions;

	private Classification< BranchSpotTree > classification;

	private boolean running = false;

	private boolean showDendrogram = true;

	public ClusterRootNodesController( final Model model, final BranchGraphSynchronizer synchronizer )
	{
		this.model = model;
		this.synchronizer = synchronizer;
	}

	public void createTagSet()
	{
		if ( running )
			return;
		if ( !isValidParams() )
			throw new IllegalArgumentException( "Invalid parameters settings." );
		ReentrantReadWriteLock.WriteLock writeLock = model.getGraph().getLock().writeLock();
		writeLock.lock();
		try
		{
			running = true;
			runClassification();
		}
		finally
		{
			writeLock.unlock();
			running = false;
		}
	}

	private void runClassification()
	{
		List< BranchSpotTree > roots = getRoots();
		classification = classifyLineageTrees( roots );
		List< Pair< String, Integer > > tagsAndColors = createTagsAndColors();
		applyClassification( classification, tagsAndColors );
		if ( showDendrogram )
			showDendrogram();
	}

	String getParameters()
	{
		StringJoiner joiner = new StringJoiner( ", " );
		joiner.add( "Crop criterion: " + cropCriterion.getName() );
		joiner.add( "Crop start: " + cropStart );
		joiner.add( "Crop end: " + cropEnd );
		joiner.add( "Number of classes: " + numberOfClasses );
		joiner.add( "Minimum cell divisions: " + minCellDivisions );
		joiner.add( "Similarity measure: " + similarityMeasure.getName() );
		joiner.add( "Clustering method: " + clusteringMethod.getName() );
		joiner.add( "Resulting lineage trees: " + getRoots().size() );
		return joiner.toString();
	}

	private void showDendrogram()
	{

		String header = "<html><body>Dendrogram of hierarchical clustering of lineages<br>" + getParameters() + "</body></html>";
		DendrogramView< BranchSpotTree > dendrogramView = new DendrogramView<>( classification, header );
		dendrogramView.show();
	}

	private Classification< BranchSpotTree > classifyLineageTrees( List< BranchSpotTree > roots )
	{
		logger.debug( "Start computing similarity matrix for {} lineage trees.", roots.size() );
		double[][] distances = ClusterUtils.getDistanceMatrix( new ArrayList<>( roots ), similarityMeasure );
		logger.debug(
				"Finished computing similarity matrix. Shape: {}x{}={} entries.", distances.length, distances[ 0 ].length,
				distances.length * distances[ 0 ].length
		);
		BranchSpotTree[] rootBranchSpots = roots.toArray( new BranchSpotTree[ 0 ] );
		Classification< BranchSpotTree > result = ClusterUtils.getClassificationByClassCount( rootBranchSpots, distances,
				clusteringMethod.getLinkageStrategy(), numberOfClasses
		);
		logger.debug(
				"Finished hierarchical clustering. Created {} object classifications.", result.getObjectClassifications().size() );
		return result;
	}

	private List< Pair< String, Integer > > createTagsAndColors()
	{

		List< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		Set< Classification.ObjectClassification< BranchSpotTree > > objectClassifications = classification.getObjectClassifications();
		int i = 0;
		for ( Classification.ObjectClassification< BranchSpotTree > objectClassification : objectClassifications )
		{
			tagsAndColors.add( Pair.of( "Class " + ( i + 1 ), objectClassification.getColor() ) );
			i++;
		}
		return tagsAndColors;
	}

	private void applyClassification( Classification< BranchSpotTree > classification, List< Pair< String, Integer > > tagsAndColors )
	{
		Set< Classification.ObjectClassification< BranchSpotTree > > objectClassifications = classification.getObjectClassifications();
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, getTagSetName(), tagsAndColors );
		int i = 0;
		for ( Classification.ObjectClassification< BranchSpotTree > objectClassification : objectClassifications )
		{
			Set< BranchSpotTree > trees = objectClassification.getObjects();
			logger.info( "Class {} has {} trees", i, trees.size() );
			TagSetStructure.Tag tag = tagSet.getTags().get( i );
			for ( BranchSpotTree tree : trees )
			{
				Spot rootSpot = model.getBranchGraph().getFirstLinkedVertex( tree.getBranchSpot(), model.getGraph().vertexRef() );
				ModelGraph modelGraph = model.getGraph();
				DepthFirstIterator< Spot, Link > iterator = new DepthFirstIterator<>( rootSpot, modelGraph );
				iterator.forEachRemaining( spot -> {
					if ( spot.getTimepoint() < cropStart )
						return;
					if ( spot.getTimepoint() > cropEnd )
						return;
					TagSetUtils.tagSpotAndIncomingEdges( model, spot, tagSet, tag );
				} );
			}
			i++;
		}
	}

	private List< BranchSpotTree > getRoots()
	{
		if ( !synchronizer.isUptodate() )
			model.getBranchGraph().graphRebuilt();

		int cropStartTime = cropStart;
		int cropEndTime = cropEnd;
		if ( cropCriterion.equals( CropCriteria.NUMBER_OF_SPOTS ) )
		{
			try
			{
				cropStartTime = LineageTreeUtils.getFirstTimepointWithNSpots( model, cropStart );
				cropEndTime = LineageTreeUtils.getFirstTimepointWithNSpots( model, cropEnd );
				logger.debug( "Crop time, start: {}, end: {}", cropStartTime, cropEndTime );
			}
			catch ( NoSuchElementException e )
			{
				return Collections.emptyList();
			}
		}
		RefSet< Spot > roots = LineageTreeUtils.getRoots( model.getGraph(), cropStartTime );
		List< BranchSpotTree > trees = new ArrayList<>();
		for ( Spot root : roots )
		{
			BranchSpot rootBranchSpot = model.getBranchGraph().getBranchVertex( root, model.getBranchGraph().vertexRef() );
			try
			{
				BranchSpotTree branchSpotTree = new BranchSpotTree( rootBranchSpot, cropEndTime );
				int minTreeSize = 2 * minCellDivisions + 1;
				if ( TreeUtils.size( branchSpotTree ) < minTreeSize )
					continue;
				trees.add( branchSpotTree );
			}
			catch ( IllegalArgumentException e )
			{
				logger.trace( "Could not create tree for root {}. Message: {}", root, e.getMessage() );
			}
		}
		return trees;
	}

	public void setInputParams( final CropCriteria cropCriterion, final int cropStart, final int cropEnd, final int minCellDivisions )
	{
		this.cropCriterion = cropCriterion;
		this.cropStart = cropStart;
		this.cropEnd = cropEnd;
		this.minCellDivisions = minCellDivisions;
		logger.debug( "Crop criterion {}, start: {}, end: {}", cropCriterion.getName(), this.cropStart, this.cropEnd );
	}

	public void setComputeParams( SimilarityMeasure similarityMeasure, ClusteringMethod clusteringMethod, int numberOfClasses )
	{
		this.similarityMeasure = similarityMeasure;
		this.clusteringMethod = clusteringMethod;
		this.numberOfClasses = numberOfClasses;
	}

	public void setShowDendrogram( boolean showDendrogram )
	{
		this.showDendrogram = showDendrogram;
	}

	public List< String > getFeedback()
	{
		List< String > feedback = new ArrayList<>();
		if ( cropStart >= cropEnd )
		{
			String message = "Crop start (" + cropStart + ") must be smaller than crop end (" + cropEnd + ")";
			feedback.add( message );
			logger.debug( message );
		}

		int roots = getRoots().size();
		if ( numberOfClasses >= roots )
		{
			String message = "Number of classes (" + numberOfClasses + ") must be smaller than number of valid roots (" + roots + ")";
			feedback.add( message );
			logger.debug( message );
		}
		if ( cropCriterion.equals( CropCriteria.NUMBER_OF_SPOTS ) )
		{
			try
			{
				LineageTreeUtils.getFirstTimepointWithNSpots( model, cropStart );
			}
			catch ( NoSuchElementException e )
			{
				String message = e.getMessage();
				feedback.add( message );
				logger.debug( message );
			}
			try
			{
				LineageTreeUtils.getFirstTimepointWithNSpots( model, cropEnd );
			}
			catch ( NoSuchElementException e )
			{
				String message = e.getMessage();
				feedback.add( message );
				logger.debug( message );
			}
		}
		return feedback;
	}

	public boolean isValidParams()
	{
		return getFeedback().isEmpty();
	}

	private String getTagSetName()
	{
		return "Classification"
				+ " ("
				+ cropCriterion.getNameShort()
				+ ": "
				+ cropStart
				+ "-"
				+ cropEnd
				+ ", classes: "
				+ numberOfClasses
				+ ", min. div: "
				+ minCellDivisions
				+ ") ";
	}
}
