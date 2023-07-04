package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;

import javax.annotation.Nullable;
import java.util.Map;

public interface ClusterRootNodesListener< T >
{
	void clusterRootNodesComputed( @Nullable Cluster cluster, @Nullable final Map< String, T > objectMapping, final double cutoff );
}
