package org.mastodon.mamut.clustering;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterUtils
{

	private ClusterUtils()
	{
		// prevent from instantiation
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final ClusteringAlgorithm algorithm = new DefaultClusteringAlgorithm();

	/**
	 * Gets a mapping from cluster id to leave names. The cluster ids are incremented by 1 starting from 0.
	 * The amount of clusters depends on the given threshold. The leave names are sorted alphabetically.
	 * <p>
	 * Constraints:
	 * <ul>
	 *     <li>The distance matrix needs to be quadratic</li>
	 *     <li>The distance matrix to be symmetric with zero diagonal</li>
	 *     <li>The length of the leave names needs to equal the length of the distance matrix</li>
	 *     <li>The leave names need to be unique</li>
	 * </ul>
	 *
	 * @param leaveNames the unique names of the leaves
	 * @param distances the symmetric distance matrix with zero diagonal
	 * @param linkageStrategy the linkage strategy (e.g. {@link com.apporiented.algorithm.clustering.AverageLinkageStrategy}, {@link com.apporiented.algorithm.clustering.CompleteLinkageStrategy}, {@link com.apporiented.algorithm.clustering.SingleLinkageStrategy})
	 * @param threshold the threshold for the distance for building clusters
	 * @return a mapping from cluster id to leave names
	 */
	public static Map< Integer, List< String > > getClustersByThreshold( String[] leaveNames, double[][] distances,
			LinkageStrategy linkageStrategy, double threshold )
	{
		if ( threshold < 0 )
			throw new IllegalArgumentException( "threshold must be greater than or equal to zero" );

		List< Cluster > clusters = getSortedClusters( leaveNames, distances, linkageStrategy );

		List< Cluster > output = new ArrayList<>();
		for ( Cluster cluster : clusters )
		{
			if ( cluster.getDistanceValue() < threshold )
				break;
			output.add( cluster );
		}

		Map< Integer, List< String > > classes = convertClustersToClasses( output );
		log( classes );
		return classes;
	}

	/**
	 * Gets a mapping from cluster id to leave names. The cluster ids are incremented by 1 starting from 0.
	 * The amount of clusters depends on the given class count. The leave names are sorted alphabetically.
	 * <p>
	 * Constraints:
	 * <ul>
	 *     <li>The distance matrix needs to be quadratic</li>
	 *     <li>The distance matrix to be symmetric with zero diagonal</li>
	 *     <li>The length of the leave names needs to equal the length of the distance matrix</li>
	 *     <li>The leave names need to be unique</li>
	 *     <li>The class count needs to be greater than zero</li>
	 *     <li>The class count needs to be less than or equal to the number of names</li>
	 * </ul>
	 *
	 * @param leaveNames the unique names of the leaves
	 * @param distances the symmetric distance matrix with zero diagonal
	 * @param linkageStrategy the linkage strategy (e.g. {@link com.apporiented.algorithm.clustering.AverageLinkageStrategy}, {@link com.apporiented.algorithm.clustering.CompleteLinkageStrategy}, {@link com.apporiented.algorithm.clustering.SingleLinkageStrategy})
	 * @param classCount the number of classes to be built
	 * @return a mapping from cluster id to leave names
	 */
	public static Map< Integer, List< String > > getClustersByClassCount( String[] leaveNames, double[][] distances,
			LinkageStrategy linkageStrategy, int classCount )
	{
		if ( classCount < 1 )
			throw new IllegalArgumentException( "classCount must be greater than zero" );
		else if ( classCount > leaveNames.length )
			throw new IllegalArgumentException( "classCount must be less than or equal to the number of names" );
		else if ( classCount == 1 )
			return Collections.singletonMap( 0, Arrays.asList( leaveNames ) );

		int clusterId = 0;
		Map< Integer, List< String > > classes = new HashMap<>();
		if ( classCount == leaveNames.length )
		{
			for ( String name : leaveNames )
				classes.put( clusterId++, Collections.singletonList( name ) );
			return classes;
		}
		List< Cluster > clusters = getSortedClusters( leaveNames, distances, linkageStrategy );
		List< Cluster > output = new ArrayList<>();
		for ( Cluster cluster : clusters )
		{
			output.add( cluster );
			if ( output.size() == classCount - 1 )
				break;
		}

		classes = convertClustersToClasses( output );
		log( classes );
		return classes;
	}

	private static List< Cluster > getSortedClusters( String[] leaveNames, double[][] distances, LinkageStrategy linkageStrategy )
	{
		Cluster algorithmResult = algorithm.performClustering( distances, leaveNames, linkageStrategy );
		List< Cluster > clusters = allClusters( algorithmResult );
		clusters.sort( Comparator.comparingDouble( Cluster::getDistanceValue ) );
		Collections.reverse( clusters );
		return clusters;
	}

	private static Map< Integer, List< String > > convertClustersToClasses( List< Cluster > output )
	{
		int clusterId = 0;
		Map< Integer, List< String > > classes = new HashMap<>();
		for ( Cluster cluster : output )
		{
			for ( Cluster child : cluster.getChildren() )
			{
				if ( !output.contains( child ) )
					classes.put( clusterId++, leaveNames( child ) );
			}
		}
		return classes;
	}

	private static void log( Map< Integer, List< String > > leavesToClusterIds )
	{
		leavesToClusterIds.forEach( ( id, leaves ) -> logger.info( "clusterId: {}, leaves: {}", id, String.join( ",", leaves ) ) );
	}

	private static List< Cluster > allClusters( final Cluster cluster )
	{
		if ( cluster == null )
			throw new IllegalArgumentException( "Given cluster must not be null" );
		List< Cluster > list = new ArrayList<>();
		list.add( cluster );
		for ( Cluster child : cluster.getChildren() )
			list.addAll( allClusters( child ) );
		return list;
	}

	private static List< String > leaveNames( final Cluster cluster )
	{
		if ( cluster == null )
			throw new IllegalArgumentException( "Given cluster must not be null" );
		List< String > list = new ArrayList<>();
		if ( cluster.isLeaf() )
			list.add( cluster.getName() );
		for ( Cluster child : cluster.getChildren() )
			list.addAll( leaveNames( child ) );
		Collections.sort( list );
		return list;
	}
}
