package org.mastodon.mamut.clustering;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.graph.algorithm.RootFinder;
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
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.ColorUtils;
import org.mastodon.util.TagSetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClusterRootNodesController
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private SimilarityMeasure similarityMeasure = SimilarityMeasure.NORMALIZED_DIFFERENCE;

	private ClusteringMethod clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE;

	private int cropStart;

	private int cropEnd;

	private int numberOfClasses;

	private int minCellDivisions;

	private Classification< BranchSpotTree > classification;

	private boolean running = false;

	private boolean showDendrogram = true;

	public ClusterRootNodesController( final Model model )
	{
		this.model = model;
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

		Collection< Pair< String, Integer > > tagsAndColors = createTagsAndColors();
		applyClassification( classification, tagsAndColors );
		if ( showDendrogram )
			showDendrogram();
	}

	private void showDendrogram()
	{
		DendrogramView< BranchSpotTree > dendrogramView =
				new DendrogramView<>(
						classification.getAlgorithmResult(), classification.getObjectMapping(), classification.getCutoff(),
						"Dendrogram of hierarchical clustering of lineages"
				);
		dendrogramView.show();
	}

	private Classification< BranchSpotTree > classifyLineageTrees( List< BranchSpotTree > roots )
	{
		double[][] distances = ClusterUtils.getDistanceMatrix( new ArrayList<>( roots ), similarityMeasure );
		BranchSpotTree[] rootBranchSpots = roots.toArray( new BranchSpotTree[ 0 ] );
		return ClusterUtils.getClassificationByClassCount( rootBranchSpots, distances,
				clusteringMethod.getLinkageStrategy(), numberOfClasses
		);
	}

	private Collection< Pair< String, Integer > > createTagsAndColors()
	{
		Collection< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		for ( int i = 0; i < numberOfClasses; i++ )
			tagsAndColors.add( Pair.of( "Class " + ( i + 1 ), ColorUtils.GLASBEY[ i + 1 ].getRGB() ) );
		return tagsAndColors;
	}

	private void applyClassification( Classification< BranchSpotTree > classification, Collection< Pair< String, Integer > > tagsAndColors )
	{
		Map< Integer, List< BranchSpotTree > > classifiedObjects = classification.getClassifiedObjects();
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "Classification", tagsAndColors );
		for ( Map.Entry< Integer, List< BranchSpotTree > > entry : classifiedObjects.entrySet() )
		{
			logger.info( "Class {} has {} trees", entry.getKey(), entry.getValue().size() );
			TagSetStructure.Tag tag = tagSet.getTags().get( entry.getKey() );
			for ( BranchSpotTree tree : entry.getValue() )
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
		}
	}

	private List< BranchSpotTree > getRoots()
	{
		model.getBranchGraph().graphRebuilt();
		Set< Spot > roots = RootFinder.getRoots( model.getGraph() );
		List< BranchSpotTree > trees = new ArrayList<>();
		for ( Spot root : roots )
		{
			BranchSpot rootBranchSpot = model.getBranchGraph().getBranchVertex( root, model.getBranchGraph().vertexRef() );
			try
			{
				BranchSpotTree branchSpotTree = new BranchSpotTree( rootBranchSpot, cropEnd );
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

	public void setParams( final InputParams inputParams, final ComputeParams computeParams, boolean showDendrogram )
	{
		CropCriteria cropCriterion = inputParams.cropCriterion;
		cropStart = inputParams.cropStart;
		cropEnd = inputParams.cropEnd;
		if ( cropCriterion.equals( CropCriteria.NUMBER_OF_CELLS ) )
		{
			logger.debug( "Crop criterion cells, crop start cells: {}, crop end cells: {}", cropStart, cropEnd );
			cropStart = LineageTreeUtils.getFirstTimepointWithNSpots( model, inputParams.cropStart );
			cropEnd = LineageTreeUtils.getFirstTimepointWithNSpots( model, inputParams.cropEnd );
		}
		logger.debug( "Crop criterion {}, start timepoint: {}, crop end timepoint: {}", cropCriterion, cropStart, cropEnd );
		minCellDivisions = inputParams.minCellDivisions;
		similarityMeasure = computeParams.similarityMeasure;
		clusteringMethod = computeParams.clusteringMethod;
		numberOfClasses = computeParams.numberOfClasses;
		this.showDendrogram = showDendrogram;
	}

	public List< String > getFeedback()
	{
		List< String > feedback = new ArrayList<>();
		if ( cropStart >= cropEnd )
		{
			String message = "Crop start (timepoint=" + cropStart + ") must be smaller than crop end (timepoint=" + cropEnd + ")";
			feedback.add( message );
			logger.debug( message );
		}

		int roots = getRoots().size();
		if ( numberOfClasses > roots )
		{
			String message = "Number of classes (" + numberOfClasses + ") must not be larger than number of valid roots (" + roots + ")";
			feedback.add( message );
			logger.debug( message );
		}

		return feedback;
	}

	public boolean isValidParams()
	{
		return getFeedback().isEmpty();
	}

	public static class InputParams
	{
		private final CropCriteria cropCriterion;

		private final int cropStart;

		private final int cropEnd;

		private final int minCellDivisions;

		public InputParams( final CropCriteria cropCriterion, final int cropStart, final int cropEnd, final int minCellDivisions )
		{
			this.cropCriterion = cropCriterion;
			this.cropStart = cropStart;
			this.cropEnd = cropEnd;
			this.minCellDivisions = minCellDivisions;
		}
	}

	public static class ComputeParams
	{
		private final SimilarityMeasure similarityMeasure;

		private final ClusteringMethod clusteringMethod;

		private final int numberOfClasses;

		public ComputeParams(
				final SimilarityMeasure similarityMeasure, final ClusteringMethod clusteringMethod, final int numberOfClasses
		)
		{
			this.similarityMeasure = similarityMeasure;
			this.clusteringMethod = clusteringMethod;
			this.numberOfClasses = numberOfClasses;
		}
	}
}
