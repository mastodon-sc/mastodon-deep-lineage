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
package org.mastodon.mamut.classification.treesimilarity;

import org.mastodon.mamut.classification.treesimilarity.tree.Node;
import org.mastodon.mamut.classification.treesimilarity.tree.Tree;
import org.mastodon.mamut.classification.treesimilarity.tree.TreeUtils;
import org.mastodon.mamut.classification.treesimilarity.util.NodeMapping;
import org.mastodon.mamut.classification.treesimilarity.util.FlowNetwork;
import org.mastodon.mamut.classification.treesimilarity.util.NodeMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

/**
 * Implementation of "A Constrained Edit Distance Between Unordered Labeled Trees", Kaizhong Zhang, Algorithmica (1996) 15:205-222<br>
 *
 * The Zhang unordered edit distance allows the following edit operations. The edit operations are defined in a way
 * that they satisfy the constraints elaborated in section 3.1 ("Constrained Edit Distance Mappings") of the paper:
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
 * 2a: Delete subtree (opposite of 2b)
 *
 *       A         A
 *      / \   -&gt;   |
 *     TB TC       TB
 *
 * 2b: Insert subtree (opposite of 2a)
 *
 *       A          A
 *       |    -&gt;   / \
 *       TB       TB TC
 *
 *
 * 3a: Delete one child of a node and delete the node itself (opposite of 3b)
 *
 *       A          A
 *      / \   -&gt;   / \
 *     B  TC      TD TC
 *    / \
 *   TD TE        (delete TE and B, TD becomes child of A)
 *
 * 3b: Insert a node and insert one child at that node (opposite of 3a)
 *       A             A
 *      / \    -&gt;     / \
 *     TB TC         D  TC
 *                  / \
 *                 TB TE       (insert D and TE, TB becomes child of D)
 *
 *
 * 4a: Delete node and delete its sibling subtree (opposite of 4b)
 *       A               A
 *      / \             / \
 *     B  TC   -&gt;      TD TE
 *    / \
 *   TD TE            (Node B and its sibling subtree TC are deleted and the children
 *                     of B, namely TD and TE, become the children of A)
 *
 * 4b: Insert node and insert a sibling subtree (opposite of 4a)
 *       A               A
 *      / \             / \
 *     TB TC   -&gt;      D  TE
 *                    / \
 *                   TB TC       (Node D and its sibling TE are inserted,
 *                                TB and TC become the children of D)
 * </pre>
 *
 * As an example, the following case explicitly does not fulfill the constraints mentioned in the paper:
 * <pre>
 * Delete a node without deleting one of its children
 *         A           A
 *        / \   -&gt;   / | \
 *       B  TC      TD TE TC
 *      / \
 *     TD TE        (delete B, TD and TE become children of A and TC remains)
 * </pre>
 * @param <T> Attribute type of the tree nodes.
 *
 * @author Stefan Hahmann
 * @author Matthias Arzt
 */
