package org.mastodon.mamut.clustering.config;

import org.apache.commons.lang3.function.TriFunction;
import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public enum SimilarityMeasure
{
	NORMALIZED_DIFFERENCE( "Normalized Zhang Tree Distance", ZhangUnorderedTreeEditDistance::normalizedDistance ),
	AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE( "Per Cell Zhang Tree Distance", ZhangUnorderedTreeEditDistance::averageDistance ),
	ABSOLUTE_DIFFERENCE( "Zhang Tree Distance", ZhangUnorderedTreeEditDistance::distance );

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

		throw new NoSuchElementException();
	}

	public double compute( Tree< Double > tree1, Tree< Double > tree2, BinaryOperator< Double > costFunction )
	{
		return distanceFunction.apply( tree1, tree2, costFunction );
	}

	public String getName()
	{
		return name;
	}
}
