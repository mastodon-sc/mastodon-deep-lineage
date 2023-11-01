package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Classification< T >
{
	private final List< Set< T > > classifiedObjects;

	@Nullable
	private final Cluster algorithmResult;

	@Nullable
	private final Map< String, T > objectMapping;

	@Nullable
	private final Map< Integer, Cluster > clusterClasses;

	private final double cutoff;

	public Classification(
			final List< Set< T > > classifiedObjects, @Nullable final Cluster algorithmResult,
			@Nullable final Map< String, T > objectMapping, double cutoff, @Nullable final Map< Integer, Cluster > clusterClasses
	)
	{
		this.classifiedObjects = classifiedObjects;
		this.algorithmResult = algorithmResult;
		this.objectMapping = objectMapping;
		this.clusterClasses = clusterClasses;
		this.cutoff = cutoff;
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
	public Map< String, T > getObjectMapping()
	{
		return objectMapping;
	}

	public double getCutoff()
	{
		return cutoff;
	}

	@Nullable
	public Map< Integer, Cluster > getClusterClasses()
	{
		return clusterClasses;
	}
}
