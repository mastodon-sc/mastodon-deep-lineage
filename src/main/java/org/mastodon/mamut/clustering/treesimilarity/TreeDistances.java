/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.clustering.treesimilarity;

import org.mastodon.mamut.clustering.treesimilarity.tree.Tree;
import org.mastodon.mamut.clustering.treesimilarity.tree.TreeUtils;

import javax.annotation.Nullable;
import java.util.function.ToDoubleBiFunction;

/**
 * Utility class for calculating distances between trees.
 */
public class TreeDistances
{

	private TreeDistances()
	{
		// prevent from instantiation
	}

	/**
	 * Cost function as used in the treex library tests that returns the absolute value of the difference between two attributes,
	 * if both attributes exist or the attribute value of the other, if one attribute is {@code null}.
	 *
	 * @see <a href="https://gitlab.inria.fr/mosaic/treex/-/blob/master/test/test_analysis/test_zhang_labeled_trees.py?ref_type=heads#L99">treex library</a>
	 */
	public static final ToDoubleBiFunction< Double, Double > LOCAL_ABSOLUTE_COST_FUNCTION = TreeDistances::localAbsoluteCostFunction;

	/**
	 * Cost function as used in Guignard et al. 2020. It returns the normalized absolute difference between two attributes or 1 if one attribute is {@code null}.
	 *
	 * @see <a href="https://www.science.org/doi/suppl/10.1126/science.aar5663/suppl_file/aar5663_guignard_sm.pdf">Guignard et al. (2020) Page 38-39</a>
	 */
	public static final ToDoubleBiFunction< Double, Double > LOCAL_NORMALIZED_COST_FUNCTION = TreeDistances::localNormalizedCostFunction;

	/**
	 * Calculates the normalized Zhang edit distance between two labeled unordered trees.
	 * <br>
	 * The normalized distance is defined as the absolute distance divided by the sum of the distances to empty/null trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction mandatory cost function.
	 * @param <T> Attribute type of the tree nodes.
	 *
	 * @return The normalized Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double normalizedDistance( @Nullable final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final ToDoubleBiFunction< T, T > costFunction )
	{
		double denominator = ZhangUnorderedTreeEditDistance.distance( tree1, null, costFunction )
				+ ZhangUnorderedTreeEditDistance.distance( null, tree2, costFunction );
		// NB: avoid division by zero. Two empty trees are considered equal and also two trees with zero edit distance are considered equal.
		if ( denominator == 0 )
			return 0;
		return ZhangUnorderedTreeEditDistance.distance( tree1, tree2, costFunction ) / denominator;
	}

	/**
	 * Calculates the average Zhang edit distance between two labeled unordered trees.
	 * <br>
	 * The average distance is defined as the absolute distance divided by the sum of the sizes (i.e. number of nodes) of the trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction mandatory cost function.
	 * @param <T> Attribute type of the tree nodes.
	 *
	 * @return The average Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double averageDistance( @Nullable final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final ToDoubleBiFunction< T, T > costFunction )
	{
		double denominator = ( double ) TreeUtils.size( tree1 ) + ( double ) TreeUtils.size( tree2 );
		// NB: avoid division by zero. Two empty trees are considered equal.
		if ( denominator == 0 )
			return 0;
		return ZhangUnorderedTreeEditDistance.distance( tree1, tree2, costFunction ) / denominator;
	}

	/**
	 * @see <a href="https://gitlab.inria.fr/mosaic/treex/-/blob/master/test/test_analysis/test_zhang_labeled_trees.py?ref_type=heads#L99">treex library</a>
	 */
	private static Double localAbsoluteCostFunction( final Double o1, final Double o2 )
	{
		if ( o2 == null )
			return o1;
		else if ( o1 == null )
			return o2;
		else
			return Math.abs( o1 - o2 );
	}

	/**
	 * @see <a href="https://www.science.org/doi/suppl/10.1126/science.aar5663/suppl_file/aar5663_guignard_sm.pdf">Guignard et al. (2020) Page 38</a>
	 */
	private static Double localNormalizedCostFunction( final Double o1, final Double o2 )
	{
		if ( o1 == null || o2 == null )
			return 1d;
		else if ( o1.equals( o2 ) ) // NB: avoid non-required division and possible division by zero
			return 0d;
		else
			return Math.abs( o1 - o2 ) / ( o1 + o2 );
	}
}
