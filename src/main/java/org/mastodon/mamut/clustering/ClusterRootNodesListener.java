package org.mastodon.mamut.clustering;

import com.apporiented.algorithm.clustering.Cluster;

import javax.annotation.Nullable;
import java.util.Map;

public interface ClusterRootNodesListener< T >
{
	void clusterRootNodesComputed( @Nullable final Cluster cluster, @Nullable final Map< String, T > objectMapping, final double cutoff );

	void cropCriterionChanged( final double start, final double end );
}
