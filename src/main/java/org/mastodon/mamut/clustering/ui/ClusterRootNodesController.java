package org.mastodon.mamut.clustering.ui;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.model.tag.TagSetStructure;
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

	private CropCriteria cropCriterion = CropCriteria.TIMEPOINT;

	private SimilarityMeasure similarityMeasure = SimilarityMeasure.NORMALIZED_DIFFERENCE;

	private ClusteringMethod clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE;

	private int cropStart;

	private int cropEnd;

	private int numberOfClasses;

	private int minCellDivisions;

	private final List< ClusterRootNodesListener< BranchSpotTree > > listeners = new ArrayList<>();

	private boolean running = false;

	public ClusterRootNodesController( final Model model )
	{
		this.model = model;
		setDefaults();
	}

	public void createTagSet()
	{
		if ( running )
			return;
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
		model.getBranchGraph().graphRebuilt();
		List< BranchSpotTree > roots = getRoots();
		Classification< BranchSpotTree > classification = classifyLineageTrees( roots );

		// notify view to start dendrogram visualisation
		listeners.forEach( listener -> listener.clusterRootNodesComputed( classification.getAlgorithmResult(),
				classification.getObjectMapping(), classification.getCutoff() ) );

		Collection< Pair< String, Integer > > tagsAndColors = createTagsAndColors();
		applyClassification( classification, tagsAndColors );
	}

	private Classification< BranchSpotTree > classifyLineageTrees( List< BranchSpotTree > roots )
	{
		double[][] distances = ClusterUtils.getDistanceMatrix( new ArrayList<>( roots ), getSimilarityMeasure() );
		BranchSpotTree[] rootBranchSpots = roots.toArray( new BranchSpotTree[ 0 ] );
		return ClusterUtils.getClassificationByClassCount( rootBranchSpots, distances,
				getClusteringMethod().getLinkageStrategy(), getNumberOfClasses() );
	}

	private Collection< Pair< String, Integer > > createTagsAndColors()
	{
		GlasbeyLut.reset();
		GlasbeyLut.next();
		Collection< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		for ( int i = 0; i < getNumberOfClasses(); i++ )
			tagsAndColors.add( Pair.of( "Class " + ( i + 1 ), GlasbeyLut.next().getRGB() ) );
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
				LineageTreeUtils.callDepthFirst( model.getGraph(), rootSpot,
						spot -> {
							if ( spot.getTimepoint() > getCropEnd() )
								return;
							TagSetUtils.tagSpotAndLinks( model, spot, tagSet, tag );
						} );
			}
		}
	}

	private List< BranchSpotTree > getRoots()
	{
		Set< Spot > roots = RootFinder.getRoots( model.getGraph() );
		List< BranchSpotTree > trees = new ArrayList<>();
		for ( Spot root : roots )
		{
			BranchSpot rootBranchSpot = model.getBranchGraph().getBranchVertex( root, model.getBranchGraph().vertexRef() );
			try
			{
				BranchSpotTree branchSpotTree = new BranchSpotTree( rootBranchSpot, getCropEnd() );
				int minTreeSize = 2 * getMinCellDivisions() + 1;
				if ( TreeUtils.size( branchSpotTree ) < minTreeSize )
					continue;
				trees.add( branchSpotTree );
			}
			catch ( IllegalArgumentException e )
			{
				logger.debug( "Could not create tree for root {}. Message: {}", root, e.getMessage() );
			}
		}
		return trees;
	}

	private void setDefaults()
	{
		setCropCriterion( CropCriteria.TIMEPOINT );
		setSimilarityMeasure( SimilarityMeasure.NORMALIZED_DIFFERENCE );
		setClusteringMethod( ClusteringMethod.AVERAGE_LINKAGE );
		cropStart = 0;
		cropEnd = 0;
		numberOfClasses = 5;
		minCellDivisions = 1;
	}

	public void setCropCriterion( final CropCriteria cropCriterion )
	{
		if ( this.cropCriterion != null && this.cropCriterion.equals( cropCriterion ) )
			return;
		this.cropCriterion = cropCriterion;
		resetStartEnd();
	}

	private void resetStartEnd()
	{
		setCropStart( 0 );
		setCropEnd( 0 );
		listeners.forEach( listener -> listener.cropCriterionChanged( getCropStart(), getCropEnd() ) );
	}

	public void setSimilarityMeasure( final SimilarityMeasure similarityMeasure )
	{
		this.similarityMeasure = similarityMeasure;
	}

	public void setClusteringMethod( final ClusteringMethod clusteringMethod )
	{
		this.clusteringMethod = clusteringMethod;
	}

	public void setCropStart( final int cropStart )
	{
		this.cropStart = cropStart;
	}

	public void setCropEnd( final int cropEnd )
	{
		this.cropEnd = cropEnd;
	}

	public void setNumberOfClasses( final int numberOfClasses )
	{
		this.numberOfClasses = numberOfClasses;
	}

	public void setMinCellDivisions( final int minCellDivisions )
	{
		this.minCellDivisions = minCellDivisions;
	}

	public CropCriteria getCropCriterion()
	{
		return cropCriterion;
	}

	public SimilarityMeasure getSimilarityMeasure()
	{
		return similarityMeasure;
	}

	public ClusteringMethod getClusteringMethod()
	{
		return clusteringMethod;
	}

	public int getCropStart()
	{
		return cropStart;
	}

	public int getCropEnd()
	{
		return cropEnd;
	}

	public int getNumberOfClasses()
	{
		return numberOfClasses;
	}

	public int getMinCellDivisions()
	{
		return minCellDivisions;
	}

	public void addListener( final ClusterRootNodesListener< BranchSpotTree > listener )
	{
		listeners.add( listener );
	}
}
