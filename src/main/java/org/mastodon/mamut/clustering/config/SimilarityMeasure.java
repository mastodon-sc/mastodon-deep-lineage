/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.clustering.config;

import org.apache.commons.lang3.function.TriFunction;
import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public enum SimilarityMeasure
{
	NORMALIZED_DIFFERENCE( "Normalized Zhang Tree Distance", ZhangUnorderedTreeEditDistance::normalizedDistance ),
	AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE( "Per Branch Spot Zhang Tree Distance", ZhangUnorderedTreeEditDistance::averageDistance ),
	ABSOLUTE_DIFFERENCE( "Zhang Tree Distance", ZhangUnorderedTreeEditDistance::distance );

	private final String name;

	private final TriFunction< Tree< Double >, Tree< Double >, BiFunction< Double, Double, Double >, Double > distanceFunction;

	SimilarityMeasure( String name,
			TriFunction< Tree< Double >, Tree< Double >, BiFunction< Double, Double, Double >, Double > distanceFunction )
	{
		this.name = name;
		this.distanceFunction = distanceFunction;
	}

	public double compute( Tree< Double > tree1, Tree< Double > tree2, BinaryOperator< Double > costFunction )
	{
		return distanceFunction.apply( tree1, tree2, costFunction );
	}

	@Override
	public String toString()
	{
		return name;
	}
}
