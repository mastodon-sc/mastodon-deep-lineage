package org.mastodon.mamut.clustering.config;

public class Settings
{
	private CropCriteria cropCriterion;

	private SimilarityMeasure similarityMeasure;

	private ClusteringMethod clusteringMethod;

	private int cropStart;

	private int cropEnd;

	private int numberOfClasses;

	public Settings()
	{
		this.cropCriterion = CropCriteria.TIMEPOINT;
		this.cropStart = 0;
		this.cropEnd = Integer.MAX_VALUE;
		this.numberOfClasses = 3;
		this.similarityMeasure = SimilarityMeasure.NORMALIZED_DIFFERENCE;
		this.clusteringMethod = ClusteringMethod.AVERAGE_LINKAGE;
	}

	public CropCriteria getCropCriterion()
	{
		return cropCriterion;
	}

	public void setCropCriterion( CropCriteria cropCriterion )
	{
		this.cropCriterion = cropCriterion;
	}

	public SimilarityMeasure getSimilarityMeasure()
	{
		return similarityMeasure;
	}

	public void setSimilarityMeasure( SimilarityMeasure similarityMeasure )
	{
		this.similarityMeasure = similarityMeasure;
	}

	public ClusteringMethod getClusteringMethod()
	{
		return clusteringMethod;
	}

	public void setClusteringMethod( ClusteringMethod clusteringMethod )
	{
		this.clusteringMethod = clusteringMethod;
	}

	public int getCropStart()
	{
		return cropStart;
	}

	public void setCropStart( int start )
	{
		this.cropStart = start;
	}

	public int getCropEnd()
	{
		return cropEnd;
	}

	public void setCropEnd( int end )
	{
		this.cropEnd = end;
	}

	public int getNumberOfClasses()
	{
		return numberOfClasses;
	}

	public void setNumberOfClasses( int numberOfClasses )
	{
		this.numberOfClasses = numberOfClasses;
	}
}
