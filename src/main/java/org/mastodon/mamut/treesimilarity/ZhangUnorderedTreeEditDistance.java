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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ZhangUnorderedTreeEditDistance< T >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Map< Tree< T >, Double > treeInsertCosts;

	private final Map< Tree< T >, Double > forestInsertCosts;

	private final Map< Tree< T >, Double > treeDeleteCosts;

	private final Map< Tree< T >, Double > forestDeleteCosts;

	private final Double[][] forestDistances;

	private final Double[][] treeDistances;

	private final Map< Tree< T >, Integer > equivalenceClasses;

	private final List< Tree< T > > subtrees1;

	private final List< Tree< T > > subtrees2;

	private final Map< Pair< Tree< T >, Tree< T > >, Double > attributeDistances;

	/**
	 * Calculate the Zhang edit distance between two labeled unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction Optional cost function.
	 *
	 * @return The Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double distance( final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final BiFunction< T, T, Double > costFunction )
	{
		if ( costFunction == null )
			throw new NullPointerException( "The cost function is expected to be non-null, but it is null." );

		// trivial cases
		if ( tree2 == null )
		{
			double distance = 0;
			for ( Tree< T > subtree : TreeUtils.listOfSubtrees( tree1 ) )
			{
				distance += costFunction.apply( subtree.getAttribute(), null );
			}
			return distance;
		}
		ZhangUnorderedTreeEditDistance< T > zhang = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction );
		return zhang.compute( tree1, tree2, costFunction );
	}

	private ZhangUnorderedTreeEditDistance( final Tree< T > tree1, final Tree< T > tree2,
			final BiFunction< T, T, Double > costFunction )
	{
		subtrees1 = TreeUtils.listOfSubtrees( tree1 );
		subtrees2 = TreeUtils.listOfSubtrees( tree2 );

		equivalenceClasses = getEquivalenceClasses( tree1, tree2 ); attributeDistances = new HashMap<>();
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

		ChangeCosts deleteCosts = new ChangeCosts( tree1, costTreeToNone );
		treeDeleteCosts = deleteCosts.treeCosts;
		forestDeleteCosts = deleteCosts.forestCosts;

		ChangeCosts insertCosts = new ChangeCosts( tree2, costTreeToNone );
		treeInsertCosts = insertCosts.treeCosts;
		forestInsertCosts = insertCosts.forestCosts;

		treeDistances = new Double[ subtrees1.size() ][ subtrees2.size() ];
		forestDistances = new Double[ subtrees1.size() ][ subtrees2.size() ];
	}

	/**
	 * Calculate the Zhang edit distance between two (labeled) unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction Optional cost function.
	 *
	 * @return The Zhang edit distance between tree1 and tree2 as an integer.
	 */
	private double compute( final Tree< T > tree1, final Tree< T > tree2, final BiFunction< T, T, Double > costFunction )
	{
		for ( Double[] row : forestDistances )
			Arrays.fill( row, -1d );
		for ( Double[] row : treeDistances )
			Arrays.fill( row, -1d );

		double distance = distanceTree( tree1, tree2, costFunction );

		log();

		return distance;
	}

	private void log()
	{
		logger.trace( "matrix of tree distances:" );
		for ( int i = 0; i < subtrees1.size(); i++ )
		{
			if ( logger.isTraceEnabled() )
				logger.trace( "tree distance[{}] = {}", i, Arrays.toString( treeDistances[ i ] ) );
		}
		logger.trace( "matrix of forest distances:" );
		for ( int i = 0; i < subtrees1.size(); i++ )
		{
			if ( logger.isTraceEnabled() )
				logger.trace( "forest distance[{}] = {}", i, Arrays.toString( forestDistances[ i ] ) );
		}
		logger.trace( "tree deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
		{
			logger.trace( "tree deletion[{}] = {}", subtree, treeDeleteCosts.get( subtree ) );
		}
		logger.trace( "forest deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
		{
			logger.trace( "forest deletion[{}] = {}", subtree, forestDeleteCosts.get( subtree ) );
		}
		logger.trace( "tree insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
		{
			logger.trace( "tree insertion[{}] = {}", subtree, treeInsertCosts.get( subtree ) );
		}
		logger.trace( "forest insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
		{
			logger.trace( "forest insertion[{}] = {}", subtree, forestInsertCosts.get( subtree ) );
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-trees
	 */
	private double distanceTree( final Tree< T > tree1, final Tree< T > tree2, final BiFunction< T, T, Double > costFunction )
	{
		if ( equivalenceClasses.get( tree1 ).intValue() == equivalenceClasses.get( tree2 ).intValue() )
		{
			treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0d;
			forestDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0d;
			return 0;
		}
		double distance = treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ];
		if ( distance != -1 )
			return distance;

		if ( tree1.isLeaf() && tree2.isLeaf() )
			distance = attributeDistances.get( Pair.of( tree1, tree2 ) );
		else
			distance =  getMinTreeChangeCosts( tree1, tree2, costFunction );

		treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = distance;
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
		double distance = forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ];
		if ( distance != -1 )
			return distance;

		if ( !forest1.isLeaf() && forest2.isLeaf() )
			distance = forestDeleteCosts.get( forest1 );
		else if ( forest1.isLeaf() && !forest2.isLeaf() )
			distance = forestInsertCosts.get( forest2 );
		else if ( !forest2.isLeaf() && !forest1.isLeaf() )
			distance = getMinForestChangeCosts( forest1, forest2 );
		else
			throw new IllegalArgumentException( "The given trees are both leaves and thus they are both not forests." );

		forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = distance;
		return distance;
	}

	private double getMinTreeChangeCosts( Tree< T > tree1, Tree< T > tree2, BiFunction< T, T, Double > costFunction )
	{
		// NB: the order of the following three lines is important, changing the order will result in a wrong distance.
		double insertCosts = insertOperationCosts( tree1, tree2, costFunction );
		double deleteCosts = deleteOperationCosts( tree1, tree2, costFunction );
		double changeCosts = distanceForest( tree1, tree2 ) + attributeDistances.get( Pair.of( tree1, tree2 ) );
		return min( insertCosts, deleteCosts, changeCosts );
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
		tree2.getChildren().forEach( child -> distances.add( distanceTree( tree1, child, costFunction ) - treeInsertCosts.get( child ) ) );
		return treeInsertCosts.get( tree2 ) + Collections.min( distances );
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
		tree1.getChildren().forEach( child -> distances.add( distanceTree( child, tree2, costFunction ) - treeDeleteCosts.get( child ) ) );
		return treeDeleteCosts.get( tree1 ) + Collections.min( distances );
	}

	double getMinForestChangeCosts( Tree< T > forest1, Tree< T > forest2 )
	{
		double insertCosts = forestInsertCosts.get( forest2 );
		List< Double > distances = new ArrayList<>();
		if ( !forest2.isLeaf() )
		{
			for ( Tree< T > child : forest2.getChildren() )
				distances.add( distanceForest( forest1, child ) - forestInsertCosts.get( child ) );
			insertCosts += Collections.min( distances );
		}
		double deleteCosts = forestDeleteCosts.get( forest1 );
		distances = new ArrayList<>();
		if ( !forest1.isLeaf() )
		{
			for ( Tree< T > child : forest1.getChildren() )
				distances.add( distanceForest( child, forest2 ) - forestDeleteCosts.get( child ) );
			deleteCosts += Collections.min( distances );
		}
		double changeCosts = minCostMaxFlow( forest1, forest2 );
		return min( insertCosts, deleteCosts, changeCosts );
	}

	private double minCostMaxFlow( final Tree< T > forest1, final Tree< T > forest2 )
	{
		List< List< Tree< T > > > classifiedTreesOfForest1 = getClassifiedTrees( forest1 );
		List< List< Tree< T > > > classifiedTreesOfForest2 = getClassifiedTrees( forest2 );

		// Construction of graph for max flow min cost algorithm
		SimpleDirectedWeightedGraph< Integer, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );

		// NB: The size of the forests is the number of their nodes minus one, since the root of the forest is not counted
		int forest1Size = TreeUtils.size( forest1 ) - 1;
		int forest2Size = TreeUtils.size( forest1 ) - 1;

		int numberOfEquivalenceClasses1 = classifiedTreesOfForest1.size();
		int numberOfEquivalenceClasses2 = classifiedTreesOfForest2.size();

		Integer source = 0;
		Integer sink = numberOfEquivalenceClasses1 + numberOfEquivalenceClasses2 + 1;
		Integer emptyTree1 = numberOfEquivalenceClasses1 + numberOfEquivalenceClasses2 + 2;
		Integer emptyTree2 = numberOfEquivalenceClasses1 + numberOfEquivalenceClasses2 + 3;

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
		capacities.put( e1, forest2Size - Math.min( forest1Size, forest2Size ) );
		capacities.put( e2, Math.max( forest1Size, forest2Size ) - Math.min( forest1Size, forest2Size ) );
		capacities.put( e3, forest1Size - Math.min( forest1Size, forest2Size ) );

		for ( int i = 0; i < classifiedTreesOfForest1.size(); i++ )
		{
			List< Tree< T > > subtreesWithSameEquivalenceClassForest1 = classifiedTreesOfForest1.get( i );
			if ( !graph.containsVertex( i + 1 ) )
				graph.addVertex( i + 1 );
			DefaultWeightedEdge edge = graph.addEdge( 0, i + 1 );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, subtreesWithSameEquivalenceClassForest1.size() );
			Tree< T > firstTree1OfForest1 = subtreesWithSameEquivalenceClassForest1.get( 0 );
			for ( int j = 0; j < classifiedTreesOfForest2.size(); j++ )
			{
				List< Tree< T > > subtreesWithSameEquivalenceClassForest2 = classifiedTreesOfForest2.get( j );
				Tree< T > firstTreeOfForest2 = subtreesWithSameEquivalenceClassForest2.get( 0 );
				double edgeWeight = treeDistances[ subtrees1.indexOf( firstTree1OfForest1 ) ][ subtrees2.indexOf( firstTreeOfForest2 ) ];
				Integer start = i + 1;
				Integer target = numberOfEquivalenceClasses1 + j + 1;
				if ( !graph.containsVertex( start ) )
					graph.addVertex( start );
				if ( !graph.containsVertex( target ) )
					graph.addVertex( target );
				edge = graph.addEdge( ( i + 1 ), ( numberOfEquivalenceClasses1 + j + 1 ) );
				graph.setEdgeWeight( edge, edgeWeight );
				capacities.put( edge, subtreesWithSameEquivalenceClassForest1.size() );
			}
			edge = graph.addEdge( i + 1, emptyTree2 );
			graph.setEdgeWeight( edge, treeDeleteCosts.get( firstTree1OfForest1 ) );
			capacities.put( edge, subtreesWithSameEquivalenceClassForest1.size() );
		}
		for ( int j = 0; j < classifiedTreesOfForest2.size(); j++ )
		{
			List< Tree< T > > subtreesWithSameEquivalenceClassForest2 = classifiedTreesOfForest2.get( j );
			Tree< T > tree2 = subtreesWithSameEquivalenceClassForest2.get( 0 );

			DefaultWeightedEdge edge = graph.addEdge( emptyTree1, numberOfEquivalenceClasses1 + j + 1 );
			Double weight = treeInsertCosts.get( tree2 );
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, forest2Size - Math.min( forest1Size, forest2Size ) );

			edge = graph.addEdge( numberOfEquivalenceClasses1 + j + 1, sink );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, subtreesWithSameEquivalenceClassForest2.size() );
		}
		return JGraphtTools.maxFlowMinCost( graph, capacities, source, sink );
	}

	/**
	 * Returns a list of lists of trees, where each list contains trees with the same equivalence class.
	 * @param tree the forest
	 * @return a list of lists of trees, where each list contains trees with the same equivalence class
	 */
	private List< List< Tree< T > > > getClassifiedTrees( Tree< T > tree )
	{
		// NB: a LinkedHashMap is used to preserve the order of the keys
		Map< Integer, List< Tree< T > > > equivalenceClassesTree1 = new LinkedHashMap<>();
		for ( Tree< T > tree1 : tree.getChildren() )
		{
			int equivalenceClass = equivalenceClasses.get( tree1 );
			if ( equivalenceClassesTree1.containsKey( equivalenceClass ) )
			{
				List< Tree< T > > trees = equivalenceClassesTree1.get( equivalenceClass );
				trees.add( tree1 );
			}
			else
			{
				List< Tree< T > > list = new ArrayList<>();
				list.add( tree1 );
				equivalenceClassesTree1.put( equivalenceClass, list );
			}
		}
		return new ArrayList<>( equivalenceClassesTree1.values() );
	}

	private static double min( final double a, final double b, final double c )
	{
		return Math.min( Math.min( a, b ), c );
	}

	private static < T > int classifyTrees( final Tree< T > tree, final Map< Integer, Map< T, List< Tree< T > > > > classifiedTrees )
	{
		int depth = 0;
		List< Integer > depths = new ArrayList<>();
		for ( Tree< T > child : tree.getChildren() )
		{
			int d = classifyTrees( child, classifiedTrees );
			depths.add( d );
			depth = Collections.max( depths );
		}

		Map< T, List< Tree< T > > > attributeToTrees = classifiedTrees.computeIfAbsent( depth, ignore -> new HashMap<>() );
		T attribute = tree.getAttribute();
		List< Tree< T > > treesWithSameAttribute = attributeToTrees.computeIfAbsent( attribute, ignore -> new ArrayList<>() );
		treesWithSameAttribute.add( tree );
		return depth + 1;
	}

	private static < T > Map< Tree< T >, Integer > getEquivalenceClasses( final Tree< T > tree1, final Tree< T > tree2 )
	{
		Map< Tree< T >, Integer > equivalenceClasses = new HashMap<>();
		Map< Integer, Map< T, List< Tree< T > > > > classifiedTrees = new LinkedHashMap<>();

		classifyTrees( tree1, classifiedTrees );
		classifyTrees( tree2, classifiedTrees );

		boolean subtreeClassNumberIncremented = false;

		int classNumber = 0;
		for ( Map.Entry< Integer, Map< T, List< Tree< T > > > > graphDepth : classifiedTrees.entrySet() )
		{
			for ( Map.Entry< T, List< Tree< T > > > treesWithSameAttributeAtGraphDepth : graphDepth.getValue().entrySet() )
			{
				List< Tree< T > > treesWithSameAttribute = treesWithSameAttributeAtGraphDepth.getValue();
				for ( Tree< T > tree : treesWithSameAttribute )
				{
					equivalenceClasses.put( tree, classNumber );
					// NB: ensure that the two subtrees of the given tree have different class numbers, if the attribute is used
					if ( ( tree.equals( tree1 ) || tree.equals( tree2 ) ) && !subtreeClassNumberIncremented )
					{
						classNumber++;
						subtreeClassNumberIncremented = true;
					}
				}
				classNumber++;
			}
		}
		return equivalenceClasses;
	}

	private class ChangeCosts
	{
		private final Map< Tree< T >, Double > costTreeToNone;

		private final Map< Tree< T >, Double > treeCosts;

		private final Map< Tree< T >, Double > forestCosts;

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
		private ChangeCosts( final Tree< T > tree, final Map< Tree< T >, Double > costTreeToNone )
		{
			this.costTreeToNone = costTreeToNone;
			this.treeCosts = new HashMap<>();
			this.forestCosts = new HashMap<>();
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
					cost += treeCosts.get( child );
				}
			}
			forestCosts.put( tree, cost );
			treeCosts.put( tree, cost + costTreeToNone.get( tree ) );
		}
	}
}
