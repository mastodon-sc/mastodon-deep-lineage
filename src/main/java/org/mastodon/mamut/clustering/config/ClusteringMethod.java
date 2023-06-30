package org.mastodon.mamut.clustering.config;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;

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

	public LinkageStrategy getLinkageStrategy()
	{
		return linkageStrategy;
	}
}