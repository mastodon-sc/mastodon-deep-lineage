package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class Classification< T >
{
	private final Set< Set< T > > classifiedObjects;

	@Nullable
	private final Cluster algorithmResult;

	@Nullable
	private final Map< String, T > objectMapping;

	private final double cutoff;

	public Classification(
			final Set< Set< T > > classifiedObjects, @Nullable final Cluster algorithmResult,
			@Nullable final Map< String, T > objectMapping, double cutoff )
	{
		this.classifiedObjects = classifiedObjects;
		this.algorithmResult = algorithmResult;
		this.objectMapping = objectMapping;
		this.cutoff = cutoff;
	}

	public Set< Set< T > > getClassifiedObjects()
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
