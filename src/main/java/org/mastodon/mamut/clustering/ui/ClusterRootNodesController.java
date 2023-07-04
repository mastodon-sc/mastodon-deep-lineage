package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;

public interface ClusterRootNodesController< T >
{
	void createTagSet();

	void setCropCriterion( CropCriteria cropCriterion );

	void setSimilarityMeasure( SimilarityMeasure similarityMeasure );

	void setClusteringMethod( ClusteringMethod clusteringMethod );

	void setCropStart( int cropStart );

	void setCropEnd( int cropEnd );

	void setNumberOfClasses( int numberOfClasses );

	void setMinCellDivisions( int minCellDivisions );

	CropCriteria getCropCriterion();

	SimilarityMeasure getSimilarityMeasure();

	ClusteringMethod getClusteringMethod();

	int getCropStart();

	int getCropEnd();

	int getNumberOfClasses();

	int getMinCellDivisions();

	void addListener( final ClusterRootNodesListener< T > listener );
}
