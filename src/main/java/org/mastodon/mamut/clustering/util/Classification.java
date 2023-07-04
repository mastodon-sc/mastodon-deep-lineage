package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Classification< T >
{
	private final Map< Integer, List< T > > classifiedObjects;

	@Nullable
	private final Cluster algorithmResult;

	@Nullable
	private final Map< String, T > objectMapping;

	private final double cutoff;

	public Classification( final Map< Integer, List< T > > classifiedObjects, @Nullable final Cluster algorithmResult,
			@Nullable final Map< String, T > objectMapping, double cutoff )
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
}
