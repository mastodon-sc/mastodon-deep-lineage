package org.mastodon.mamut.feature.dimensionalityreduction;

public enum DimensionalityReductionAlgorithm
{
	UMAP( "UMAP", "Uniform Manifold Approximation and Projection for Dimension Reduction." ),
	TSNE( "t-SNE", "t-distributed Stochastic Neighbor Embedding." );

	private final String name;

	private final String description;

	DimensionalityReductionAlgorithm( final String name, final String description )
	{
		this.name = name;
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
