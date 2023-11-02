package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Classification< T >
{
	private final List< Set< T > > classifiedObjects;

	@Nullable
	private final Cluster algorithmResult;

	@Nullable
	private final Map< String, T > leafMapping;

	private final Map< Cluster, Integer > clusterColors;

	private final Map< Integer, Integer > classIdColors;

	private final double cutoff;

	public Classification(
			final List< Pair< Set< T >, Cluster > > classifiedObjects, @Nullable final Cluster algorithmResult,
			@Nullable final Map< String, T > leafMapping, double cutoff
	)
	{
		this.classifiedObjects = classifiedObjects.stream().map( Pair::getKey ).collect( Collectors.toList() );
		this.algorithmResult = algorithmResult;
		this.leafMapping = leafMapping;
		this.cutoff = cutoff;

		List< Integer > glasbeyColors = ClusterUtils.getGlasbeyColors( classifiedObjects.size() );
		clusterColors = new HashMap<>();
		classIdColors = new HashMap<>();
		for ( int i = 0; i < classifiedObjects.size(); i++ )
		{
			Pair< Set< T >, Cluster > clusterClassPair = classifiedObjects.get( i );
			int color = glasbeyColors.get( i );
			classIdColors.put( i, color );
			Cluster cluster = clusterClassPair.getRight();
			if ( cluster != null )
				clusterColors.put( cluster, color );
		}
	}

	public List< Set< T > > getClassifiedObjects()
	{
		return classifiedObjects;
	}

	@Nullable
	public Cluster getAlgorithmResult()
	{
		return algorithmResult;
	}

	@Nullable
	public Map< String, T > getLeafMapping()
	{
		return leafMapping;
	}

	public double getCutoff()
	{
		return cutoff;
	}

	public Map< Cluster, Integer > getClusterColors()
	{
		return clusterColors;
	}

	public Map< Integer, Integer > getClassIdColors()
	{
		return classIdColors;
	}
}
