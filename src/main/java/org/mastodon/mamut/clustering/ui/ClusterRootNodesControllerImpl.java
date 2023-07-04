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

public class ClusterRootNodesControllerImpl implements ClusterRootNodesController< BranchSpotTree >
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private CropCriteria cropCriterion;

	private SimilarityMeasure similarityMeasure;

	private ClusteringMethod clusteringMethod;

	private int cropStart;

	private int cropEnd;

	private int numberOfClasses;

	private int minCellDivisions;

	private List< ClusterRootNodesListener< BranchSpotTree > > listeners;

	public ClusterRootNodesControllerImpl( final Model model )
	{
		this.model = model;
		setDefaults();
	}

	@Override
	public void createTagSet()
	{
		List< BranchSpotTree > roots = getRoots();

		// compute similarity matrix and hierarchical clustering
		double[][] distances = ClusterUtils.getDistanceMatrix( new ArrayList<>( roots ), similarityMeasure );
		BranchSpotTree[] rootBranchSpots = roots.toArray( new BranchSpotTree[ 0 ] );
		Classification< BranchSpotTree > classification = ClusterUtils.getClassificationByClassCount( rootBranchSpots, distances,
				clusteringMethod.getLinkageStrategy(), numberOfClasses );
		Map< Integer, List< BranchSpotTree > > classifiedObjects = classification.getClassifiedObjects();

		// notify listeners (e.g. for visualization of dendrogram)
		listeners.forEach( listener -> listener.clusterRootNodesComputed( classification.getAlgorithmResult(),
				classification.getObjectMapping(), classification.getCutoff() ) );

		GlasbeyLut.reset();
		GlasbeyLut.next();
		// create tagset
		Collection< Pair< String, Integer > > tagsAndColors = new ArrayList<>();
		for ( int i = 0; i < numberOfClasses; i++ )
			tagsAndColors.add( Pair.of( "Class " + ( i + 1 ), GlasbeyLut.next().getRGB() ) );

		// apply tagset
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, "Classifcation", tagsAndColors );
		for ( Map.Entry< Integer, List< BranchSpotTree > > entry : classifiedObjects.entrySet() )
		{
			logger.info( "Class {} has {} trees", entry.getKey(), entry.getValue().size() );
			TagSetStructure.Tag tag = tagSet.getTags().get( entry.getKey() );
			for ( BranchSpotTree tree : entry.getValue() )
			{
				Spot rootSpot = model.getBranchGraph().getFirstLinkedVertex( tree.getBranchSpot(), model.getGraph().vertexRef() );
				LineageTreeUtils.callDepthFirst( model.getGraph(), rootSpot,
						spot -> TagSetUtils.tagSpotAndLinks( model, tagSet, tag, spot, cropEnd ) );
			}
		}
	}

	private List< BranchSpotTree > getRoots()
	{
		// TODO: is rebuilt necessary?
		model.getBranchGraph().graphRebuilt();
		// TODO: lock model?
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
				logger.debug( "Could not create tree for root {}. Message: {}", root, e.getMessage() );
			}
		}
		return trees;
	}

	private void setDefaults()
	{
		cropCriterion = CropCriteria.TIMEPOINT;
		similarityMeasure = SimilarityMeasure.NORMALIZED_DIFFERENCE;
		clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE;
		cropStart = 0;
		cropEnd = 0;
		numberOfClasses = 5;
		minCellDivisions = 1;
	}

	@Override
	public void setCropCriterion( CropCriteria cropCriterion )
	{
		this.cropCriterion = cropCriterion;
	}

	@Override
	public void setSimilarityMeasure( SimilarityMeasure similarityMeasure )
	{
		this.similarityMeasure = similarityMeasure;
	}

	@Override
	public void setClusteringMethod( ClusteringMethod clusteringMethod )
	{
		this.clusteringMethod = clusteringMethod;
	}

	@Override
	public void setCropStart( int cropStart )
	{
		this.cropStart = cropStart;
	}

	@Override
	public void setCropEnd( int cropEnd )
	{
		this.cropEnd = cropEnd;
	}

	@Override
	public void setNumberOfClasses( int numberOfClasses )
	{
		this.numberOfClasses = numberOfClasses;
	}

	@Override
	public void setMinCellDivisions( int minCellDivisions )
	{
		this.minCellDivisions = minCellDivisions;
	}

	@Override
	public CropCriteria getCropCriterion()
	{
		return cropCriterion;
	}

	@Override
	public SimilarityMeasure getSimilarityMeasure()
	{
		return similarityMeasure;
	}

	@Override
	public ClusteringMethod getClusteringMethod()
	{
		return clusteringMethod;
	}

	@Override
	public int getCropStart()
	{
		return cropStart;
	}

	@Override
	public int getCropEnd()
	{
		return cropEnd;
	}

	@Override
	public int getNumberOfClasses()
	{
		return numberOfClasses;
	}

	@Override
	public int getMinCellDivisions()
	{
		return minCellDivisions;
	}

	@Override
	public void addListener( ClusterRootNodesListener< BranchSpotTree > listener )
	{
		if ( listeners == null )
			listeners = new ArrayList<>();
		listeners.add( listener );
	}
}
