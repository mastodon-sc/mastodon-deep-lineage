package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;

import java.util.List;
import java.util.Map;

public class Classification< T >
{
	private final Map< Integer, List< T > > classifiedObjects;

	private final Cluster algorithmResult;

	private final Map< String, T > objectMapping;

	private final double cutoff;

	public Classification( Map< Integer, List< T > > classifiedObjects, Cluster algorithmResult, Map< String, T > objectMapping,
			double cutoff )
	{
		this.classifiedObjects = classifiedObjects;
		this.algorithmResult = algorithmResult;
		this.objectMapping = objectMapping;
		this.cutoff = cutoff;
	}

	public Map< Integer, List< T > > getClassifiedObjects()
	{
		return classifiedObjects;
	}

	public Cluster getAlgorithmResult()
	{
		return algorithmResult;
	}

	public Map< String, T > getObjectMapping()
	{
		return objectMapping;
	}

	public double getCutoff()
	{
		return cutoff;
	}
}
