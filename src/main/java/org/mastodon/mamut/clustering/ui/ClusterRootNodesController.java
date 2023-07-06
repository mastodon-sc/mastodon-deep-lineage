package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;

public interface ClusterRootNodesController< T >
{
	void createTagSet();

	void setCropCriterion( final CropCriteria cropCriterion );

	void setSimilarityMeasure( final SimilarityMeasure similarityMeasure );

	void setClusteringMethod( final ClusteringMethod clusteringMethod );

	void setCropStart( final int cropStart );

	void setCropEnd( final int cropEnd );

	void setNumberOfClasses( final int numberOfClasses );

	void setMinCellDivisions( final int minCellDivisions );

	CropCriteria getCropCriterion();

	SimilarityMeasure getSimilarityMeasure();

	ClusteringMethod getClusteringMethod();

	int getCropStart();

	int getCropEnd();

	int getNumberOfClasses();

	int getMinCellDivisions();

	void addListener( final ClusterRootNodesListener< T > listener );
}
