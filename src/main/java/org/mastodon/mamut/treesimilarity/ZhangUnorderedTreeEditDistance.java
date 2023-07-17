package org.mastodon.mamut.treesimilarity;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.mastodon.mamut.treesimilarity.tree.Tree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiFunction;

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

	private final Map< Pair< Tree< T >, Tree< T > >, Double > treeDistances;

	private final Map< Pair< Tree< T >, Tree< T > >, Double > forestDistances;

	private final List< Tree< T > > subtrees1;

	private final List< Tree< T > > subtrees2;

	private final Map< Pair< Tree< T >, Tree< T > >, Double > attributeDistances;

	private final BiFunction< T, T, Double > costFunction;

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
		this.costFunction = costFunction;

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

		Map< Tree< T >, Double > costTreeToNone = new HashMap<>();
		subtrees1.forEach( tree -> costTreeToNone.put( tree, costFunction.apply( tree.getAttribute(), null ) ) );
		subtrees2.forEach( tree -> costTreeToNone.put( tree, costFunction.apply( tree.getAttribute(), null ) ) );

		insertCosts = new EditCosts( tree2, costTreeToNone ).costs;
		deleteCosts = new EditCosts( tree1, costTreeToNone ).costs;

		treeDistances = new HashMap<>();
		forestDistances = new HashMap<>();
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
		double distance = distanceTree( tree1, tree2, costFunction );

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

	private void logDistances( String prefix, Map< Pair< Tree< T >, Tree< T > >, Double > distances )
	{
		logger.trace( "matrix of {} distances:", prefix );
		for ( Tree< T > t1 : subtrees1 )
		{
			List< Double > row = new ArrayList<>();
			for ( Tree< T > t2 : subtrees2 )
				row.add( distances.get( Pair.of( t1, t2 ) ) );
			StringJoiner stringJoiner = new StringJoiner( ", ", "[", "]" );
			row.forEach( entry -> stringJoiner.add( entry == null ? "-" : entry.toString() ) );
			logger.trace( "{} distance[{}] = {}", prefix, t1, stringJoiner );
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-trees
	 */
	private double distanceTree( final Tree< T > tree1, final Tree< T > tree2, final BiFunction< T, T, Double > costFunction )
	{
		Double distance = treeDistances.get( Pair.of( tree1, tree2 ) );
		if ( distance != null )
			return distance;

		if ( tree1.isLeaf() && tree2.isLeaf() )
			distance = attributeDistances.get( Pair.of( tree1, tree2 ) );
		else
			distance = getMinTreeChangeCosts( tree1, tree2, costFunction );

		treeDistances.put( Pair.of( tree1, tree2 ), distance );
		return distance;
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
	private double distanceForest( final Tree< T > forest1, final Tree< T > forest2 )
	{
		Double distance = forestDistances.get( Pair.of( forest1, forest2 ) );
		if ( distance != null )
			return distance;

		if ( !forest1.isLeaf() && forest2.isLeaf() )
			distance = deleteCosts.get( forest1 ).forestCost;
		else if ( forest1.isLeaf() && !forest2.isLeaf() )
			distance = insertCosts.get( forest2 ).forestCost;
		else if ( !forest2.isLeaf() && !forest1.isLeaf() )
			distance = getMinForestChangeCosts( forest1, forest2 );
		else
			throw new IllegalArgumentException( "The given trees are both leaves and thus they are both not forests." );

		forestDistances.put( Pair.of( forest1, forest2 ), distance );
		return distance;
	}

	private double getMinTreeChangeCosts( Tree< T > tree1, Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		// NB: the order of the following three lines is important, changing the order will result in a wrong distance.
		double insertOperationCosts = insertOperationCosts( tree1, tree2, costFunction );
		double deleteOperationCosts = deleteOperationCosts( tree1, tree2, costFunction );
		double changeCosts = distanceForest( tree1, tree2 ) + attributeDistances.get( Pair.of( tree1, tree2 ) );
		return min( insertOperationCosts, deleteOperationCosts, changeCosts );
	}

	/**
	 * Costs for inserting tree2 with all but one child-tree, and changing tree1 to replace that child-tree.
	 * (These are the costs for matching tree1 to a child-tree of tree2.)
	 */
	private double insertOperationCosts( Tree< T > tree1, Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		if ( tree2.isLeaf() )
			return Double.POSITIVE_INFINITY;
		List< Double > distances = new ArrayList<>();
		tree2.getChildren()
				.forEach( child -> distances.add( distanceTree( tree1, child, costFunction ) - insertCosts.get( child ).treeCost ) );
		return insertCosts.get( tree2 ).treeCost + Collections.min( distances );
	}

	/**
	 * Costs for deleting tree1 but keeping a child-tree of tree1, and changing that child-tree to tree2.
	 * (These are the costs for matching a child-tree of tree1 to tree2.)
	 */
	private double deleteOperationCosts( Tree< T > tree1, Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		if ( tree1.isLeaf() )
			return Double.POSITIVE_INFINITY;
		List< Double > distances = new ArrayList<>();
		tree1.getChildren()
				.forEach( child -> distances.add( distanceTree( child, tree2, costFunction ) - deleteCosts.get( child ).treeCost ) );
		return deleteCosts.get( tree1 ).treeCost + Collections.min( distances );
	}

	private double getMinForestChangeCosts( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		if ( forest1.isLeaf() || forest2.isLeaf() )
			throw new AssertionError();

		double forestInsertCosts = getForestInsertCosts( forest1, forest2 );
		double forestDeleteCosts = getForestDeleteCosts( forest1, forest2 );
		double changeCosts = minCostMaxFlow( forest1, forest2 );
		return min( forestInsertCosts, forestDeleteCosts, changeCosts );
	}

	private double getForestInsertCosts( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		if ( forest2.isLeaf() )
			throw new AssertionError();
		List< Double > distances = new ArrayList<>();
		forest2.getChildren().forEach( child -> distances.add( distanceForest( forest1, child ) - insertCosts.get( child ).forestCost ) );
		return insertCosts.get( forest2 ).forestCost + Collections.min( distances );
	}

	/**
	 * This effectively calculates the costs for the following edit operation that
	 * converts forest1 to forest2:
	 * Where the children "X1" and "Y1" of "A" are mapped to the children "X2" and "Y2" of "T2"
	 * "A" and "B" are deleted. (The children of "B" are also removed)
	 * <pre>
	 *      T1             T2
	 *     /  \           / \
	 *    A    B   ->   X2  Y2
	 *   / \
	 *  X1 Y1
	 * </pre>
	 */
	private double getForestDeleteCosts( Tree< T > forest1, Tree< T > forest2 )
	{
		// NB: this method should not be called on leaves.
		if ( forest1.isLeaf() )
			throw new AssertionError();
		List< Double > distances = new ArrayList<>();
		forest1.getChildren().forEach( child -> distances.add( distanceForest( child, forest2 ) - deleteCosts.get( child ).forestCost ) );
		return deleteCosts.get( forest1 ).forestCost + Collections.min( distances );
	}

	private double minCostMaxFlow( final Tree< T > forest1, final Tree< T > forest2 )
	{
		// Construction of graph for max flow min cost algorithm
		SimpleDirectedWeightedGraph< Integer, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );

		List< Tree< T > > childrenForest1 = new ArrayList<>( forest1.getChildren() );
		List< Tree< T > > childrenForest2 = new ArrayList<>( forest2.getChildren() );

		int forest1NumberOfChildren = childrenForest1.size();
		int forest2NumberOfChildren = childrenForest2.size();

		Integer source = 0;
		Integer sink = forest1NumberOfChildren + forest2NumberOfChildren + 1;
		Integer emptyTree1 = forest1NumberOfChildren + forest2NumberOfChildren + 2;
		Integer emptyTree2 = forest1NumberOfChildren + forest2NumberOfChildren + 3;

		graph.addVertex( source );
		graph.addVertex( sink );
		graph.addVertex( emptyTree1 );
		graph.addVertex( emptyTree2 );

		DefaultWeightedEdge e1 = graph.addEdge( source, emptyTree1 );
		DefaultWeightedEdge e2 = graph.addEdge( emptyTree1, emptyTree2 );
		DefaultWeightedEdge e3 = graph.addEdge( emptyTree2, sink );

		graph.setEdgeWeight( e1, 0 );
		graph.setEdgeWeight( e2, 0 );
		graph.setEdgeWeight( e3, 0 );

		Map< DefaultWeightedEdge, Integer > capacities = new HashMap<>();
		capacities.put( e1, forest2NumberOfChildren - Math.min( forest1NumberOfChildren, forest2NumberOfChildren ) );
		capacities.put( e2, Math.max( forest1NumberOfChildren, forest2NumberOfChildren )
				- Math.min( forest1NumberOfChildren, forest2NumberOfChildren ) );
		capacities.put( e3, forest1NumberOfChildren - Math.min( forest1NumberOfChildren, forest2NumberOfChildren ) );

		for ( int i = 0; i < forest1NumberOfChildren; i++ )
		{
			if ( !graph.containsVertex( i + 1 ) )
				graph.addVertex( i + 1 );
			DefaultWeightedEdge edge = graph.addEdge( 0, i + 1 );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, 1 );
			for ( int j = 0; j < childrenForest2.size(); j++ )
			{
				double edgeWeight = treeDistances.get( Pair.of( childrenForest1.get( i ), childrenForest2.get( j ) ) );
				Integer start = i + 1;
				Integer target = forest1NumberOfChildren + j + 1;
				if ( !graph.containsVertex( start ) )
					graph.addVertex( start );
				if ( !graph.containsVertex( target ) )
					graph.addVertex( target );
				edge = graph.addEdge( ( i + 1 ), ( forest1NumberOfChildren + j + 1 ) );
				graph.setEdgeWeight( edge, edgeWeight );
				capacities.put( edge, 1 );
			}
			edge = graph.addEdge( i + 1, emptyTree2 );
			graph.setEdgeWeight( edge, deleteCosts.get( childrenForest1.get( i ) ).treeCost );
			capacities.put( edge, 1 );
		}
		for ( int j = 0; j < childrenForest2.size(); j++ )
		{
			DefaultWeightedEdge edge = graph.addEdge( emptyTree1, forest1NumberOfChildren + j + 1 );
			double weight = insertCosts.get( childrenForest2.get( j ) ).treeCost;
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, forest2NumberOfChildren - Math.min( forest1NumberOfChildren, forest2NumberOfChildren ) );

			edge = graph.addEdge( forest1NumberOfChildren + j + 1, sink );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, 1 );
		}
		return JGraphtTools.maxFlowMinCost( graph, capacities, source, sink );
	}

	private static double min( final double a, final double b, final double c )
	{
		return Math.min( Math.min( a, b ), c );
	}

	private class EditCosts
	{
		private final Map< Tree< T >, Double > costTreeToNone;

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
		 * @param costTreeToNone a mapping from tree to the cost of deleting or inserting the attribute of its source
		 */
		private EditCosts( final Tree< T > tree, final Map< Tree< T >, Double > costTreeToNone )
		{
			this.costTreeToNone = costTreeToNone;
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
			costs.put( tree, new TreeDetails( cost + costTreeToNone.get( tree ), cost ) );
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