public class ZhangUnorderedTreeEditDistance< T >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final CachedTree< T > root1;

	private final CachedTree< T > root2;

	private final List< CachedTree< T > > subtrees1;

	private final List< CachedTree< T > > subtrees2;

	private final double[][] costMatrix;

	private final NodeMapping< T >[][] treeMappings;

	private final NodeMapping< T >[][] forestMappings;

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
			final ToDoubleBiFunction< T, T > costFunction )
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
		return zhang.compute();
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
	public static < T > Map< Tree< T >, Tree< T > > nodeMapping( Tree< T > tree1, Tree< T > tree2, ToDoubleBiFunction< T, T > costFunction )
	{
		if ( tree1 == null || tree2 == null )
			return Collections.emptyMap();

		ZhangUnorderedTreeEditDistance< T > zhang = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction );
		NodeMapping< T > mapping = zhang.treeMapping();
		return mapping.asMap();
	}

	private NodeMapping< T > treeMapping()
	{
		return treeMapping( root1, root2 );
	}

	private static < T > double distanceTreeToNull( Tree< T > tree2, ToDoubleBiFunction< T, T > costFunction )
	{
		double distance = 0;
		for ( Tree< T > subtree : TreeUtils.getAllChildren( tree2 ) )
			distance += costFunction.applyAsDouble( null, subtree.getAttribute() );
		return distance;
	}

	@SuppressWarnings( "unchecked" )
	private ZhangUnorderedTreeEditDistance( final Tree< T > tree1, final Tree< T > tree2,
			final ToDoubleBiFunction< T, T > costFunction )
	{

		root1 = new CachedTree<>( tree1 );
		root2 = new CachedTree<>( tree2 );
		subtrees1 = TreeUtils.getAllChildren( root1 );
		subtrees2 = TreeUtils.getAllChildren( root2 );
		subtrees1.forEach( cachedTree -> cachedTree.index = subtrees1.indexOf( cachedTree ) );
		subtrees2.forEach( cachedTree -> cachedTree.index = subtrees2.indexOf( cachedTree ) );

		costMatrix = new double[ subtrees1.size() ][ subtrees2.size() ];
		for ( CachedTree< T > subtree1 : subtrees1 )
		{
			for ( CachedTree< T > subtree2 : subtrees2 )
			{
				double distance = costFunction.applyAsDouble( subtree1.attribute, subtree2.attribute );
				costMatrix[ subtree1.index ][ subtree2.index ] = distance;
			}
		}

		computeChangeCosts( root1, costFunction );
		computeChangeCosts( root2, costFunction );

		treeMappings = new NodeMapping[ subtrees1.size() ][ subtrees2.size() ];
		forestMappings = new NodeMapping[ subtrees1.size() ][ subtrees2.size() ];
	}

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
	 * @param costFunction the local cost function
	 */
	private void computeChangeCosts( CachedTree< T > tree, ToDoubleBiFunction< T, T > costFunction )
	{
		double forestCosts = 0;
		for ( CachedTree< T > child : tree.children )
		{
			computeChangeCosts( child, costFunction );
			forestCosts += child.treeCost;
		}
		tree.treeCost = forestCosts + costFunction.applyAsDouble( tree.attribute, null );
		tree.forestCost = forestCosts;
	}

	/**
	 * Calculate the Zhang edit distance between two (labeled) unordered trees.
	 *
	 * @return The Zhang edit distance between tree1 and tree2 as an integer.
	 */
	private double compute()
	{
		double distance = treeMapping( root1, root2 ).getCost();

		log();

		return distance;
	}

	private void log()
	{
		if ( !logger.isTraceEnabled() )
			return;
		logDistances( "tree", treeMappings );
		logDistances( "forest", forestMappings );

		logger.trace( "tree deletion costs (tree1):" );
		for ( CachedTree< T > subtree : subtrees1 )
			logger.trace( "tree deletion[{}] = {}", subtree.externalTree, subtree.treeCost );

		logger.trace( "forest deletion costs (tree1):" );
		for ( CachedTree< T > subtree : subtrees1 )
			logger.trace( "forest deletion[{}] = {}", subtree.externalTree, subtree.forestCost );

		logger.trace( "tree insertion costs (tree2):" );
		for ( CachedTree< T > subtree : subtrees2 )
			logger.trace( "tree insertion[{}] = {}", subtree.externalTree, subtree.treeCost );

		logger.trace( "forest insertion costs (tree2):" );
		for ( CachedTree< T > subtree : subtrees2 )
			logger.trace( "forest insertion[{}] = {}", subtree.externalTree, subtree.forestCost );
	}

	private void logDistances( String prefix, NodeMapping< T >[][] nodeMappings )
	{
		if ( !logger.isTraceEnabled() )
			return;
		logger.trace( "matrix of {} distances:", prefix );
		for ( CachedTree< T > t1 : subtrees1 )
		{
			StringJoiner stringJoiner = new StringJoiner( ", ", "[", "]" );
			for ( CachedTree< T > t2 : subtrees2 )
			{
				NodeMapping< T > editOperation = nodeMappings[ t1.index ][ t2.index ];
				stringJoiner.add( editOperation == null ? "-" : Double.toString( editOperation.getCost() ) );
			}
			logger.trace( "{} distance[{}] = {}", prefix, t1, stringJoiner );
		}
	}

	/**
	 * Returns the optimal node mapping with respect to zhang edit distance
	 * that maps from tree1 to tree2.
	 */
	private NodeMapping< T > treeMapping( CachedTree< T > tree1, CachedTree< T > tree2 )
	{
		NodeMapping< T > operation = treeMappings[ tree1.index ][ tree2.index ];
		if ( operation == null )
		{
			operation = computeTreeMapping( tree1, tree2 );
			treeMappings[ tree1.index ][ tree2.index ] = operation;
		}
		return operation;
	}

	private NodeMapping< T > computeTreeMapping( CachedTree< T > tree1, CachedTree< T > tree2 )
	{
		double cost = costMatrix[ tree1.index ][ tree2.index ];
		NodeMapping< T > attributeMapping = NodeMappings.singleton( cost, tree1.externalTree, tree2.externalTree );
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
	 * `"Suppose that we have a numbering for each tree.
	 * <ul>
	 *   <li>Let t[i] be the i<sup>th</sup> node of tree T in the given numbering.</li>
	 *   <li>Let T[i] be the subtree rooted at t[i]</li>
	 *   <li>Let F[i] be the unordered forest obtained by deleting t[i] from T[i]."</li>
	 * </ul>
	 * Algorithmica (1996) 15:208
	 */
	private NodeMapping< T > forestMapping( final CachedTree< T > forest1, final CachedTree< T > forest2 )
	{
		NodeMapping< T > operation = forestMappings[ forest1.index ][ forest2.index ];
		if ( operation == null )
		{
			operation = computeForestMapping( forest1, forest2 );
			forestMappings[ forest1.index ][ forest2.index ] = operation;
		}
		return operation;
	}

	private NodeMapping< T > computeForestMapping( CachedTree< T > forest1, CachedTree< T > forest2 )
	{
		boolean forest1IsLeaf = forest1.isLeaf();
		boolean forest2IsLeaf = forest2.isLeaf();

		if ( forest1IsLeaf && forest2IsLeaf )
			throw new IllegalArgumentException( "The given trees are both leaves and thus they are both not forests." );

		if ( forest1IsLeaf )
			return NodeMappings.empty( forest2.forestCost );

		if ( forest2IsLeaf )
			return NodeMappings.empty( forest1.forestCost );

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
	private NodeMapping< T > insertOperationMapping( CachedTree< T > tree1, CachedTree< T > tree2 )
	{
		double insertCostTree2 = tree2.treeCost;
		return findBestMapping( tree2.getChildren(), child ->
		{
			NodeMapping< T > insertMapping = NodeMappings.empty( insertCostTree2 - child.treeCost );
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
	private NodeMapping< T > deleteOperationMapping( CachedTree< T > tree1, CachedTree< T > tree2 )
	{
		double deleteCostTree1 = tree1.treeCost;
		return findBestMapping( tree1.getChildren(), child ->
		{
			NodeMapping< T > deleteMapping = NodeMappings.empty( deleteCostTree1 - child.treeCost );
			NodeMapping< T > childMapping = treeMapping( child, tree2 );
			return NodeMappings.compose( deleteMapping, childMapping );
		} );
	}

	/**
	 * Returns the best {@link NodeMapping} that uses edit operation (4b)
	 * to map {@code forest1} to {@code forest2}. See {@link ZhangUnorderedTreeEditDistance}.
	 */
	private NodeMapping< T > forestInsertMapping( CachedTree< T > forest1, CachedTree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		double insertCostForest2 = forest2.forestCost;
		return findBestMapping( forest2.getChildren(), child ->
		{
			NodeMapping< T > insertMapping = NodeMappings.empty( insertCostForest2 - child.forestCost );
			NodeMapping< T > childMapping = forestMapping( forest1, child );
			return NodeMappings.compose( insertMapping, childMapping );
		} );
	}

	/**
	 * Returns the best {@link NodeMapping} that uses edit operation (4a)
	 * to map {@code forest1} to {@code forest2}. See {@link ZhangUnorderedTreeEditDistance}.
	 */
	private NodeMapping< T > forestDeleteMapping( CachedTree< T > forest1, CachedTree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		double deleteCostForest1 = forest1.forestCost;
		return findBestMapping( forest1.getChildren(), child ->
		{
			NodeMapping< T > deleteMapping = NodeMappings.empty( deleteCostForest1 - child.forestCost );
			NodeMapping< T > childMapping = forestMapping( child, forest2 );
			return NodeMappings.compose( deleteMapping, childMapping );
		} );
	}

	/**
	 * Invokes the given {@code function} for each of the given {@code children}.
	 * Each function invocation must return a {@link NodeMapping}. This method returns
	 * the best {@link NodeMapping}, i.e. the one with the lowest cost.
	 */
	private NodeMapping< T > findBestMapping( Collection< CachedTree< T > > children,
			Function< CachedTree< T >, NodeMapping< T > > function )
	{
		NodeMapping< T > best = NodeMappings.empty( Double.POSITIVE_INFINITY );
		for ( CachedTree< T > child : children )
		{
			NodeMapping< T > nodeMapping = function.apply( child );
			if ( nodeMapping.getCost() < best.getCost() )
				best = nodeMapping;
		}
		return best;
	}

	private NodeMapping< T > minCostMaxFlow( final CachedTree< T > forestA, final CachedTree< T > forestB )
	{
		Collection< CachedTree< T > > childrenForestA = forestA.getChildren();
		Collection< CachedTree< T > > childrenForestB = forestB.getChildren();

		List< NodeMapping< T > > childMappings;
		boolean isBinaryTreeComparison = childrenForestA.size() == 2 && childrenForestB.size() == 2;
		if ( isBinaryTreeComparison )
			childMappings = minCostMaxFlowBinary( childrenForestA, childrenForestB );
		else
			childMappings = minCostMaxFlowNonBinary( childrenForestA, childrenForestB );

		return NodeMappings.compose( childMappings );
	}

	/**
	 * Returns the best mapping for binary trees.
	 * <br>
	 * For the case of two binary trees / forests, the flow network can be simplified by removing the "empty" nodes.
	 * <br>
	 * The remaining flow network can be visualized as follows (capacities annotated with 1):
	 * <pre>
	 *                   1
	 *                A1 --- B1
	 *               /\      /\
	 *             1/ 1\    /1 \1
	 *             /    \  /    \
	 *     source  \     \/      \    sink
	 *              \    /\      /
	 *              1\  /  \    /1
	 *                \/    \  /
	 *               A2 --- B2
	 *                   1
	 * </pre>
	 * For this simplified network, the "parallel" and "cross" costs can be calculated directly by summing the costs of the corresponding edges.
	 * <br>
	 * The best mapping is then determined by comparing the "parallel" and "cross" costs.
	 * @param childrenForestA The children of the first forest.
	 * @param childrenForestB The children of the second forest.
	 * @return The best mapping for binary trees.
	 */
	private List< NodeMapping< T > > minCostMaxFlowBinary( final Collection< CachedTree< T > > childrenForestA,
			final Collection< CachedTree< T > > childrenForestB )
	{
		final Iterator< CachedTree< T > > forestAIterator = childrenForestA.iterator();
		CachedTree< T > forestAChild1 = forestAIterator.next();
		CachedTree< T > forestAChild2 = forestAIterator.next();

		final Iterator< CachedTree< T > > forestBIterator = childrenForestB.iterator();
		CachedTree< T > forestBChild1 = forestBIterator.next();
		CachedTree< T > forestBChild2 = forestBIterator.next();

		NodeMapping< T > mappingA1B1 = treeMapping( forestAChild1, forestBChild1 );
		NodeMapping< T > mappingA2B2 = treeMapping( forestAChild2, forestBChild2 );
		NodeMapping< T > mappingA1B2 = treeMapping( forestAChild1, forestBChild2 );
		NodeMapping< T > mappingA2B1 = treeMapping( forestAChild2, forestBChild1 );

		double parallelCosts = mappingA1B1.getCost() + mappingA2B2.getCost();
		double crossCosts = mappingA1B2.getCost() + mappingA2B1.getCost();

		if ( parallelCosts <= crossCosts )
			return Arrays.asList( mappingA1B1, mappingA2B2 );
		else
			return Arrays.asList( mappingA1B2, mappingA2B1 );
	}

	private List< NodeMapping< T > > minCostMaxFlowNonBinary( final Collection< CachedTree< T > > childrenForest1,
			final Collection< CachedTree< T > > childrenForest2 )
	{
		// Construction of graph for max flow min cost algorithm
		String source = "source";
		String sink = "sink";
		String emptyTree1 = "empty1";
		String emptyTree2 = "empty2";

		FlowNetwork network = buildFlowNetwork( source, sink, emptyTree1, emptyTree2, childrenForest1, childrenForest2 );

		network.solveMaxFlowMinCost( source, sink );

		List< NodeMapping< T > > childMappings = new ArrayList<>();

		for ( CachedTree< T > child1 : childrenForest1 )
			if ( isFlowEqualToOne( network.getFlow( child1, emptyTree2 ) ) )
				childMappings.add( NodeMappings.empty( child1.treeCost ) );

		for ( CachedTree< T > child2 : childrenForest2 )
			if ( isFlowEqualToOne( network.getFlow( emptyTree1, child2 ) ) )
				childMappings.add( NodeMappings.empty( child2.treeCost ) );

		for ( CachedTree< T > child1 : childrenForest1 )
			for ( CachedTree< T > child2 : childrenForest2 )
				if ( isFlowEqualToOne( network.getFlow( child1, child2 ) ) )
					childMappings.add( treeMapping( child1, child2 ) );
		return childMappings;
	}

	private FlowNetwork buildFlowNetwork(
			String source, String sink, String emptyTree1, String emptyTree2, Collection< CachedTree< T > > childrenForest1,
			Collection< CachedTree< T > > childrenForest2
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

		for ( CachedTree< T > child1 : childrenForest1 )
		{
			network.addEdge( source, child1, 1, 0 );
			network.addEdge( child1, emptyTree2, 1, child1.treeCost );
			for ( CachedTree< T > child2 : childrenForest2 )
				network.addEdge( child1, child2, 1, treeMapping( child1, child2 ).getCost() );
		}

		for ( CachedTree< T > child2 : childrenForest2 )
		{
			network.addEdge( child2, sink, 1, 0 );
			network.addEdge( emptyTree1, child2, 1, child2.treeCost );
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

	private static class CachedTree< T > implements Node< CachedTree< T > >
	{
		private int index;

		private double treeCost;

		private double forestCost;

		private final T attribute;

		private final List< CachedTree< T > > children;

		private final boolean isLeaf;

		private final Tree< T > externalTree;

		private CachedTree( Tree< T > tree )
		{
			isLeaf = tree.isLeaf();
			externalTree = tree;
			attribute = tree.getAttribute();
			children = new ArrayList<>();
			for ( Tree< T > child : tree.getChildren() )
				children.add( new CachedTree<>( child ) );
		}

		private boolean isLeaf()
		{
			return isLeaf;
		}

		public List< CachedTree< T > > getChildren()
		{
			return children;
		}
	}

}
