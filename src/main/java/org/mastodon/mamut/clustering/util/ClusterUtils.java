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
package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import net.imglib2.parallel.Parallelization;
import net.imglib2.util.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;
import org.mastodon.util.ColorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ClusterUtils
{

	private ClusterUtils()
	{
		// prevent from instantiation
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final ClusteringAlgorithm algorithm = new DefaultClusteringAlgorithm();

	/**
	 * Computes a symmetric quadratic distance matrix for the given trees using the given similarity measure. The diagonals are set to zero.
	 * @param trees a list of trees
	 * @param similarityMeasure the similarity measure to be used
	 * @return a symmetric quadratic distance matrix
	 */
	public static double[][] getDistanceMatrix( List< Tree< Double > > trees, SimilarityMeasure similarityMeasure )
	{
		int size = trees.size();
		double[][] distances = new double[ size ][ size ];
		List< Pair< Integer, Integer > > pairs = new ArrayList<>();

		// NB: only the upper triangle needs to be computed since the matrix is symmetric
		for ( int i = 0; i < size; i++ )
			for ( int j = i; j < size; j++ )
			{
				if ( i == j )
					distances[ i ][ j ] = 0; // Set diagonal elements to zero
				else
					pairs.add( Pair.of( i, j ) );
			}

		int numTasks = pairs.size();
		int outputRate = ( int ) Math.pow( 10, Math.floor( Math.log10( numTasks ) ) );
		AtomicInteger counter = new AtomicInteger( 0 );
		Parallelization.getTaskExecutor().forEach( pairs, pair -> {
			int i = pair.getLeft();
			int j = pair.getRight();
			double distance = similarityMeasure.compute( trees.get( i ), trees.get( j ),
					ZhangUnorderedTreeEditDistance.DEFAULT_COST_FUNCTION );
			distances[ i ][ j ] = distance;
			distances[ j ][ i ] = distance; // symmetric
			int finishedTasks = counter.incrementAndGet();
			if ( finishedTasks % outputRate == 0 )
				logger.debug( "Computed {} of {} distances ({}%).", finishedTasks, numTasks, ( finishedTasks * 100 ) / numTasks );
		} );

		return distances;
	}

	/**
	 * Gets a {@link Classification} that contains a mapping from cluster ids to objects.<br>
	 * The cluster ids are incremented by 1 starting from 0.
	 * The amount of clusters depends on the given threshold.
	 * <br>
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
	public static < T > Classification< T > getClassificationByThreshold(
			final T[] objects, final double[][] distances, final LinkageStrategy linkageStrategy, final double threshold
	)
	{
		return getClassificationByThreshold( objects, distances, linkageStrategy, threshold, null, null, null );
	}

	private static < T > Classification< T > getClassificationByThreshold(
			final T[] objects, final double[][] distances, final LinkageStrategy linkageStrategy, final double threshold,
			@Nullable Map< String, T > objectMapping, @Nullable Cluster algorithmResult, @Nullable List< Cluster > sortedClusters
	)
	{
		if ( threshold < 0 )
			throw new IllegalArgumentException( "threshold must be greater than or equal to zero" );

		if ( objectMapping == null )
			objectMapping = objectMapping( objects );
		if ( algorithmResult == null )
			algorithmResult = performClustering( distances, linkageStrategy, objectMapping );
		if ( sortedClusters == null )
			sortedClusters = sortClusters( algorithmResult );

		List< Cluster > resultClusters = new ArrayList<>();
		for ( Cluster cluster : sortedClusters )
		{
			if ( cluster.getDistanceValue() <= threshold )
				break;
			resultClusters.add( cluster );
		}
		if ( resultClusters.isEmpty() )
		{
			Cluster pseudoRoot = new Cluster( null );
			pseudoRoot.addChild( algorithmResult );
			resultClusters.add( pseudoRoot );
		}

		List< Pair< Set< T >, Cluster > > classesAndClusters = convertClustersToClasses( resultClusters, objectMapping );
		resetClusterNames( algorithmResult, objectMapping );
		log( classesAndClusters );
		double[] upperTriangle = ClusterUtils.getUpperTriangle( distances );
		double median = upperTriangle.length == 0 ? Double.NaN : Util.median( upperTriangle );
		return new Classification<>( classesAndClusters, algorithmResult, threshold, median );
	}

	private static void resetClusterNames( final Cluster cluster, final Map< String, ? > objectMapping )
	{
		if ( cluster.isLeaf() )
		{
			if ( objectMapping.containsKey( cluster.getName() ) )
				cluster.setName( objectMapping.get( cluster.getName() ).toString() );
		}
		else
		{
			for ( Cluster child : cluster.getChildren() )
			{
				resetClusterNames( child, objectMapping );
			}
		}
	}

	/**
	 * Gets a {@link Classification} that contains a mapping from cluster ids to objects.<br>
	 * The cluster ids are incremented by 1 starting from 0.
	 * The amount of clusters depends on the given class count.
	 * <br>
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
	public static < T > Classification< T > getClassificationByClassCount( final T[] objects, final double[][] distances,
			final LinkageStrategy linkageStrategy, final int classCount )
	{
		if ( classCount < 1 )
			throw new IllegalArgumentException( "number of classes (" + classCount + ") must be greater than zero." );
		else if ( classCount > objects.length )
			throw new IllegalArgumentException(
					"number of classes (" + classCount + ") must be less than or equal to the number of objects to be classified ("
							+ objects.length + ")." );

		// NB: the cluster algorithm needs unique names instead of objects
		Map< String, T > objectMapping = objectMapping( objects );
		Cluster algorithmResult = performClustering( distances, linkageStrategy, objectMapping );
		List< Cluster > sortedClusters = sortClusters( algorithmResult );
		double threshold = getThreshold( sortedClusters, classCount );

		return getClassificationByThreshold(
				objects, distances, linkageStrategy, threshold, objectMapping, algorithmResult, sortedClusters );
	}

	/**
	 * Gets a list of color values from the {@link ColorUtils#GLASBEY} palette.<br>
	 * Skips the first five colors of the palette, since the 4th color is close to black and thus difficult to see.
	 * If the given {@code n} is larger than the size of {@link ColorUtils#GLASBEY}, the colors are added and repeated in a round-robin fashion.<br>
	 * @param n the number of colors to be picked
	 * @return a list of color values
	 */
	public static List< Integer > getGlasbeyColors( int n )
	{
		if ( n <= 0 )
			return Collections.emptyList();
		List< Integer > colors = new ArrayList<>();
		for ( int i = 0; i < n; i++ )
			colors.add( getGlasbeyColor( i, 5 ) );
		return colors;
	}

	static int getGlasbeyColor( int n, int skipFirstNColors )
	{
		int index = n % ( ColorUtils.GLASBEY.length - skipFirstNColors ) + skipFirstNColors;
		return ColorUtils.GLASBEY[ index ].getRGB();
	}

	private static double getThreshold( final List< Cluster > sortedClusters, int classCount )
	{
		if ( classCount == 1 )
			return Double.MAX_VALUE;
		// Threshold for clustering by class count is determined as the mean between the cutoff cluster and the next cluster
		double threshold = sortedClusters.get( classCount - 2 ).getDistanceValue();
		if ( sortedClusters.size() < classCount )
			return threshold;
		else
		{
			return ( threshold + sortedClusters.get( classCount - 1 ).getDistanceValue() ) / 2d;
		}

	}

	private static < T > Cluster performClustering( double[][] distances, LinkageStrategy linkageStrategy,
			Map< String, T > uniqueObjectNames )
	{
		String[] uniqueNames = uniqueObjectNames.keySet().toArray( new String[ 0 ] );
		return algorithm.performClustering( distances, uniqueNames, linkageStrategy );
	}

	private static List< Cluster > sortClusters( Cluster algorithmResult )
	{
		List< Cluster > clusters = allClusters( algorithmResult );
		clusters.sort( Comparator.comparingDouble( Cluster::getDistanceValue ) );
		Collections.reverse( clusters );
		return clusters;
	}

	private static < T > Map< String, T > objectMapping( T[] objects )
	{
		Map< String, T > objectNames = new LinkedHashMap<>();
		for ( int i = 0; i < objects.length; i++ )
			objectNames.put( String.valueOf( i ), objects[ i ] );
		return objectNames;
	}

	private static < T > List< Pair< Set< T >, Cluster > > convertClustersToClasses(
			List< Cluster > output, Map< String, T > objectNames
	)
	{
		List< Cluster > classes = new ArrayList<>();
		for ( Cluster cluster : output )
		{
			for ( Cluster child : cluster.getChildren() )
			{
				if ( !output.contains( child ) )
					classes.add( child );
			}
		}
		List< Pair< Set< T >, Cluster > > classifiedObjects = new ArrayList<>();
		for ( Cluster cluster : classes )
		{
			Set< T > objects = new HashSet<>();
			List< String > leaveNames = leaveNames( cluster );
			for ( String leaveName : leaveNames )
				objects.add( objectNames.get( leaveName ) );
			classifiedObjects.add( Pair.of( objects, cluster ) );
		}
		return classifiedObjects;
	}

	private static < T > void log( List< Pair< Set< T >, Cluster > > objectsToClusterIds )
	{
		int i = 0;
		for ( Pair< Set< T >, Cluster > entry : objectsToClusterIds )
		{
			if ( logger.isInfoEnabled() )
				logger.info( "clusterId: {}, object: {}", i++,
						entry.getLeft().stream().map( Object::toString ).collect( Collectors.joining( "," ) )
				);
		}
	}

	private static List< Cluster > allClusters( final Cluster cluster )
	{
		List< Cluster > list = new ArrayList<>();
		list.add( cluster );
		for ( Cluster child : cluster.getChildren() )
			list.addAll( allClusters( child ) );
		return list;
	}

	private static List< String > leaveNames( final Cluster cluster )
	{
		List< String > list = new ArrayList<>();
		if ( cluster.isLeaf() )
			list.add( cluster.getName() );
		for ( Cluster child : cluster.getChildren() )
			list.addAll( leaveNames( child ) );
		Collections.sort( list );
		return list;
	}

	/**
	 * Gets the upper triangle of a two-dimensional quadratic array and outputs it as a one-dimensional array.
	 * E.g. for the following matrix:
	 * <pre>
	 *     0 1 2 3
	 *     1 0 4 5
	 *     2 4 0 6
	 *     3 5 6 0
	 * </pre>
	 * the upper triangle is:
	 * <pre>
	 *     [1 2 3 4 5 6]
	 * </pre>
	 *
	 * @param twoDimensionalArray a two-dimensional quadratic array
	 * @return the upper triangle of the given two-dimensional array as a one-dimensional array. If the given array is {@code null}, empty or has length 1, an empty array is returned.
	 * @throws IllegalArgumentException if the given array is not quadratic
	 */
	public static double[] getUpperTriangle( final double[][] twoDimensionalArray )
	{
		if ( twoDimensionalArray == null )
			return new double[ 0 ];
		int inputLength = twoDimensionalArray.length;
		if ( inputLength <= 1 )
			return new double[ 0 ];
		if ( twoDimensionalArray.length != twoDimensionalArray[ 0 ].length )
			throw new IllegalArgumentException( "The given array is not quadratic." );
		int outputLength = ( inputLength * inputLength - inputLength ) / 2;
		double[] array = new double[ outputLength ];
		int index = 0;
		for ( int i = 0; i < inputLength; i++ )
			for ( int j = i + 1; j < inputLength; j++ )
				array[ index++ ] = twoDimensionalArray[ i ][ j ];
		return array;
	}
}
