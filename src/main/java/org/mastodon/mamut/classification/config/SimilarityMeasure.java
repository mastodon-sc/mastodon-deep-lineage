/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.classification.config;

import org.apache.commons.lang3.function.TriFunction;
import org.mastodon.mamut.treesimilarity.TreeDistances;
import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.NoSuchElementException;
import java.util.function.ToDoubleBiFunction;

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

	private final TriFunction< Tree< Double >, Tree< Double >, ToDoubleBiFunction< Double, Double >, Double > distanceFunction;

	private final ToDoubleBiFunction< Double, Double > costFunction;

	SimilarityMeasure( final String name,
			final TriFunction< Tree< Double >, Tree< Double >, ToDoubleBiFunction< Double, Double >, Double > distanceFunction,
			final ToDoubleBiFunction< Double, Double > costFunction )
	{
		this.name = name;
		this.distanceFunction = distanceFunction;
		this.costFunction = costFunction;
	}

	public static SimilarityMeasure getByName(final String name)
	{
		for (final SimilarityMeasure measure : values())
			if (measure.getName().equals(name))
				return measure;

		throw new NoSuchElementException();
	}

	public double compute( final Tree< Double > tree1, final Tree< Double > tree2 )
	{
		return distanceFunction.apply( tree1, tree2, costFunction );
	}

	public String getName()
	{
		return name;
	}
}
