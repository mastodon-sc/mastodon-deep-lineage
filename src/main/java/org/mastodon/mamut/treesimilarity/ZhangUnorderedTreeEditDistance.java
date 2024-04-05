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
package org.mastodon.mamut.treesimilarity;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.treesimilarity.tree.Tree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;
import org.mastodon.mamut.treesimilarity.util.FlowNetwork;
import org.mastodon.mamut.treesimilarity.util.NodeMapping;
import org.mastodon.mamut.treesimilarity.util.NodeMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Implementation of "A Constrained Edit Distance Between Unordered Labeled Trees", Kaizhong Zhang, Algorithmica (1996) 15:205-222<br>
 *
 * The Zhang unordered edit distance allows the following edit operations:
 *
 * <pre>
 *
 * Note: The prefix T may represent a node or a complete subtree. Nodes without this prefix are just nodes.
 *
 * 1. Change label
 *
 *       A         A'
 *      / \  -&gt;   / \
 *     TB TC     TB TC
 *
 *
 * 2a: Remove subtree (opposite of 2b)
 *
 *       A         A
 *      / \   -&gt;   |
 *     TB TC       TB
 *
 * 2b: Add new subtree (opposite of 2a)
 *
 *       A          A
 *       |    -&gt;   / \
 *       TB       TB TC
 *
 *
 * 3a: Remove subtree but keep one child (opposite of 3b)
 *
 *       A          A
 *      / \   -&gt;   / \
 *     B  TC      TD TC
 *    / \
 *   TD TE        (remove B and TE, keep TD)
 *
 * 3b: Convert existing subtree into child of a newly inserted subtree (opposite of 3a)
 *       A             A
 *      / \    -&gt;     / \
 *     TB TC         D  TC
 *                  / \
 *                 TB TE       (insert D and TE, keep TB)
 *
 *
 * 4a: Remove subtree (and siblings) but keep all children (opposite of 4b)
 *       A               A
 *      / \             / \
 *     B  TC   -&gt;      TD TE
 *    / \
 *   TD TE            (Subtree B and it's sibling TC are removed, but the children
 *                     of B namely TD and TE are kept)
 *
 * 4b: Convert existing subtrees into children of a newly inserted subtree (opposite of 4a)
 *       A               A
 *      / \             / \
 *     TB TC   -&gt;      D  TE
 *                    / \
 *                   TB TC       (Subtree D and it's sibling TE are newly inserted,
 *                                TB and TC are kept as children of D)
 * </pre>
 * @param <T> Attribute type of the tree nodes.
 *
 * @author Stefan Hahmann
 * @author Matthias Arzt
 */
