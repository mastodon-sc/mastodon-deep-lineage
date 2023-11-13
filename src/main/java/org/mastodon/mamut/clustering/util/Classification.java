package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class that encapsulates the result of a clustering algorithm.<p>
 * It contains:
 *     <ul>
 *         <li>the root {@link Cluster} object, from which the results of the algorithm can be accessed</li>
 *         <li>a {@link List} of {@link ObjectClassification} objects, where each objects contains:</li>
 *         <ul>
 *             <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
 *             <li>a {@link Set} of objects, which are classified into the same class</li>
 *             <li>a color, which is associated with that class</li>
 *         </ul>
 *         <li>the cutoff value of classification, i.e. where the dendrogram is cut</li>
 *     </ul>
 * @author Stefan Hahmann
 */
public class Classification< T >
{
	private final Set< ObjectClassification< T > > objectClassifications;

	@Nullable
	private final Cluster rootCluster;

	private final double cutoff;

	/**
	 * Creates a new {@link Classification} object.
	 * @param classifiedObjects a {@link List} of {@link Pair} objects, where each pair contains:
	 * 						<ul>
	 * 						    <li>a {@link Set} of objects, which are classified into the same class</li>
	 * 						    <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
	 * 						</ul>
	 * @param rootCluster the root {@link Cluster} object, from which the results of the algorithm can be accessed
	 * @param cutoff the cutoff value of classification, i.e. where the dendrogram is cut
	 */
	public Classification( final List< Pair< Set< T >, Cluster > > classifiedObjects, @Nullable final Cluster rootCluster, double cutoff )
	{
		this.objectClassifications = new HashSet<>();
		List< Integer > glasbeyColors = ClusterUtils.getGlasbeyColors( classifiedObjects.size() );
		for ( int i = 0; i < classifiedObjects.size(); i++ )
		{
			Pair< Set< T >, Cluster > clusterClassPair = classifiedObjects.get( i );
			this.objectClassifications.add(
					new ObjectClassification<>( glasbeyColors.get( i ), clusterClassPair.getRight(), clusterClassPair.getLeft() ) );
		}
		this.rootCluster = rootCluster;
		this.cutoff = cutoff;
	}

	public Set< ObjectClassification< T > > getObjectClassifications()
	{
		return objectClassifications;
	}

	@Nullable
	public Cluster getRootCluster()
	{
		return rootCluster;
	}

	public double getCutoff()
	{
		return cutoff;
	}

	Set< Set< T > > getClassifiedObjects()
	{
		return objectClassifications.stream().map( ObjectClassification::getObjects ).collect( Collectors.toSet() );
	}

	/**
	 * A class that encapsulates the result of a clustering algorithm for a single class.<p>
	 * It contains:
	 *    <ul>
	 *        <li>a {@link Cluster} object, which represents the classified objects in the dendrogram</li>
	 *        <li>a {@link Set} of objects, which are classified into the same class</li>
	 *        <li>a color, which is associated with that class</li>
	 *    </ul>
	 */
	public static class ObjectClassification< T >
	{
		private final int color;

		private final Cluster cluster;

		private final Set< T > objects;

		private ObjectClassification( final int color, final Cluster cluster, final Set< T > objects )
		{
			this.color = color;
			this.cluster = cluster;
			this.objects = objects;
		}

		public int getColor()
		{
			return color;
		}

		public Cluster getCluster()
		{
			return cluster;
		}

		public Set< T > getObjects()
		{
			return objects;
		}
	}
}
