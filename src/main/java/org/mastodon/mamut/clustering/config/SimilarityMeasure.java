package org.mastodon.mamut.clustering.config;

import java.util.NoSuchElementException;

import org.mastodon.mamut.clustering.treesimilarity.TreeDistances;
import org.mastodon.mamut.clustering.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.clustering.treesimilarity.tree.Tree;
import org.mastodon.mamut.util.ToDoubleQuadFunction;
import org.mastodon.mamut.util.ToDoubleTriFunction;

public enum SimilarityMeasure implements HasName
{
	NORMALIZED_ZHANG_DIFFERENCE( "Normalized Zhang Tree Distance", TreeDistances::normalizedDistance,
			TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION
	),
	NORMALIZED_ZHANG_DIFFERENCE_WITH_LOCAL_NORMALIZATION( "Normalized Zhang Tree Distance (with additional local normalization)",
			TreeDistances::normalizedDistance, TreeDistances.LOCAL_NORMALIZED_COST_FUNCTION
	),
	PER_BRANCH_ZHANG_DISTANCE( "Per Branch Zhang Tree Distance", TreeDistances::averageDistance,
			TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION
	),
	ZHANG_DISTANCE( "Zhang Tree Distance", ZhangUnorderedTreeEditDistance::distance,
			TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION
	),
	ZHANG_DISTANCE_WITH_LOCAL_NORMALIZATION( "Zhang Tree Distance (with additional local normalization)",
			ZhangUnorderedTreeEditDistance::distance, TreeDistances.LOCAL_NORMALIZED_COST_FUNCTION
	);

	private final String name;

	private final ToDoubleQuadFunction< Tree< Double >, Tree< Double >, ToDoubleTriFunction< Double, Double, Double >,
			Double > distanceFunction;

	private final ToDoubleTriFunction< Double, Double, Double > costFunctionWithScale;

	SimilarityMeasure( final String name, final ToDoubleQuadFunction< Tree< Double >, Tree< Double >,
			ToDoubleTriFunction< Double, Double, Double >, Double > distanceFunction,
			final ToDoubleTriFunction< Double, Double, Double > costFunctionWithScale )
	{
		this.name = name;
		this.distanceFunction = distanceFunction;
		this.costFunctionWithScale = costFunctionWithScale;
	}

	public static SimilarityMeasure getByName( final String name )
	{
		for ( final SimilarityMeasure measure : values() )
			if ( measure.getName().equals( name ) )
				return measure;

		throw new NoSuchElementException();
	}

	public double compute( final Tree< Double > tree1, final Tree< Double > tree2, final Double scale )
	{
		return distanceFunction.applyAsDouble( tree1, tree2, costFunctionWithScale, scale );
	}

	public String getName()
	{
		return name;
	}
}
