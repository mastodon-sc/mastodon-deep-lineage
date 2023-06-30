package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import net.imglib2.parallel.Parallelization;
import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;
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
import java.util.stream.Collectors;

public class ClusterUtils
{

	private ClusterUtils()
	{
		// prevent from instantiation
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final ClusteringAlgorithm algorithm = new DefaultClusteringAlgorithm();

	public static double[][] getDistanceMatrix( List< Tree< Double > > trees, SimilarityMeasure similarityMeasure )
	{
		int size = trees.size();
		double[][] distances = new double[ size ][ size ];
		List< Pair< Integer, Integer > > pairs = new ArrayList<>();

		// only the upper triangle is computed since the matrix is symmetric
		for ( int i = 0; i < size; i++ )
			for ( int j = i; j < size; j++ )
			{
				if ( i == j )
				{
					distances[ i ][ j ] = 0; // Set diagonal elements to zero
					continue;
				}
				pairs.add( Pair.of( i, j ) );
			}

		Parallelization.getTaskExecutor().forEach( pairs, pair -> {
			int i = pair.getLeft();
			int j = pair.getRight();
			double distance = similarityMeasure.compute( trees.get( i ), trees.get( j ),
					ZhangUnorderedTreeEditDistance.getDefaultCostFunction() );
			distances[ i ][ j ] = distance;
			distances[ j ][ i ] = distance;
		} );

		return distances;
	}

	/**
	 * Gets a mapping from cluster id to objects. The cluster ids are incremented by 1 starting from 0.
	 * The amount of clusters depends on the given threshold.
	 * <p>
	 * Constraints:
	 * <ul>
	 *     <li>The distance matrix needs to be quadratic</li>
	 *     <li>The distance matrix to be symmetric with zero diagonal</li>
	 *     <li>The number of object needs to equal the length of the distance matrix</li>
	 * </ul>
	 *
	 * @param objects the objects to be clustered
	 * @param distances the symmetric distance matrix with zero diagonal
	 * @param linkageStrategy the linkage strategy (e.g. {@link com.apporiented.algorithm.clustering.AverageLinkageStrategy}, {@link com.apporiented.algorithm.clustering.CompleteLinkageStrategy}, {@link com.apporiented.algorithm.clustering.SingleLinkageStrategy})
	 * @param threshold the threshold for the distance for building clusters
	 * @return a mapping from cluster id objects
	 */
	public static < T > Map< Integer, List< T > > getClustersByThreshold( T[] objects, double[][] distances,
			LinkageStrategy linkageStrategy, double threshold )
	{
		if ( threshold < 0 )
			throw new IllegalArgumentException( "threshold must be greater than or equal to zero" );

		Map< String, T > objectNames = new HashMap<>();
		for ( int i = 0; i < objects.length; i++ )
			objectNames.put( String.valueOf( i ), objects[ i ] );
		String[] names = objectNames.keySet().toArray( new String[ 0 ] );

		List< Cluster > sortedClusters = getSortedClusters( names, distances, linkageStrategy );
		List< Cluster > output = new ArrayList<>();
		for ( Cluster cluster : sortedClusters )
		{
			if ( cluster.getDistanceValue() < threshold )
				break;
			output.add( cluster );
		}

		Map< Integer, List< T > > classifiedObjects = convertClustersToClasses( output, objectNames );
		log( classifiedObjects );
		return classifiedObjects;
	}

	/**
	 * Gets a mapping from cluster id to objects. The cluster ids are incremented by 1 starting from 0.
	 * The amount of clusters depends on the given class count.
	 * <p>
	 * Constraints:
	 * <ul>
	 *     <li>The distance matrix needs to be quadratic</li>
	 *     <li>The distance matrix to be symmetric with zero diagonal</li>
	 *     <li>The number of objects needs to equal the length of the distance matrix</li>
	 *     <li>The class count needs to be greater than zero</li>
	 *     <li>The class count needs to be less than or equal to the number of names</li>
	 * </ul>
	 *
	 * @param objects the objects to be clustered
	 * @param distances the symmetric distance matrix with zero diagonal
	 * @param linkageStrategy the linkage strategy (e.g. {@link com.apporiented.algorithm.clustering.AverageLinkageStrategy}, {@link com.apporiented.algorithm.clustering.CompleteLinkageStrategy}, {@link com.apporiented.algorithm.clustering.SingleLinkageStrategy})
	 * @param classCount the number of classes to be built
	 * @return a mapping from cluster id objects
	 */
	public static < T > Map< Integer, List< T > > getClustersByClassCount( T[] objects, double[][] distances,
			LinkageStrategy linkageStrategy, int classCount )
	{
		if ( classCount < 1 )
			throw new IllegalArgumentException( "classCount must be greater than zero" );
		else if ( classCount > objects.length )
			throw new IllegalArgumentException( "classCount must be less than or equal to the number of names" );
		else if ( classCount == 1 )
			return Collections.singletonMap( 0, Arrays.asList( objects ) );

		int clusterId = 0;
		Map< Integer, List< T > > classes = new HashMap<>();
		if ( classCount == objects.length )
		{
			for ( T name : objects )
				classes.put( clusterId++, Collections.singletonList( name ) );
			return classes;
		}

		Map< String, T > objectNames = convertObjects( objects );
		String[] names = objectNames.keySet().toArray( new String[ 0 ] );

		List< Cluster > sortedClusters = getSortedClusters( names, distances, linkageStrategy );
		List< Cluster > clusters = new ArrayList<>();
		for ( Cluster cluster : sortedClusters )
		{
			clusters.add( cluster );
			if ( clusters.size() == classCount - 1 )
				break;
		}

		Map< Integer, List< T > > classifiedObjects = convertClustersToClasses( clusters, objectNames );
		log( classifiedObjects );
		return classifiedObjects;
	}

	private static List< Cluster > getSortedClusters( String[] leaveNames, double[][] distances, LinkageStrategy linkageStrategy )
	{
		Cluster algorithmResult = algorithm.performClustering( distances, leaveNames, linkageStrategy );
		List< Cluster > clusters = allClusters( algorithmResult );
		clusters.sort( Comparator.comparingDouble( Cluster::getDistanceValue ) );
		Collections.reverse( clusters );
		return clusters;
	}

	private static < T > Map< String, T > convertObjects( T[] objects )
	{
		Map< String, T > objectNames = new HashMap<>();
		for ( int i = 0; i < objects.length; i++ )
			objectNames.put( String.valueOf( i ), objects[ i ] );
		return objectNames;
	}

	private static < T > Map< Integer, List< T > > convertClustersToClasses( List< Cluster > output, Map< String, T > objectNames )
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
		Map< Integer, List< T > > classifiedObjects = new HashMap<>();
		for ( Map.Entry< Integer, List< String > > entry : classes.entrySet() )
		{
			List< T > o = new ArrayList<>();
			for ( String name : entry.getValue() )
				o.add( objectNames.get( name ) );
			classifiedObjects.put( entry.getKey(), o );
		}
		return classifiedObjects;
	}

	private static < T > void log( Map< Integer, List< T > > objectsToClusterIds )
	{
		for ( Map.Entry< Integer, List< T > > entry : objectsToClusterIds.entrySet() )
		{
			List< T > objects = entry.getValue();
			if ( logger.isInfoEnabled() )
				logger.info( "clusterId: {}, object: {}", entry.getKey(),
						objects.stream().map( Object::toString ).collect( Collectors.joining( "," ) ) );
		}
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
