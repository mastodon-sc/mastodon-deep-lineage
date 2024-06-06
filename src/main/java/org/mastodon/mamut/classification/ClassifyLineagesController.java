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
package org.mastodon.mamut.classification;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.classification.config.ClusteringMethod;
import org.mastodon.mamut.classification.config.SimilarityMeasure;
import org.mastodon.mamut.classification.util.Classification;
import org.mastodon.mamut.classification.config.CropCriteria;
import org.mastodon.mamut.classification.ui.DendrogramView;
import org.mastodon.mamut.classification.util.ClassificationUtils;
import org.mastodon.mamut.classification.util.ProjectAccessor;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.classification.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.classification.treesimilarity.tree.TreeUtils;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.TagSetUtils;
import org.scijava.prefs.PrefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Controller class that serves as bridge between the classification algorithm and the user interface.
 * It is responsible for setting the parameters of the classification algorithm, running the algorithm and displaying the results.
 */
public class ClassifyLineagesController
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model referenceModel;

	private final ProjectModel referenceProjectModel;

	private final PrefService prefs;

	private SimilarityMeasure similarityMeasure = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE;

	private ClusteringMethod clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE;

	private CropCriteria cropCriterion;

	private int cropStart;

	private int cropEnd;

	private int numberOfClasses;

	private int minCellDivisions;

	private final List< File > externalProjects;

	private boolean showDendrogram;

	private Classification< BranchSpotTree > classification;

	private boolean running = false;

	/**
	 * Create a new controller for classifying lineage trees.
	 * @param referenceProjectModel the reference project model
	 */
	public ClassifyLineagesController( final ProjectModel referenceProjectModel )
	{
		this( referenceProjectModel, null );
	}

	/**
	 * Create a new controller for classifying lineage trees.
	 * @param referenceProjectModel the reference project model
	 * @param prefs the preference service
	 */
	public ClassifyLineagesController( final ProjectModel referenceProjectModel, final PrefService prefs )
	{
		this.referenceProjectModel = referenceProjectModel;
		this.referenceModel = referenceProjectModel.getModel();
		this.prefs = prefs;
		this.externalProjects = new ArrayList<>();
	}

	/**
	 * Create a new tag set based on the current settings of the controller.
	 */
	public void createTagSet()
	{
		if ( running )
			return;
		if ( !isValidParams() )
			throw new IllegalArgumentException( "Invalid parameters settings." );
		ReentrantReadWriteLock.WriteLock writeLock = referenceModel.getGraph().getLock().writeLock();
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

	public ProjectModel getProjectModel()
	{
		return referenceProjectModel;
	}

	private void runClassification()
	{
		Pair< List< BranchSpotTree >, double[][] > rootsAndDistances = getRootsAndDistanceMatrix();
		List< BranchSpotTree > roots = rootsAndDistances.getLeft();
		double[][] distances = rootsAndDistances.getRight();
		classification = classifyLineageTrees( roots, distances );
		List< Pair< String, Integer > > tagsAndColors = createTagsAndColors();
		applyClassification( classification, tagsAndColors, referenceModel );
		if ( showDendrogram )
			showDendrogram();
	}

	private Pair< List< BranchSpotTree >, double[][] > getRootsAndDistanceMatrix()
	{
		List< BranchSpotTree > roots = getRoots();
		if ( externalProjects.isEmpty() )
		{
			double[][] distances = ClassificationUtils.getDistanceMatrix( roots, similarityMeasure );
			return Pair.of( roots, distances );
		}

		try (ProjectAccessor projectAccessor = new ProjectAccessor( externalProjects, referenceProjectModel.getContext() ))
		{
			List< ProjectModel > externalProjectModels = projectAccessor.getProjectModels();
			List< String > commonRootNames = findCommonRootNames( externalProjectModels );
			if ( logger.isDebugEnabled() )
			{
				logger.info( "Found {} common root names in {} projects.", commonRootNames.size(), externalProjectModels.size() + 1 );
				String names = commonRootNames.stream().map( Object::toString ).collect( Collectors.joining( "," ) );
				logger.debug( "Common root names are: {}", names );
			}
			List< List< BranchSpotTree > > treeMatrix = new ArrayList<>();

			keepCommonRootsAndSort( roots, commonRootNames );
			treeMatrix.add( roots );
			for ( ProjectModel projectModel : externalProjectModels )
			{
				List< BranchSpotTree > externalRoots = getRoots( projectModel );
				keepCommonRootsAndSort( externalRoots, commonRootNames );
				treeMatrix.add( externalRoots );
			}
			return Pair.of( roots, ClassificationUtils.getAverageDistanceMatrix( treeMatrix, similarityMeasure ) );
		}
	}

	private List< String > findCommonRootNames( final List< ProjectModel > externalProjectModels )
	{
		Set< String > commonRootNames = extractRootNamesFromProjectModel( referenceProjectModel );
		for ( ProjectModel projectModel : externalProjectModels )
		{
			Set< String > rootNames = extractRootNamesFromProjectModel( projectModel );
			commonRootNames.retainAll( rootNames );
		}
		List< String > commonRootNamesList = new ArrayList<>( commonRootNames );
		commonRootNamesList.sort( String::compareTo );
		return commonRootNamesList;
	}

	private Set< String > extractRootNamesFromProjectModel( final ProjectModel projectModel )
	{
		List< BranchSpotTree > roots = getRoots( projectModel );
		Set< String > rootNames = new HashSet<>();
		roots.forEach( root -> rootNames.add( root.getName() ) );
		return rootNames;
	}

	private static void keepCommonRootsAndSort( final List< BranchSpotTree > roots, final List< String > commonRootNames )
	{
		roots.removeIf( root -> !commonRootNames.contains( root.getName() ) );
		roots.sort( Comparator.comparing( BranchSpotTree::getName ) );
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
		DendrogramView< BranchSpotTree > dendrogramView = new DendrogramView<>( classification, header, referenceModel, prefs );
		dendrogramView.show();
	}

	private Classification< BranchSpotTree > classifyLineageTrees( final List< BranchSpotTree > roots, final double[][] distances )
	{
		if ( roots.size() != distances.length )
			throw new IllegalArgumentException(
					"Number of roots (" + roots.size() + ") and size of distance matrix (" + distances.length + "x"
							+ distances[ 0 ].length + ") do not match." );
		BranchSpotTree[] rootBranchSpots = roots.toArray( new BranchSpotTree[ 0 ] );
		Classification< BranchSpotTree > result = ClassificationUtils.getClassificationByClassCount( rootBranchSpots, distances,
				clusteringMethod.getLinkageStrategy(), numberOfClasses );
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

	private void applyClassification( final Classification< BranchSpotTree > classification,
			final List< Pair< String, Integer > > tagsAndColors, final Model model )
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
		return getRoots( referenceProjectModel );
	}

	private List< BranchSpotTree > getRoots( final ProjectModel projectModel )
	{
		Model model = projectModel.getModel();
		if ( !projectModel.getBranchGraphSync().isUptodate() )
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
				BranchSpotTree tree = new BranchSpotTree( rootBranchSpot, cropStartTime, cropEndTime, model );
				int minTreeSize = 2 * minCellDivisions + 1;
				if ( TreeUtils.size( tree ) < minTreeSize )
					continue;
				trees.add( tree );
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

	public void setComputeParams( final SimilarityMeasure similarityMeasure, final ClusteringMethod clusteringMethod,
			final int numberOfClasses )
	{
		this.similarityMeasure = similarityMeasure;
		this.clusteringMethod = clusteringMethod;
		this.numberOfClasses = numberOfClasses;
	}

	public void setVisualisationParams( final boolean showDendrogram )
	{
		this.showDendrogram = showDendrogram;
	}

	public void setExternalProjects( final File[] projects )
	{
		externalProjects.clear();
		if ( projects == null )
			return;
		externalProjects.addAll( Arrays.asList( projects ) );
	}

	public List< String > getFeedback()
	{
		List< String > feedback = new ArrayList<>();
		if ( cropStart >= cropEnd )
		{
			String message = "Crop start (" + cropStart + ") must be smaller than crop end (" + cropEnd + ").";
			feedback.add( message );
			logger.debug( message );
		}

		int roots = getRoots().size();
		if ( numberOfClasses > roots )
		{
			String message =
					"Number of classes (" + numberOfClasses + ") must not be greater than the number of valid roots (" + roots + ").";
			feedback.add( message );
			logger.debug( message );
		}
		if ( cropCriterion.equals( CropCriteria.NUMBER_OF_SPOTS ) )
		{
			try
			{
				LineageTreeUtils.getFirstTimepointWithNSpots( referenceModel, cropStart );
			}
			catch ( NoSuchElementException e )
			{
				String message = e.getMessage();
				feedback.add( message );
				logger.debug( message );
			}
			try
			{
				LineageTreeUtils.getFirstTimepointWithNSpots( referenceModel, cropEnd );
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
