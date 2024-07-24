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
import org.mastodon.mamut.classification.multiproject.ClassifiableProject;
import org.mastodon.mamut.classification.multiproject.ExternalProjects;
import org.mastodon.mamut.classification.util.Classification;
import org.mastodon.mamut.classification.config.CropCriteria;
import org.mastodon.mamut.classification.ui.DendrogramView;
import org.mastodon.mamut.classification.util.ClassificationUtils;
import org.mastodon.mamut.io.ProjectSaver;
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
import org.scijava.Context;
import org.scijava.prefs.PrefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
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

	private boolean showDendrogram;

	private final ExternalProjects externalProjects;

	private boolean addTagSetToExternalProjects;

	private boolean running = false;

	/**
	 * Create a new controller for classifying lineage trees.
	 * @param referenceProjectModel the reference project model
	 */
	public ClassifyLineagesController( final ProjectModel referenceProjectModel )
	{
		this( referenceProjectModel, null, null );
	}

	/**
	 * Create a new controller for classifying lineage trees.
	 * @param referenceProjectModel the reference project model
	 * @param prefs the preference service
	 * @param context the SciJava context
	 */
	public ClassifyLineagesController( final ProjectModel referenceProjectModel, final PrefService prefs, final Context context )
	{
		this.referenceProjectModel = referenceProjectModel;
		this.referenceModel = referenceProjectModel.getModel();
		this.prefs = prefs;
		this.externalProjects = new ExternalProjects( context );
	}

	/**
	 * Create a new tag set based on the current settings of the controller.
	 */
	public String createTagSet()
	{
		if ( running )
			return null;
		if ( !isValidParams() )
			throw new IllegalArgumentException( "Invalid parameters settings." );
		try
		{
			running = true;
			return runClassification();
		}
		finally
		{
			running = false;
		}
	}

	private String runClassification()
	{
		ReentrantReadWriteLock.ReadLock lock = referenceModel.getGraph().getLock().readLock();
		lock.lock();
		String createdTagSetName;
		try
		{
			Pair< List< ClassifiableProject >, double[][] > rootsAndDistances = getRootsAndDistanceMatrix();
			List< ClassifiableProject > rootsMatrix = rootsAndDistances.getLeft();
			double[][] distances = rootsAndDistances.getRight();
			ClassifiableProject referenceProject = rootsMatrix.get( 0 );
			Classification< BranchSpotTree > classification = classifyLineageTrees( referenceProject.getTrees(), distances );
			List< Pair< String, Integer > > tagsAndColors = createTagsAndColors( classification );
			Function< BranchSpotTree, BranchSpot > branchSpotProvider = BranchSpotTree::getBranchSpot;
			createdTagSetName = applyClassification( classification, tagsAndColors, referenceModel, branchSpotProvider );
			if ( addTagSetToExternalProjects && rootsMatrix.size() > 1 )
				classifyExternalProjects( rootsMatrix, distances, tagsAndColors );
			if ( showDendrogram )
				showDendrogram( classification );
		}
		finally
		{
			lock.unlock();
		}
		return createdTagSetName;
	}

	private void classifyExternalProjects( final List< ClassifiableProject > rootsMatrix,
			final double[][] distances, final List< Pair< String, Integer > > tagsAndColors )
	{
		Function< BranchSpotTree, BranchSpot > branchSpotProvider;
		for ( int i = 1; i < rootsMatrix.size(); i++ ) // NB: start at 1 to skip reference project
		{
			ClassifiableProject project = rootsMatrix.get( i );
			Classification< BranchSpotTree > classification = classifyLineageTrees( project.getTrees(), distances );
			ProjectModel projectModel = project.getProjectModel();
			Model model = projectModel.getModel();
			File file = project.getFile();
			branchSpotProvider = branchSpotTree -> model.getBranchGraph().vertices().stream()
					.filter( ( branchSpot -> branchSpot.getFirstLabel().equals( branchSpotTree.getName() ) ) )
					.findFirst().orElse( null );
			applyClassification( classification, tagsAndColors, model, branchSpotProvider );
			try
			{
				ProjectSaver.saveProject( file, projectModel );
			}
			catch ( IOException e )
			{
				logger.warn( "Could not save tag set of project {} to file {}. Message: {}", projectModel.getProjectName(),
						file.getAbsolutePath(), e.getMessage() );
			}
		}
	}

	private Pair< List< ClassifiableProject >, double[][] > getRootsAndDistanceMatrix()
	{
		List< BranchSpotTree > roots = getRoots();
		ClassifiableProject referenceProject = new ClassifiableProject( null, referenceProjectModel, roots );
		if ( externalProjects.isEmpty() )
		{
			double[][] distances = ClassificationUtils.getDistanceMatrix( roots, similarityMeasure );
			return Pair.of( Collections.singletonList( referenceProject ), distances );
		}

		List< String > commonRootNames = findCommonRootNames();
		List< ClassifiableProject > projects = new ArrayList<>();

		keepCommonRootsAndSort( roots, commonRootNames );
		projects.add( referenceProject );
		for ( Map.Entry< File, ProjectModel > project : externalProjects.getProjects() )
		{
			List< BranchSpotTree > externalRoots = getRoots( project.getValue() );
			keepCommonRootsAndSort( externalRoots, commonRootNames );
			projects.add( new ClassifiableProject( project.getKey(), project.getValue(), externalRoots ) );
		}
		List< List< BranchSpotTree > > treeMatrix = projects.stream().map( ClassifiableProject::getTrees ).collect( Collectors.toList() );
		return Pair.of( projects, ClassificationUtils.getAverageDistanceMatrix( treeMatrix, similarityMeasure ) );
	}

	private List< String > findCommonRootNames()
	{
		Set< String > commonRootNames = extractRootNamesFromProjectModel( referenceProjectModel );
		for ( ProjectModel projectModel : externalProjects.getProjectModels() )
		{
			Set< String > rootNames = extractRootNamesFromProjectModel( projectModel );
			commonRootNames.retainAll( rootNames );
		}
		List< String > commonRootNamesList = new ArrayList<>( commonRootNames );
		commonRootNamesList.sort( String::compareTo );

		if ( logger.isDebugEnabled() )
		{
			logger.info( "Found {} common root names in {} projects.", commonRootNames.size(), externalProjects.size() + 1 );
			String names = commonRootNames.stream().map( Object::toString ).collect( Collectors.joining( "," ) );
			logger.debug( "Common root names are: {}", names );
		}

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

	private void showDendrogram( final Classification< BranchSpotTree > classification )
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

	private List< Pair< String, Integer > > createTagsAndColors( final Classification< BranchSpotTree > classification )
	{

		List< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		List< Classification.ObjectClassification< BranchSpotTree > > objectClassifications = classification.getObjectClassifications();
		for ( int i = 0; i < objectClassifications.size(); i++ )
		{
			Classification.ObjectClassification< BranchSpotTree > objectClassification = objectClassifications.get( i );
			tagsAndColors.add( Pair.of( "Class " + ( i + 1 ), objectClassification.getColor() ) );
		}
		return tagsAndColors;
	}

	private String applyClassification( final Classification< BranchSpotTree > classification,
			final List< Pair< String, Integer > > tagsAndColors, final Model model,
			final Function< BranchSpotTree, BranchSpot > branchSpotProvider )
	{
		String tagSetName = getTagSetName();
		List< Classification.ObjectClassification< BranchSpotTree > > objectClassifications = classification.getObjectClassifications();
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, tagSetName, tagsAndColors );
		for ( int i = 0; i < objectClassifications.size(); i++ )
		{
			Classification.ObjectClassification< BranchSpotTree > objectClassification = objectClassifications.get( i );
			Set< BranchSpotTree > trees = objectClassification.getObjects();
			logger.debug( "Applying tag set for class {}, which has {} trees", i, trees.size() );
			TagSetStructure.Tag tag = tagSet.getTags().get( i );
			for ( BranchSpotTree tree : trees )
			{
				BranchSpot rootBranchSpot = branchSpotProvider.apply( tree );
				Spot rootSpot = model.getBranchGraph().getFirstLinkedVertex( rootBranchSpot, model.getGraph().vertexRef() );
				if ( rootSpot == null )
					continue;
				ModelGraph modelGraph = model.getGraph();
				DepthFirstIterator< Spot, Link > iterator = new DepthFirstIterator<>( rootSpot, modelGraph );
				iterator.forEachRemaining( spot -> {
					if ( spot.getTimepoint() < tree.getStartTimepoint() )
						return;
					if ( spot.getTimepoint() > tree.getEndTimepoint() )
						return;
					TagSetUtils.tagSpotAndIncomingEdges( model, spot, tagSet, tag );
				} );
			}
		}
		return tagSetName;
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

	public void setShowDendrogram( final boolean showDendrogram )
	{
		this.showDendrogram = showDendrogram;
	}

	public void setExternalProjects( final File[] projects, final boolean addTagSetToExternalProjects )
	{
		this.addTagSetToExternalProjects = addTagSetToExternalProjects;
		externalProjects.setProjects( projects );
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

		int roots = findCommonRootNames().size();
		if ( numberOfClasses > roots )
		{
			String message =
					"Number of classes (" + numberOfClasses + ") must not be greater than the number of valid roots (" + roots + ").";
			feedback.add( message );
			logger.debug( message );
		}
		if ( cropCriterion.equals( CropCriteria.NUMBER_OF_SPOTS ) )
			feedback.addAll( checkNumberOfSpots() );
		feedback.addAll( externalProjects.getFailingProjectMessages() );
		return feedback;
	}

	public boolean isValidParams()
	{
		return getFeedback().isEmpty();
	}

	private String getTagSetName()
	{
		String prefix = externalProjects.isEmpty() ? "" : "Average ";
		return prefix + "Classification"
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

	private List< String > checkNumberOfSpots()
	{
		List< String > feedback = new ArrayList<>();
		Set< ProjectModel > allModels = new HashSet<>( Collections.singletonList( referenceProjectModel ) );
		allModels.addAll( externalProjects.getProjectModels() );
		for ( ProjectModel projectModel : allModels )
		{
			Model model = projectModel.getModel();
			String projectName = projectModel.getProjectName();
			try
			{
				LineageTreeUtils.getFirstTimepointWithNSpots( model, cropStart );
			}
			catch ( NoSuchElementException e )
			{
				String message = projectName + ", crop start: " + e.getMessage();
				feedback.add( message );
				logger.debug( message );
			}
			try
			{
				LineageTreeUtils.getFirstTimepointWithNSpots( model, cropEnd );
			}
			catch ( NoSuchElementException e )
			{
				String message = projectName + ", crop end: " + e.getMessage();
				feedback.add( message );
				logger.debug( message );
			}
		}
		return feedback;
	}

	public void close()
	{
		externalProjects.close();
	}

	public String getProjectName()
	{
		return referenceProjectModel.getProjectName();
	}
}
