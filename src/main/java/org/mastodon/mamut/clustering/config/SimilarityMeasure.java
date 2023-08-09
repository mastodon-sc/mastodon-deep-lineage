package org.mastodon.mamut.clustering.config;

import lombok.Getter;
import org.apache.commons.lang3.function.TriFunction;
import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public enum SimilarityMeasure
{
	NORMALIZED_DIFFERENCE( "Normalized difference", ZhangUnorderedTreeEditDistance::normalizedDistance ),
	AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE( "Average difference per cell life cycle", ZhangUnorderedTreeEditDistance::averageDistance ),
	ABSOLUTE_DIFFERENCE( "Absolute difference", ZhangUnorderedTreeEditDistance::distance );

	@Getter
	private final String name;

	private final TriFunction< Tree< Double >, Tree< Double >, BiFunction< Double, Double, Double >, Double > distanceFunction;

	SimilarityMeasure( String name,
			TriFunction< Tree< Double >, Tree< Double >, BiFunction< Double, Double, Double >, Double > distanceFunction )
	{
		this.name = name;
		this.distanceFunction = distanceFunction;
	}

	public static SimilarityMeasure getByName(final String name)
	{
		for (final SimilarityMeasure measure : values())
			if (measure.getName().equals(name))
				return measure;

		return null;
	}

	public double compute( Tree< Double > tree1, Tree< Double > tree2, BinaryOperator< Double > costFunction )
	{
		return distanceFunction.apply( tree1, tree2, costFunction );
	}
}