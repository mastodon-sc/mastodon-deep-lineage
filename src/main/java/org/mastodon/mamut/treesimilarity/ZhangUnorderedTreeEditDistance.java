package org.mastodon.mamut.treesimilarity;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.treesimilarity.tree.Tree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;
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
import java.util.function.Function;

/**
 * Implementation of "A Constrained Edit Distance Between Unordered Labeled Trees", Kaizhong Zhang, Algorithmica (1996) 15:205-222<p>
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
 *      / \  ->   / \
 *     TB TC     TB TC
 *
 *
 * 2a: Remove subtree (opposite of 2b)
 *
 *       A         A
 *      / \   ->   |
 *     TB TC       TB
 *
 * 2b: Add new subtree (opposite of 2a)
 *
 *       A          A
 *       |    ->   / \
 *       TB       TB TC
 *
 *
 * 3a: Remove subtree but keep one child (opposite of 3b)
 *
 *       A          A
 *      / \   ->   / \
 *     B  TC      TD TC
 *    / \
 *   TD TE        (remove B and TE, keep TD)
 *
 * 3b: Convert existing subtree into child of a newly inserted subtree (opposite of 3a)
 *       A             A
 *      / \    ->     / \
 *     TB TC         D  TC
 *                  / \
 *                 TB TE       (insert D and TE, keep TB)
 *
 *
 * 4a: Remove subtree (and siblings) but keep all children (opposite of 4b)
 *       A               A
 *      / \             / \
 *     B  TC   ->      TD TE
 *    / \
 *   TD TE            (Subtree B and it's sibling TC are removed, but the children
 *                     of B namely TD and TE are kept)
 *
 * 4b: Convert existing subtrees into children of a newly inserted subtree (opposite of 4a)
 *       A               A
 *      / \             / \
 *     TB TC   ->      D  TE
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
	 * Calculate the Zhang edit distance between two labeled unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction mandatory cost function.
	 *
	 * @return The Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double distance( @Nullable final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final BiFunction< T, T, Double > costFunction )
	{
		if ( costFunction == null )
			throw new IllegalArgumentException( "The cost function is expected to be non-null, but it is null." );
		if ( tree1 == null && tree2 == null )
			throw new IllegalArgumentException( "Both trees are null. This is not allowed." );

		// trivial cases
		if ( tree1 == null )
			return distanceTreeToNull( tree2, costFunction );
		else if ( tree2 == null )
			return distanceTreeToNull( tree1, costFunction );

		ZhangUnorderedTreeEditDistance< T > zhang = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction );
		return zhang.compute( tree1, tree2 );
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

	public static < T > Map< Tree< T >, Tree< T > > nodeMapping( Tree< T > tree1, Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		if ( tree1 == null || tree2 == null )
			return Collections.emptyMap();

		NodeMapping< T > matching = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction ).treeMapping( tree1, tree2 );
		return matching.asMap();
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
	 * Returns the optimal mapping with respect to zhang edit distance between two tree1 and tree2.
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
		NodeMapping< T > attributeMapping = NodeMapping.singleton( attributeDistances.get( Pair.of( tree1, tree2 ) ), tree1, tree2 );
		if ( tree1.isLeaf() && tree2.isLeaf() )
			return attributeMapping;

		// NB: the order of the following three lines is important, changing the order will result in a wrong distance.
		NodeMapping< T > insertOperationCosts = insertOperationMapping( tree1, tree2 );
		NodeMapping< T > deleteOperationCosts = deleteOperationMapping( tree1, tree2 );
		NodeMapping< T > changeCosts = NodeMapping.compose( attributeMapping, forestMapping( tree1, tree2 ) );
		return findBestMapping( insertOperationCosts, deleteOperationCosts, changeCosts );
	}

	/**
	 * Calculate the zhang edit distance between two sub-forests.
	 * <strong>What is a forest?</strong>
	 * <p>
	 * "Suppose that we have a numbering for each tree.
	 * <ul>
	 * <li>Let t[i] be the i<sup>th</sup> node of tree T in the given numbering.</li>
	 * <li>Let T[i] be the subtree rooted at t[i]</li>
	 * <li>Let F[i] be the unordered forest obtained by deleting t[i] from T[i]."</li>
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
			return NodeMapping.empty( insertCosts.get( forest2 ).forestCost );

		if ( forest2IsLeaf )
			return NodeMapping.empty( deleteCosts.get( forest1 ).forestCost );

		NodeMapping< T > forestInsertCosts = forestInsertMapping( forest1, forest2 );
		NodeMapping< T > forestDeleteCosts = forestDeleteMapping( forest1, forest2 );
		NodeMapping< T > changeCosts = minCostMaxFlow( forest1, forest2 );
		return findBestMapping( forestInsertCosts, forestDeleteCosts, changeCosts );
	}

	/**
	 * Computes the cost for edit operation (3b). see {@link ZhangUnorderedTreeEditDistance}
	 * <p>
	 * Costs for inserting tree2 with all but one child-tree, and changing tree1 to replace that child-tree.
	 */
	private NodeMapping< T > insertOperationMapping( Tree< T > tree1, Tree< T > tree2 )
	{
		double insertCostTree2 = insertCosts.get( tree2 ).treeCost;
		return findBestMapping( tree2.getChildren(), child ->
		{
			NodeMapping< T > insertCosts = NodeMapping.empty( insertCostTree2 - this.insertCosts.get( child ).treeCost );
			NodeMapping< T > childMapping = treeMapping( tree1, child );
			return NodeMapping.compose( insertCosts, childMapping );
		} );
	}

	/**
	 * Computes the cost for edit operation (3a) see {@link ZhangUnorderedTreeEditDistance}
	 * <p>
	 * Costs for deleting tree1 but keeping a child-tree of tree1, and changing that child-tree to tree2.
	 */
	private NodeMapping< T > deleteOperationMapping( Tree< T > tree1, Tree< T > tree2 )
	{
		double deleteCostTree1 = deleteCosts.get( tree1 ).treeCost;
		return findBestMapping( tree1.getChildren(), child ->
		{
			NodeMapping< T > deleteCosts = NodeMapping.empty( deleteCostTree1 - this.deleteCosts.get( child ).treeCost );
			NodeMapping< T > childMapping = treeMapping( child, tree2 );
			return NodeMapping.compose( deleteCosts, childMapping );
		} );
	}

	/**
	 * Computes the cost for edit operation (4b). see {@link ZhangUnorderedTreeEditDistance}
	 */
	private NodeMapping< T > forestInsertMapping( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		double insertCostForest2 = insertCosts.get( forest2 ).forestCost;
		return findBestMapping( forest2.getChildren(), child ->
		{
			NodeMapping< T > insertCosts = NodeMapping.empty( insertCostForest2 - this.insertCosts.get( child ).forestCost );
			NodeMapping< T > childMapping = forestMapping( forest1, child );
			return NodeMapping.compose( insertCosts, childMapping );
		} );
	}

	/**
	 * Computes the cost for edit operation (4a). see {@link ZhangUnorderedTreeEditDistance}
	 */
	private NodeMapping< T > forestDeleteMapping( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		double deleteCostForest1 = deleteCosts.get( forest1 ).forestCost;
		return findBestMapping( forest1.getChildren(), child ->
		{
			NodeMapping< T > deleteCosts = NodeMapping.empty( deleteCostForest1 - this.deleteCosts.get( child ).forestCost );
			NodeMapping< T > childMapping = forestMapping( child, forest2 );
			return NodeMapping.compose( deleteCosts, childMapping );
		} );
	}

	private NodeMapping< T > findBestMapping( Collection< Tree< T > > children, Function< Tree< T >, NodeMapping< T > > f )
	{
		NodeMapping< T > best = NodeMapping.empty( Double.POSITIVE_INFINITY );
		for ( Tree< T > child : children )
		{
			NodeMapping< T > cost = f.apply( child );
			if ( cost.getCost() < best.getCost() )
				best = cost;
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

		FlowNetwork network = new FlowNetwork();
		network.addVertices( Arrays.asList( source, sink, emptyTree1, emptyTree2 ) );
		network.addVertices( childrenForest1 );
		network.addVertices( childrenForest2 );

		int n1 = childrenForest1.size();
		int n2 = childrenForest2.size();
		network.addEdge( source, emptyTree1, n2 - Math.min( n1, n2 ), 0 );
		network.addEdge( emptyTree1, emptyTree2, Math.max( n1, n2 ) - Math.min( n1, n2 ), 0 ); // this edge is not needed
		network.addEdge( emptyTree2, sink, n1 - Math.min( n1, n2 ), 0 );

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

		network.solveMaxFlowMinCost( source, sink );

		ArrayList< NodeMapping< T > > childMappings = new ArrayList<>();

		for ( Tree< T > child1 : childrenForest1 )
			if ( isFlowEqualToOne( network.getFlow( child1, emptyTree2 ) ) )
				childMappings.add( NodeMapping.empty( deleteCosts.get( child1 ).treeCost ) );

		for ( Tree< T > child2 : childrenForest2 )
			if ( isFlowEqualToOne( network.getFlow( emptyTree1, child2 ) ) )
				childMappings.add( NodeMapping.empty( insertCosts.get( child2 ).treeCost ) );

		for ( Tree< T > child1 : childrenForest1 )
			for ( Tree< T > child2 : childrenForest2 )
				if ( isFlowEqualToOne( network.getFlow( child1, child2 ) ) )
					childMappings.add( treeMapping( child1, child2 ) );

		return NodeMapping.compose( childMappings );
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
		 * <p>
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
		 * <p>
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
