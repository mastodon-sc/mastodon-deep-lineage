package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class that encapsulates the result of a clustering algorithm.<p>
 *     It contains:
 *     <ul>
 *         <li>an ordered list of classified objects</li>
 *         <li>the result of the clustering algorithm</li>
 *         <li>a mapping from leaf names to objects</li>
 *         <li>the cutoff value of classification, i.e. where the dendrogram is cut</li>
 *         <li>the colors of the clusters and the class ids</li>
 *     </ul>
 * @author Stefan Hahmann
 */
public class Classification< T >
{
	private final List< ColoredCluster< T > > coloredClusters;

	@Nullable
	private final Cluster rootCluster;

	private final double cutoff;

	public Classification( final List< Pair< Set< T >, Cluster > > classifiedObjects, @Nullable final Cluster rootCluster, double cutoff )
	{
		this.coloredClusters = new ArrayList<>();
		List< Integer > glasbeyColors = ClusterUtils.getGlasbeyColors( classifiedObjects.size() );
		for ( int i = 0; i < classifiedObjects.size(); i++ )
		{
			Pair< Set< T >, Cluster > clusterClassPair = classifiedObjects.get( i );
			this.coloredClusters.add(
					new ColoredCluster<>( glasbeyColors.get( i ), clusterClassPair.getRight(), clusterClassPair.getLeft() ) );
		}
		this.rootCluster = rootCluster;
		this.cutoff = cutoff;
	}

	public List< ColoredCluster< T > > getColoredClusters()
	{
		return coloredClusters;
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

	public List< Set< T > > getClassifiedObjects()
	{
		return coloredClusters.stream().map( ColoredCluster::getObjects ).collect( Collectors.toList() );
	}

	public static class ColoredCluster< T >
	{
		private final int color;

		private final Cluster cluster;

		private final Set< T > objects;

		private ColoredCluster( final int color, final Cluster cluster, final Set< T > objects )
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
