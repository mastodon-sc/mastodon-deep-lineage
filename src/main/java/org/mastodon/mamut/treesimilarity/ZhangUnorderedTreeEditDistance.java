package org.mastodon.mamut.treesimilarity;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
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
import java.util.Objects;
import java.util.function.BiFunction;

public class ZhangUnorderedTreeEditDistance< T >
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Map< Tree< T >, Double > treeInsertMap;

	private final Map< Tree< T >, Double > forestInsertMap;

	private final Map< Tree< T >, Double > treeDeleteMap;

	private final Map< Tree< T >, Double > forestDeleteMap;

	private final Map< Tree< T >, Integer > equivalenceClasses;

	private final List< Tree< T > > subtrees1;

	private final List< Tree< T > > subtrees2;

	private final Map< Pair< Tree< T >, Tree< T > >, Double > costTreeToTree;

	/**
	 * Calculate the Zhang edit distance between two labeled unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param costFunction Optional cost function.
	 *
	 * @return The Zhang edit distance between tree1 and tree2.
	 */
	public static < T > double distance( Tree< T > tree1, @Nullable Tree< T > tree2,
			@Nullable BiFunction< T, T, Double > costFunction )
	{
		ZhangUnorderedTreeEditDistance< T > zhang = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction );
		return zhang.compute( tree1, tree2, costFunction );
	}

	private ZhangUnorderedTreeEditDistance( Tree< T > tree1, @Nullable Tree< T > tree2,
			@Nullable BiFunction< T, T, Double > costFunction )
	{
		subtrees1 = TreeUtils.listOfSubtrees( tree1 );
		if ( tree2 == null )
			subtrees2 = Collections.emptyList();
		else
			subtrees2 = TreeUtils.listOfSubtrees( tree2 );

		if ( costFunction == null )
		{
			equivalenceClasses = getEquivalenceClasses( tree1, tree2, false );
			costTreeToTree = Collections.emptyMap();

			forestDeleteMap = getForestCosts( tree1, null );
			treeDeleteMap = getTreeCosts( tree1, null );

			forestInsertMap = getForestCosts( tree2, null );
			treeInsertMap = getTreeCosts( tree2, null );
		}
		else
		{
			equivalenceClasses = getEquivalenceClasses( tree1, tree2, true );
			costTreeToTree = new HashMap<>();
			for ( Tree< T > subtree1 : subtrees1 )
			{
				for ( Tree< T > subtree2 : subtrees2 )
				{
					costTreeToTree.put( Pair.of( subtree1, subtree2 ),
							costFunction.apply( subtree1.getAttribute(), subtree2.getAttribute() ) );
				}
			}

			Map< Tree< T >, Double > costTreeToNone = new HashMap<>();
			subtrees1.forEach( tree -> costTreeToNone.put( tree, costFunction.apply( tree.getAttribute(), null ) ) );
			subtrees2.forEach( tree -> costTreeToNone.put( tree, costFunction.apply( tree.getAttribute(), null ) ) );

			forestDeleteMap = getForestCosts( tree1, costTreeToNone );
			treeDeleteMap = getTreeCosts( tree1, costTreeToNone );

			forestInsertMap = getForestCosts( tree2, costTreeToNone );
			treeInsertMap = getTreeCosts( tree2, costTreeToNone );
		}
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
	private double compute( Tree< T > tree1, @Nullable Tree< T > tree2,
			@Nullable BiFunction< T, T, Double > costFunction )
	{
		if ( tree2 == null )
		{
			if ( costFunction == null )
				return TreeUtils.size( tree1 );
			else
			{
				double distance = 0;
				for ( Tree< T > subtree : TreeUtils.listOfSubtrees( tree1 ) )
				{
					distance += costFunction.apply( subtree.getAttribute(), null );
				}
				return distance;
			}
		}

		double[][] forestDistances = new double[ subtrees1.size() ][ subtrees2.size() ];
		double[][] treeDistances = new double[ subtrees1.size() ][ subtrees2.size() ];

		for ( double[] row : forestDistances )
			Arrays.fill( row, -1 );
		for ( double[] row : treeDistances )
			Arrays.fill( row, -1 );

		double distance = distanceTree( tree1, tree2, costFunction, treeDistances, forestDistances );

		log( forestDistances, treeDistances );

		return distance;
	}

	private void log( double[][] forestDistances, double[][] treeDistances )
	{
		logger.info( "matrix of tree distances:" );
		for ( int i = 0; i < subtrees1.size(); i++ )
		{
			if ( logger.isInfoEnabled() )
				logger.info( "tree distance[{}] = {}", i, Arrays.toString( treeDistances[ i ] ) );
		}
		logger.info( "matrix of forest distances:" );
		for ( int i = 0; i < subtrees1.size(); i++ )
		{
			if ( logger.isInfoEnabled() )
				logger.info( "forest distance[{}] = {}", i, Arrays.toString( forestDistances[ i ] ) );
		}
		logger.info( "tree deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
		{
			logger.info( "tree deletion[{}] = {}", subtree, treeDeleteMap.get( subtree ) );
		}
		logger.info( "forest deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
		{
			logger.info( "forest deletion[{}] = {}", subtree, forestDeleteMap.get( subtree ) );
		}
		logger.info( "tree insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
		{
			logger.info( "tree insertion[{}] = {}", subtree, treeInsertMap.get( subtree ) );
		}
		logger.info( "forest insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
		{
			logger.info( "forest insertion[{}] = {}", subtree, forestInsertMap.get( subtree ) );
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-trees
	 */
	private double distanceTree( Tree< T > tree1, Tree< T > tree2, @Nullable BiFunction< T, T, Double > costFunction,
			double[][] treeDistances, double[][] forestDistances )
	{
		if ( equivalenceClasses != null && Objects.equals( equivalenceClasses.get( tree1 ), equivalenceClasses.get( tree2 ) ) )
		{
			treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0;
			forestDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0;
			return 0;
		}
		if ( treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] != -1 )
			return treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ];

		if ( tree1.isLeaf() && tree2.isLeaf() )
		{
			if ( costFunction == null )
			{
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0;
				forestDistances[ subtrees2.indexOf( tree2 ) ][ subtrees1.indexOf( tree1 ) ] = 0;
				return 0;
			}
			else
			{
				double value = costTreeToTree.get( Pair.of( tree1, tree2 ) );
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = value;
				return value;
			}
		}
		else
		{
			double a = 0;
			if ( treeInsertMap != null )
			{
				a = treeInsertMap.get( tree2 );
				List< Double > l = new ArrayList<>();
				if ( !tree2.isLeaf() )
				{
					for ( Tree< T > child : tree2.getChildren() )
					{
						double distanceZhangTree =
								distanceTree( tree1, child, costFunction, treeDistances, forestDistances ) - treeInsertMap.get( child );
						l.add( distanceZhangTree );
					}
					a += Collections.min( l );
				}
			}
			double b = 0;
			if ( treeDeleteMap != null )
			{
				b = treeDeleteMap.get( tree1 );
				List< Double > l = new ArrayList<>();
				if ( !tree1.isLeaf() )
				{
					for ( Tree< T > child : tree1.getChildren() )
					{
						l.add( distanceTree( child, tree2, costFunction, treeDistances, forestDistances ) - treeDeleteMap.get( child ) );
					}
					b += Collections.min( l );
				}
			}
			double c = distanceForest( tree1, tree2, forestDistances, treeDistances );
			if ( costFunction != null )
			{
				c += costTreeToTree.get( Pair.of( tree1, tree2 ) );
			}

			if ( tree1.isLeaf() || tree2.isLeaf() )
			{
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = c;
				return c;
			}
			else
			{
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
	}

	/**
	 * Calculate the zhang edit distance between two forests.
	 * What is a forest?
	 * "Suppose that we have a numbering for each tree.
	 * Let t[i] be the i<sup>th</sup> node of tree T in the given numbering.
	 * Let T[i] be the subtree rooted at t[i]
	 * Let F[i] be the unordered forest obtained by deleting t[i] from T[i]."
	 * Algorithmica (1996) 15:208
	 */
	private double distanceForest( Tree< T > forest1, Tree< T > forest2, double[][] forestDistances, double[][] treeDistances )
	{
		// Calculate the zhang edit distance between two subforests
		if ( forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] != -1 )
			return forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ];
		else
		{
			if ( forestDeleteMap != null && !forest1.isLeaf() && forest2.isLeaf() )
			{
				forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = forestDeleteMap.get( forest1 );
				return forestDeleteMap.get( forest1 );
			}

			if ( forestInsertMap != null && !forest2.isLeaf() && forest1.isLeaf() )
			{
				forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = forestInsertMap.get( forest2 );
				return forestInsertMap.get( forest2 );
			}

			if ( forestInsertMap != null && forestDeleteMap != null && !forest2.isLeaf() && !forest1.isLeaf() )
			{
				double a = forestInsertMap.get( forest2 );
				List< Double > l = new ArrayList<>();
				if ( !forest2.isLeaf() )
				{
					for ( Tree< T > child : forest2.getChildren() )
					{
						l.add( distanceForest( forest1, child, forestDistances, treeDistances )
								- forestInsertMap.get( child ) );
					}
					a += Collections.min( l );
				}
				double b = forestDeleteMap.get( forest1 );
				l = new ArrayList<>();
				if ( !forest1.isLeaf() )
				{
					for ( Tree< T > child : forest1.getChildren() )
					{
						l.add( distanceForest( child, forest2, forestDistances, treeDistances )
								- forestDeleteMap.get( child ) );
					}
					b += Collections.min( l );
				}
				double c = minCostMaxFlow( forest1, forest2, treeDistances );
				forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
		return 0;
	}

	private int minCostMaxFlow( Tree< T > forest1, Tree< T > forest2, double[][] treeDistances )
	{
		int numberOfEquivalenceClasses1;
		int numberOfEquivalenceClasses2;
		Map< Number, List< Tree< T > > > equivalenceClassToTrees1 = new LinkedHashMap<>();
		Map< Number, List< Tree< T > > > equivalenceClassToTrees2 = new LinkedHashMap<>();

		if ( equivalenceClasses != null )
		{
			for ( Tree< T > tree1 : forest1.getChildren() )
			{
				int equivalenceClass = equivalenceClasses.get( tree1 );
				if ( equivalenceClassToTrees1.containsKey( equivalenceClass ) )
				{
					List< Tree< T > > trees = equivalenceClassToTrees1.get( equivalenceClass );
					trees.add( tree1 );
				}
				else
				{
					List< Tree< T > > list = new ArrayList<>();
					list.add( tree1 );
					equivalenceClassToTrees1.put( equivalenceClass, list );
				}
			}

			for ( Tree< T > tree2 : forest2.getChildren() )
			{
				int equivalenceClass = equivalenceClasses.get( tree2 );
				if ( equivalenceClassToTrees2.containsKey( equivalenceClass ) )
				{
					List< Tree< T > > trees = equivalenceClassToTrees2.get( equivalenceClass );
					trees.add( tree2 );
				}
				else
				{
					List< Tree< T > > list = new ArrayList<>();
					list.add( tree2 );
					equivalenceClassToTrees2.put( equivalenceClass, list );
				}
			}
		}

		numberOfEquivalenceClasses1 = equivalenceClassToTrees1.keySet().size();
		numberOfEquivalenceClasses2 = equivalenceClassToTrees2.keySet().size();

		List< Integer > numberOfTreesWithEquivalenceClass1 = new ArrayList<>();
		int sumEquivalenceClass1 = 0;
		for ( Map.Entry< Number, List< Tree< T > > > treesWithEquivalenceClass : equivalenceClassToTrees1.entrySet() )
		{
			int numberOfTreesWithEquivalenceClass = treesWithEquivalenceClass.getValue().size();
			sumEquivalenceClass1 += numberOfTreesWithEquivalenceClass;
			numberOfTreesWithEquivalenceClass1.add( numberOfTreesWithEquivalenceClass );
		}

		List< Integer > numberOfTreesWithEquivalenceClass2 = new ArrayList<>();
		int sumEquivalenceClass2 = 0;
		for ( Map.Entry< Number, List< Tree< T > > > treesWithEquivalenceClass : equivalenceClassToTrees2.entrySet() )
		{
			int numberOfTreesWithEquivalenceClass = treesWithEquivalenceClass.getValue().size();
			sumEquivalenceClass2 += numberOfTreesWithEquivalenceClass;
			numberOfTreesWithEquivalenceClass2.add( numberOfTreesWithEquivalenceClass );
		}

		// Construction of graph for max flow min cost algorithm
		Integer source = 0;
		Integer sink = numberOfEquivalenceClasses1 + numberOfEquivalenceClasses2 + 1;
		Integer emptyTree1 = numberOfEquivalenceClasses1 + numberOfEquivalenceClasses2 + 2;
		Integer emptyTree2 = numberOfEquivalenceClasses1 + numberOfEquivalenceClasses2 + 3;

		SimpleDirectedWeightedGraph< Integer, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );
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
		capacities.put( e1, sumEquivalenceClass2 - Math.min( sumEquivalenceClass1, sumEquivalenceClass2 ) );
		capacities.put( e2,
				Math.max( sumEquivalenceClass1, sumEquivalenceClass2 ) - Math.min( sumEquivalenceClass1, sumEquivalenceClass2 ) );
		capacities.put( e3, sumEquivalenceClass1 - Math.min( sumEquivalenceClass1, sumEquivalenceClass2 ) );

		for ( int i = 0; i < numberOfEquivalenceClasses1; i++ )
		{
			if ( !graph.containsVertex( i + 1 ) )
				graph.addVertex( i + 1 );
			DefaultWeightedEdge edge = graph.addEdge( 0, i + 1 );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, numberOfTreesWithEquivalenceClass1.get( i ) );
			Tree< T > s1 = null;
			for ( int j = 0; j < numberOfEquivalenceClasses2; j++ )
			{
				Object[] keys1 = equivalenceClassToTrees1.keySet().toArray();
				Object key1 = keys1[ i ];
				List< Tree< T > > trees1 = equivalenceClassToTrees1.get( key1 );
				s1 = trees1.get( 0 );

				Object[] keys2 = equivalenceClassToTrees2.keySet().toArray();
				Object key2 = keys2[ j ];
				List< Tree< T > > trees2 = equivalenceClassToTrees2.get( key2 );
				Tree< T > s2 = trees2.get( 0 );

				double edgeWeight = treeDistances[ subtrees1.indexOf( s1 ) ][ subtrees2.indexOf( s2 ) ];
				Integer start = i + 1;
				Integer target = numberOfEquivalenceClasses1 + j + 1;
				if ( !graph.containsVertex( start ) )
					graph.addVertex( start );
				if ( !graph.containsVertex( target ) )
					graph.addVertex( target );
				edge = graph.addEdge( ( i + 1 ), ( numberOfEquivalenceClasses1 + j + 1 ) );
				graph.setEdgeWeight( edge, edgeWeight );
				capacities.put( edge, numberOfTreesWithEquivalenceClass1.get( i ) );
			}
			edge = graph.addEdge( ( i + 1 ), ( emptyTree2 ) );
			graph.setEdgeWeight( edge, treeDeleteMap.get( s1 ) );
			capacities.put( edge, numberOfTreesWithEquivalenceClass1.get( i ) );
		}
		for ( int j = 0; j < numberOfEquivalenceClasses2; j++ )
		{
			Object[] keys2 = equivalenceClassToTrees2.keySet().toArray();
			Object key2 = keys2[ j ];
			List< Tree< T > > trees2 = equivalenceClassToTrees2.get( key2 );
			Tree< T > s2 = trees2.get( 0 );

			DefaultWeightedEdge edge = graph.addEdge( emptyTree1, numberOfEquivalenceClasses1 + j + 1 );
			Double weight = treeInsertMap.get( s2 );
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, sumEquivalenceClass2 - Math.min( sumEquivalenceClass1, sumEquivalenceClass2 ) );

			edge = graph.addEdge( numberOfEquivalenceClasses1 + j + 1, sink );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, numberOfTreesWithEquivalenceClass2.get( j ) );
		}

		return ( int ) JGraphtTools.maxFlowMinCost( graph, capacities, 0, sink );
	}

	private static double min( double a, double b, double c )
	{
		return Math.min( Math.min( a, b ), c );
	}

	/**
	 * Compute the cost of deleting/inserting a forest/tree
	 * <p>
	 * The cost of deleting/inserting a tree is
	 * <li>the cost of deleting/inserting the attribute of its source
	 * <li>+ the cost of deleting/inserting the forest associated with that source
	 * <p>
	 * The cost of deleting/inserting a forest is the cost of deleting/inserting all trees belonging to it
	 * @param tree the tree/forest to compute the cost for
	 * @param costTreeToNone a mapping from tree to the cost of deleting/inserting the attribute of its source
	 * @return
	 */
	private Pair< Map< Tree< T >, Integer >, Map< Tree< T >, Integer > > computeDeleteInsertCostsForestTree(
			Tree< T > tree, @Nullable Map< Tree< T >, Integer > costTreeToNone )
	{

		Map< Tree< T >, Integer > forestCosts = new HashMap<>();
		Map< Tree< T >, Integer > treeCosts = new HashMap<>();

		if ( tree.isLeaf() )
		{
			forestCosts.put( tree, 0 );
			if ( costTreeToNone == null )
				treeCosts.put( tree, 1 );
			else
				treeCosts.put( tree, costTreeToNone.get( tree ) );
		}
		else
		{
			int cost = 0;
			for ( Tree< T > child : tree.getChildren() )
			{
				Map< Tree< T >, Integer > forestInsertCosts;
				Map< Tree< T >, Integer > treeInsertCosts;
				Pair< Map< Tree< T >, Integer >, Map< Tree< T >, Integer > > result =
						computeDeleteInsertCostsForestTree( child, costTreeToNone );
				forestInsertCosts = result.getKey();
				treeInsertCosts = result.getValue();
				cost += treeInsertCosts.get( child );
				forestCosts.putAll( forestInsertCosts );
				treeCosts.putAll( treeInsertCosts );
			}

			forestCosts.put( tree, cost );
			if ( costTreeToNone == null )
				treeCosts.put( tree, cost + 1 );
			else
				treeCosts.put( tree, cost + costTreeToNone.get( tree ) );
		}
		return Pair.of( forestCosts, treeCosts );
	}

	/**
	 * Computes the costs of deleting or inserting a forest
	 * <p>
	 * The cost of deleting or inserting a forest is the cost of deleting or inserting all trees belonging to it
	 * @param tree the forest to compute the cost for
	 * @param costTreeToNone a mapping from tree to the costs of deleting or inserting the attribute of its source
	 * @return a mapping from tree to the cost of deleting or inserting the forest associated with that tree
	 */
	private Map< Tree< T >, Double > getForestCosts( @Nullable Tree< T > tree,
			@Nullable Map< Tree< T >, Double > costTreeToNone )
	{
		if ( tree == null )
		{
			return Collections.emptyMap();
		}
		Map< Tree< T >, Double > deleteInsertCostForest = new HashMap<>();
		if ( tree.isLeaf() )
			deleteInsertCostForest.put( tree, 0d );
		else
		{
			double cost = 0;
			for ( Tree< T > child : tree.getChildren() )
			{
				Map< Tree< T >, Double > forestInsertCosts = getForestCosts( child, costTreeToNone );
				cost += getTreeCosts( child, costTreeToNone ).get( child );
				deleteInsertCostForest.putAll( forestInsertCosts );
			}
			deleteInsertCostForest.put( tree, cost );
		}
		return deleteInsertCostForest;
	}

	/**
	 * Computes the costs of deleting or inserting a tree
	 * <p>
	 * The cost of deleting or inserting a tree is
	 * <li>the cost of deleting or inserting the attribute of its source
	 * <li>+ the cost of deleting or inserting the forest associated with that source
	 * <p>
	 * The cost of deleting/inserting a forest is the cost of deleting/inserting all trees belonging to it
	 * @param tree the tree to compute the costs for
	 * @param costTreeToNone a mapping from tree to the costs of deleting or inserting the attribute of its source
	 * @return a mapping from tree to the costs of deleting or inserting it
	 */
	private Map< Tree< T >, Double > getTreeCosts(
			@Nullable Tree< T > tree, @Nullable Map< Tree< T >, Double > costTreeToNone )
	{
		if ( tree == null )
		{
			return Collections.emptyMap();
		}
		Map< Tree< T >, Double > costs = new HashMap<>();
		if ( tree.isLeaf() )
		{
			if ( costTreeToNone == null )
				costs.put( tree, 1d );
			else
				costs.put( tree, costTreeToNone.get( tree ) );
		}
		else
		{
			double cost = 0;
			for ( Tree< T > child : tree.getChildren() )
			{
				Map< Tree< T >, Double > childCosts = getTreeCosts( child, costTreeToNone );
				cost += childCosts.get( child );
				costs.putAll( childCosts );
			}
			if ( costTreeToNone == null )
				costs.put( tree, cost + 1 );
			else
				costs.put( tree, cost + costTreeToNone.get( tree ) );
		}
		return costs;
	}

	private static < T > int postOrder( Tree< T > tree, Map< Integer, Map< T, List< Tree< T > > > > graphDepthToClassifiedTrees,
			boolean useAttribute )
	{
		int depth = 0;
		List< Integer > depths = new ArrayList<>();
		for ( Tree< T > child : tree.getChildren() )
		{
			int d = postOrder( child, graphDepthToClassifiedTrees, useAttribute );
			depths.add( d );
			depth = Collections.max( depths );
		}

		Map< T, List< Tree< T > > > attributeToTrees = graphDepthToClassifiedTrees.computeIfAbsent( depth, k -> new HashMap<>() );
		T value = useAttribute ? tree.getAttribute() : null;
		List< Tree< T > > treesWithSameAttribute = attributeToTrees.get( value );
		if ( treesWithSameAttribute == null )
		{
			treesWithSameAttribute = new ArrayList<>();
		}
		treesWithSameAttribute.add( tree );
		attributeToTrees.put( value, treesWithSameAttribute );
		graphDepthToClassifiedTrees.put( depth, attributeToTrees );

		return depth + 1;
	}

	private static < T > Map< Tree< T >, Integer > getEquivalenceClasses( Tree< T > tree1, @Nullable Tree< T > tree2,
			boolean useAttribute )
	{
		Map< Tree< T >, Integer > equivalenceClasses = new HashMap<>();
		Map< Integer, Map< T, List< Tree< T > > > > graphDepthToClassifiedTrees = new LinkedHashMap<>();

		postOrder( tree1, graphDepthToClassifiedTrees, useAttribute );
		if ( tree2 != null )
			postOrder( tree2, graphDepthToClassifiedTrees, useAttribute );

		boolean subtreeClassNumberIncremented = false;

		int classNumber = 0;
		for ( Map.Entry< Integer, Map< T, List< Tree< T > > > > graphDepth : graphDepthToClassifiedTrees.entrySet() )
		{
			for ( Map.Entry< T, List< Tree< T > > > treesWithSameAttributeAtGraphDepth : graphDepth.getValue().entrySet() )
			{
				List< Tree< T > > treesWithSameAttribute = treesWithSameAttributeAtGraphDepth.getValue();
				for ( Tree< T > tree : treesWithSameAttribute )
				{
					equivalenceClasses.put( tree, classNumber );
					// NB: ensure that the two subtrees of the given tree have different class numbers, if the attribute is used
					if ( ( tree.equals( tree1 ) || tree.equals( tree2 ) ) && !subtreeClassNumberIncremented && useAttribute )
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
}
