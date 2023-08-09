package org.mastodon.mamut.clustering.config;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;

import java.util.NoSuchElementException;

public enum ClusteringMethod
{
	AVERAGE_LINKAGE( "Average linkage", new AverageLinkageStrategy() ),
	SINGLE_LINKAGE( "Single Linkage", new SingleLinkageStrategy() ),
	COMPLETE_LINKAGE( "Complete Linkage", new CompleteLinkageStrategy() );

	private final String name;

	private final LinkageStrategy linkageStrategy;

	ClusteringMethod( String name, LinkageStrategy linkageStrategy )
	{
		this.name = name;
		this.linkageStrategy = linkageStrategy;
	}

	public String getName()
	{
		return name;
	}

	public static ClusteringMethod getByName(final String name) {
		for (final ClusteringMethod method : values())
			if (method.getName().equals(name))
				return method;

		throw new NoSuchElementException();
	}

	public LinkageStrategy getLinkageStrategy()
	{
		return linkageStrategy;
	}
}