public class ZhangUnorderedTreeEditDistance< T >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Map< Tree< T >, TreeDetails > insertCosts;

	private final Map< Tree< T >, TreeDetails > deleteCosts;

	private final Map< Pair< Tree< T >, Tree< T > >, NodeMapping< T > > treeDistances;

	private final Map< Pair< Tree< T >, Tree< T > >, NodeMapping< T > > forestDistances;

	private final List< Tree< T > > subtrees1;

	private final List< Tree< T > > subtrees2;

	private final Map< Pair< Tree< T >, Tree< T > >, Double > attributeDistances;

	/**
	 * Cost function as used in the treex library tests that returns the absolute value of the difference between two attributes,
	 * if both attributes exist or the attribute value of the other, if one attribute is {@code null}.
	 *
	 * @see <a href="https://gitlab.inria.fr/mosaic/treex/-/blob/master/test/test_analysis/test_zhang_labeled_trees.py?ref_type=heads#L99">treex library</a>
	 */
	public static final BinaryOperator< Double > TREE_X_COST_FUNCTION = ZhangUnorderedTreeEditDistance::treeXCostFunction;

	/**
	 * Cost function as used in Guignard et al. 2020. It returns the normalized absolute difference between two attributes or 1 if one attribute is {@code null}.
	 *
	 * @see <a href="https://www.science.org/doi/suppl/10.1126/science.aar5663/suppl_file/aar5663_guignard_sm.pdf">Guignard et al. (2020) Page 38-39</a>
	 */
	public static final BinaryOperator< Double > GUIGNARD_COST_FUNCTION = ZhangUnorderedTreeEditDistance::guignardCostFunction;

	/**
	 * Summarizer function to calculate the sum of the attribute values of a list of attributes.
	 */
	public static final ToDoubleFunction< List< Double > > ATTRIBUTE_SUMMARIZER =
			list -> list.stream().mapToDouble( Double::doubleValue ).sum();

	/**
	 * Calculates the absolute Zhang edit distance between two labeled unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction mandatory cost function.
	 * @param <T> Attribute type of the tree nodes.
	 *
	 * @return The absolute Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double distance( @Nullable final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final BiFunction< T, T, Double > costFunction )
	{
		if ( costFunction == null )
			throw new IllegalArgumentException( "The cost function is expected to be non-null, but it is null." );

		// trivial cases
		if ( tree1 == null && tree2 == null )
			return 0;
		if ( tree1 == null )
			return distanceTreeToNull( tree2, costFunction );
		else if ( tree2 == null )
			return distanceTreeToNull( tree1, costFunction );

		ZhangUnorderedTreeEditDistance< T > zhang = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction );
		return zhang.compute( tree1, tree2 );
	}

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
			final BiFunction< T, T, Double > costFunction )
	{
		double denominator = distance( tree1, null, costFunction ) + distance( null, tree2, costFunction );
		// NB: avoid division by zero. Two empty trees are considered equal. Two trees with zero distance are considered equal.
		if ( denominator == 0 )
			return 0;
		return distance( tree1, tree2, costFunction ) / denominator;
	}

	/**
	 * Calculates the normalized Zhang edit distance between two labeled unordered trees.
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
			final BiFunction< T, T, Double > costFunction )
	{
		double denominator = ( double ) TreeUtils.size( tree1 ) + ( double ) TreeUtils.size( tree2 );
		// NB: avoid division by zero. Two empty trees are considered equal. Two trees with zero distance are considered equal.
		if ( denominator == 0 )
			return 0;
		return distance( tree1, tree2, costFunction ) / denominator;
	}

	/**
	 * Calculates the average Zhang edit distance between two labeled unordered trees using averaging by the combined sum of the attribute values.
	 * <br>
	 * @see <a href="https://www.science.org/doi/suppl/10.1126/science.aar5663/suppl_file/aar5663_guignard_sm.pdf">Guignard et al. (2020) Page 38-39</a>
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction mandatory cost function.
	 * @param attributeSummarizer function to summarize the attribute values of the trees.
	 * @param <T> Attribute type of the tree nodes.
	 *
	 * @return The average Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double guignardAverageDistance(
			@Nullable final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final BiFunction< T, T, Double > costFunction, final ToDoubleFunction< List< T > > attributeSummarizer
	)
	{
		List< T > attributes = TreeUtils.getAllAttributes( tree1 );
		attributes.addAll( TreeUtils.getAllAttributes( tree2 ) );
		double denominator = attributeSummarizer.applyAsDouble( attributes );
		// NB: avoid division by zero. Two trees without any attribute values are considered equal.
		if ( denominator == 0 )
			return 0;
		return distance( tree1, tree2, costFunction ) / denominator;
	}

	/**
	 * Calculates a mapping between nodes in the given two trees ({@code tree1} and {@code tree2}) that links the nodes from the two trees, which have the minimum tree edit distance to each other.<br>
	 * The required minimum tree edit distance is calculated using the Zhang unordered edit distance.
	 * @param tree1 The first tree.
	 * @param tree2 The second tree.
	 * @param costFunction The cost function.
	 * @param <T> Attribute type of the tree nodes.
	 *
	 * @return The mapping between nodes.
	 */
	public static < T > Map< Tree< T >, Tree< T > > nodeMapping( Tree< T > tree1, Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		if ( tree1 == null || tree2 == null )
			return Collections.emptyMap();

		NodeMapping< T > mapping = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction ).treeMapping( tree1, tree2 );
		return mapping.asMap();
	}

	private static < T > double distanceTreeToNull( Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		double distance = 0;
		for ( Tree< T > subtree : TreeUtils.listOfSubtrees( tree2 ) )
			distance += costFunction.apply( null, subtree.getAttribute() );
		return distance;
	}

	private ZhangUnorderedTreeEditDistance( final Tree< T > tree1, final Tree< T > tree2,
			final BiFunction< T, T, Double > costFunction )
	{

		subtrees1 = TreeUtils.listOfSubtrees( tree1 );
		subtrees2 = TreeUtils.listOfSubtrees( tree2 );

		attributeDistances = new HashMap<>();
		for ( Tree< T > subtree1 : subtrees1 )
		{
			for ( Tree< T > subtree2 : subtrees2 )
			{
				attributeDistances.put( Pair.of( subtree1, subtree2 ),
						costFunction.apply( subtree1.getAttribute(), subtree2.getAttribute() ) );
			}
		}

		insertCosts = new EditCosts<>( tree2, costFunction ).costs;
		deleteCosts = new EditCosts<>( tree1, costFunction ).costs;

		treeDistances = new HashMap<>();
		forestDistances = new HashMap<>();
	}

	/**
	 * @see <a href="https://gitlab.inria.fr/mosaic/treex/-/blob/master/test/test_analysis/test_zhang_labeled_trees.py?ref_type=heads#L99">treex library</a>
	 */
	private static Double treeXCostFunction( final Double o1, final Double o2 )
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
	private static Double guignardCostFunction( final Double o1, final Double o2 )
	{
		if ( o1 == null || o2 == null )
			return 1d;
		else if ( o1.equals( o2 ) ) // NB: avoid non-required division and possible division by zero
			return 0d;
		else
			return Math.abs( o1 - o2 ) / ( o1 + o2 );
	}

	/**
	 * Calculate the Zhang edit distance between two (labeled) unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 *
	 * @return The Zhang edit distance between tree1 and tree2 as an integer.
	 */
	private double compute( final Tree< T > tree1, final Tree< T > tree2 )
	{
		treeDistances.clear();
		forestDistances.clear();
		double distance = treeMapping( tree1, tree2 ).getCost();

		log();

		return distance;
	}

	private void log()
	{
		if ( !logger.isTraceEnabled() )
			return;
		logDistances( "tree", treeDistances );
		logDistances( "forest", forestDistances );

		logger.trace( "tree deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
			logger.trace( "tree deletion[{}] = {}", subtree, deleteCosts.get( subtree ).treeCost );

		logger.trace( "forest deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
			logger.trace( "forest deletion[{}] = {}", subtree, deleteCosts.get( subtree ).forestCost );

		logger.trace( "tree insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
			logger.trace( "tree insertion[{}] = {}", subtree, insertCosts.get( subtree ).treeCost );

		logger.trace( "forest insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
			logger.trace( "forest insertion[{}] = {}", subtree, insertCosts.get( subtree ).forestCost );
	}

	private void logDistances( String prefix, Map< Pair< Tree< T >, Tree< T > >, NodeMapping< T > > distances )
	{
		if ( !logger.isTraceEnabled() )
			return;
		logger.trace( "matrix of {} distances:", prefix );
		for ( Tree< T > t1 : subtrees1 )
		{
			StringJoiner stringJoiner = new StringJoiner( ", ", "[", "]" );
			for ( Tree< T > t2 : subtrees2 )
			{
				NodeMapping< T > editOperation = distances.get( Pair.of( t1, t2 ) );
				stringJoiner.add( editOperation == null ? "-" : Double.toString( editOperation.getCost() ) );
			}
			logger.trace( "{} distance[{}] = {}", prefix, t1, stringJoiner );
		}
	}

	/**
	 * Returns the optimal node mapping with respect to zhang edit distance
	 * that maps from tree1 to tree2.
	 */
	private NodeMapping< T > treeMapping( Tree< T > tree1, Tree< T > tree2 )
	{
		Pair< Tree< T >, Tree< T > > pair = Pair.of( tree1, tree2 );
		NodeMapping< T > operation = treeDistances.get( pair );
		if ( operation == null )
		{
			operation = computeTreeMapping( tree1, tree2 );
			treeDistances.put( pair, operation );
		}
		return operation;
	}

	private NodeMapping< T > computeTreeMapping( Tree< T > tree1, Tree< T > tree2 )
	{
		NodeMapping< T > attributeMapping = NodeMappings.singleton( attributeDistances.get( Pair.of( tree1, tree2 ) ), tree1, tree2 );
		if ( tree1.isLeaf() && tree2.isLeaf() )
			return attributeMapping;

		// NB: the order of the following three lines is important, changing the order will result in a wrong distance.
		NodeMapping< T > insertOperationCosts = insertOperationMapping( tree1, tree2 );
		NodeMapping< T > deleteOperationCosts = deleteOperationMapping( tree1, tree2 );
		NodeMapping< T > changeCosts = NodeMappings.compose( attributeMapping, forestMapping( tree1, tree2 ) );
		return findBestMapping( insertOperationCosts, deleteOperationCosts, changeCosts );
	}

	/**
	 * Returns the optimal node mapping with respect to zhang edit distance
	 * that maps from forest1 to forest2.
	 * <br>
	 * <strong>What is a forest?</strong>
	 * <br>
	 * "Suppose that we have a numbering for each tree.
	 * <ul>
	 * <li>Let t[i] be the i<sup>th</sup> node of tree T in the given numbering.</li>
	 * <li>Let T[i] be the subtree rooted at t[i]</li>
	 * <li>Let F[i] be the unordered forest obtained by deleting t[i] from T[i]."</li>
	 * </ul>
	 * Algorithmica (1996) 15:208
	 */
	private NodeMapping< T > forestMapping( final Tree< T > forest1, final Tree< T > forest2 )
	{
		Pair< Tree< T >, Tree< T > > pair = Pair.of( forest1, forest2 );
		NodeMapping< T > operation = forestDistances.get( pair );
		if ( operation == null )
		{
			operation = computeForestMapping( forest1, forest2 );
			forestDistances.put( pair, operation );
		}
		return operation;
	}

	private NodeMapping< T > computeForestMapping( Tree< T > forest1, Tree< T > forest2 )
	{
		boolean forest1IsLeaf = forest1.isLeaf();
		boolean forest2IsLeaf = forest2.isLeaf();

		if ( forest1IsLeaf && forest2IsLeaf )
			throw new IllegalArgumentException( "The given trees are both leaves and thus they are both not forests." );

		if ( forest1IsLeaf )
			return NodeMappings.empty( insertCosts.get( forest2 ).forestCost );

		if ( forest2IsLeaf )
			return NodeMappings.empty( deleteCosts.get( forest1 ).forestCost );

		NodeMapping< T > forestInsertCosts = forestInsertMapping( forest1, forest2 );
		NodeMapping< T > forestDeleteCosts = forestDeleteMapping( forest1, forest2 );
		NodeMapping< T > changeCosts = minCostMaxFlow( forest1, forest2 );
		return findBestMapping( forestInsertCosts, forestDeleteCosts, changeCosts );
	}

	/**
	 * Returns the best {@link NodeMapping} that uses edit operation (3b)
	 * to map {@code tree1} to {@code tree2}. See {@link ZhangUnorderedTreeEditDistance}.
	 * <br>
	 * Costs for deleting tree1 but keeping a child-tree of tree1, and changing that child-tree to tree2.
	 */
	private NodeMapping< T > insertOperationMapping( Tree< T > tree1, Tree< T > tree2 )
	{
		double insertCostTree2 = insertCosts.get( tree2 ).treeCost;
		return findBestMapping( tree2.getChildren(), child ->
		{
			NodeMapping< T > insertMapping = NodeMappings.empty( insertCostTree2 - this.insertCosts.get( child ).treeCost );
			NodeMapping< T > childMapping = treeMapping( tree1, child );
			return NodeMappings.compose( insertMapping, childMapping );
		} );
	}

	/**
	 * Returns the best {@link NodeMapping} that uses edit operation (3a)
	 * to map {@code tree1} to {@code tree2}. See {@link ZhangUnorderedTreeEditDistance}.
	 * <br>
	 * Costs for deleting tree1 but keeping a child-tree of tree1, and changing that child-tree to tree2.
	 */
	private NodeMapping< T > deleteOperationMapping( Tree< T > tree1, Tree< T > tree2 )
	{
		double deleteCostTree1 = deleteCosts.get( tree1 ).treeCost;
		return findBestMapping( tree1.getChildren(), child ->
		{
			NodeMapping< T > deleteMapping = NodeMappings.empty( deleteCostTree1 - this.deleteCosts.get( child ).treeCost );
			NodeMapping< T > childMapping = treeMapping( child, tree2 );
			return NodeMappings.compose( deleteMapping, childMapping );
		} );
	}

	/**
	 * Returns the best {@link NodeMapping} that uses edit operation (4b)
	 * to map {@code forest1} to {@code forest2}. See {@link ZhangUnorderedTreeEditDistance}.
	 */
	private NodeMapping< T > forestInsertMapping( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		double insertCostForest2 = insertCosts.get( forest2 ).forestCost;
		return findBestMapping( forest2.getChildren(), child ->
		{
			NodeMapping< T > insertMapping = NodeMappings.empty( insertCostForest2 - this.insertCosts.get( child ).forestCost );
			NodeMapping< T > childMapping = forestMapping( forest1, child );
			return NodeMappings.compose( insertMapping, childMapping );
		} );
	}

	/**
	 * Returns the best {@link NodeMapping} that uses edit operation (4a)
	 * to map {@code forest1} to {@code forest2}. See {@link ZhangUnorderedTreeEditDistance}.
	 */
	private NodeMapping< T > forestDeleteMapping( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		double deleteCostForest1 = deleteCosts.get( forest1 ).forestCost;
		return findBestMapping( forest1.getChildren(), child ->
		{
			NodeMapping< T > deleteMapping = NodeMappings.empty( deleteCostForest1 - this.deleteCosts.get( child ).forestCost );
			NodeMapping< T > childMapping = forestMapping( child, forest2 );
			return NodeMappings.compose( deleteMapping, childMapping );
		} );
	}

	/**
	 * Invokes the given {@code function} for each of the given {@code children}.
	 * Each function invocation must return a {@link NodeMapping}. This method returns
	 * the best {@link NodeMapping}, i.e. the one with the lowest cost.
	 */
	private NodeMapping< T > findBestMapping( Collection< Tree< T > > children, Function< Tree< T >, NodeMapping< T > > function )
	{
		NodeMapping< T > best = NodeMappings.empty( Double.POSITIVE_INFINITY );
		for ( Tree< T > child : children )
		{
			NodeMapping< T > nodeMapping = function.apply( child );
			if ( nodeMapping.getCost() < best.getCost() )
				best = nodeMapping;
		}
		return best;
	}

	private NodeMapping< T > minCostMaxFlow( final Tree< T > forest1, final Tree< T > forest2 )
	{
		// Construction of graph for max flow min cost algorithm

		Collection< Tree< T > > childrenForest1 = forest1.getChildren();
		Collection< Tree< T > > childrenForest2 = forest2.getChildren();

		String source = "source";
		String sink = "sink";
		String emptyTree1 = "empty1";
		String emptyTree2 = "empty2";

		FlowNetwork network = buildFlowNetwork( source, sink, emptyTree1, emptyTree2, childrenForest1, childrenForest2 );

		network.solveMaxFlowMinCost( source, sink );

		ArrayList< NodeMapping< T > > childMappings = new ArrayList<>();

		for ( Tree< T > child1 : childrenForest1 )
			if ( isFlowEqualToOne( network.getFlow( child1, emptyTree2 ) ) )
				childMappings.add( NodeMappings.empty( deleteCosts.get( child1 ).treeCost ) );

		for ( Tree< T > child2 : childrenForest2 )
			if ( isFlowEqualToOne( network.getFlow( emptyTree1, child2 ) ) )
				childMappings.add( NodeMappings.empty( insertCosts.get( child2 ).treeCost ) );

		for ( Tree< T > child1 : childrenForest1 )
			for ( Tree< T > child2 : childrenForest2 )
				if ( isFlowEqualToOne( network.getFlow( child1, child2 ) ) )
					childMappings.add( treeMapping( child1, child2 ) );

		return NodeMappings.compose( childMappings );
	}

	private FlowNetwork buildFlowNetwork(
			String source, String sink, String emptyTree1, String emptyTree2, Collection< Tree< T > > childrenForest1,
			Collection< Tree< T > > childrenForest2
	)
	{
		FlowNetwork network = new FlowNetwork();
		network.addVertices( Arrays.asList( source, sink, emptyTree1, emptyTree2 ) );
		network.addVertices( childrenForest1 );
		network.addVertices( childrenForest2 );

		int numberOfChildrenForest1 = childrenForest1.size();
		int numberOfChildrenForest2 = childrenForest2.size();
		int minNumberOfChildren = Math.min( numberOfChildrenForest1, numberOfChildrenForest2 );
		network.addEdge( source, emptyTree1, numberOfChildrenForest2 - minNumberOfChildren, 0 );
		network.addEdge( emptyTree2, sink, numberOfChildrenForest1 - minNumberOfChildren, 0 );

		for ( Tree< T > child1 : childrenForest1 )
		{
			network.addEdge( source, child1, 1, 0 );
			network.addEdge( child1, emptyTree2, 1, deleteCosts.get( child1 ).treeCost );
			for ( Tree< T > child2 : childrenForest2 )
				network.addEdge( child1, child2, 1, treeMapping( child1, child2 ).getCost() );
		}

		for ( Tree< T > child2 : childrenForest2 )
		{
			network.addEdge( child2, sink, 1, 0 );
			network.addEdge( emptyTree1, child2, 1, insertCosts.get( child2 ).treeCost );
		}
		return network;
	}

	/**
	 * Returns true if the flow value equal to 1. Returns false if the flow value equal to 0.
	 * Throws an {@link AssertionError} if the flow value is neither 0 nor 1.
	 */
	private static boolean isFlowEqualToOne( double flowValue )
	{
		if ( flowValue != 0.0 && flowValue != 1.0 )
			throw new AssertionError( "Invalid flow value: " + flowValue );
		return flowValue == 1.0;
	}

	private static < T > NodeMapping< T > findBestMapping( final NodeMapping< T > a, final NodeMapping< T > b, final NodeMapping< T > c )
	{
		double costA = a.getCost();
		double costB = b.getCost();
		double costC = c.getCost();
		if ( costA <= costB && costA <= costC )
			return a;
		if ( costB <= costC )
			return b;
		return c;
	}

	private static class EditCosts< T >
	{
		private final BiFunction< T, T, Double > costFunction;

		private final Map< Tree< T >, TreeDetails > costs;

		/**
		 * Compute the costs of deleting or inserting a tree or a forest.
		 * <br>
		 * <strong>The cost of deleting or inserting a tree is:</strong>
		 * <ul>
		 * <li>the cost of deleting or inserting the attribute of its source</li>
		 * <li>+ the cost of deleting or inserting the forest associated with that source</li>
		 * </ul>
		 * <strong>The cost of deleting or inserting a forest is:</strong>
		 * <ul>
		 * <li>the cost of deleting or inserting all trees belonging to it
		 * </ul>
		 * <strong>What is a forest?</strong>
		 * <br>
		 * "Suppose that we have a numbering for each tree.
		 * <ul>
		 * <li>Let t[i] be the i<sup>th</sup> node of tree T in the given numbering.
		 * <li>Let T[i] be the subtree rooted at t[i]
		 * <li>Let F[i] be the unordered forest obtained by deleting t[i] from T[i]."
		 * </ul>
		 * <i>Algorithmica (1996) 15:208</i>
		 *
		 * @param tree the tree or forest to compute the change costs for
		 * @param costFunction costFunction
		 */
		private EditCosts( final Tree< T > tree, final BiFunction< T, T, Double > costFunction )
		{
			this.costFunction = costFunction;
			this.costs = new HashMap<>();
			computeChangeCosts( tree );
		}

		private void computeChangeCosts( final Tree< T > tree )
		{
			double cost = 0;
			if ( !tree.isLeaf() )
			{
				for ( Tree< T > child : tree.getChildren() )
				{
					computeChangeCosts( child );
					cost += costs.get( child ).treeCost;
				}
			}
			costs.put( tree, new TreeDetails( cost + costFunction.apply( tree.getAttribute(), null ), cost ) );
		}
	}

	private static class TreeDetails
	{
		private final double treeCost;

		private final double forestCost;

		private TreeDetails( final double treeCost, final double forestCost )
		{
			this.treeCost = treeCost;
			this.forestCost = forestCost;
		}
	}

}
