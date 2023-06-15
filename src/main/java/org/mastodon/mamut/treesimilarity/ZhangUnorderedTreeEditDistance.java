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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class ZhangUnorderedTreeEditDistance
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private ZhangUnorderedTreeEditDistance()
	{
		// prevent from instantiation
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
	public static int distance( Tree< Number > tree1, @Nullable Tree< Number > tree2,
			@Nullable BiFunction< Number, Number, Integer > costFunction )
	{

		if ( tree2 == null )
		{
			if ( costFunction == null )
			{
				return TreeUtils.size( tree1 );
			}
			else
			{
				int distance = 0;
				for ( Tree< Number > subtree : TreeUtils.listOfSubtrees( tree1 ) )
				{
					distance += getCosts( subtree, null, costFunction );
				}
				return distance;
			}
		}

		Map< Tree< Number >, Integer > treeInsertMap;
		Map< Tree< Number >, Integer > forestInsertMap;
		Map< Tree< Number >, Integer > treeDeleteMap;
		Map< Tree< Number >, Integer > forestDeleteMap;

		Map< Tree< Number >, Integer > costTreeToNone = new HashMap<>();
		Map< Pair< Tree< Number >, Tree< Number > >, Integer > costTreeToTree = new HashMap<>();

		SimpleTree< Number > supertree = new SimpleTree<>( 0 );

		supertree.getChildren().add( tree1 );
		supertree.getChildren().add( tree2 );

		Map< Tree< Number >, Integer > equivalenceClasses;

		// list of nodes of tree1
		List< Tree< Number > > subtrees1 = TreeUtils.listOfSubtrees( tree1 );
		// list of nodes of tree2
		List< Tree< Number > > subtrees2 = TreeUtils.listOfSubtrees( tree2 );

		if ( costFunction == null )
		{
			equivalenceClasses = getEquivalenceClasses( supertree, false );
			Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > p1 =
					computeDeleteInsertCostsForestTree( tree1, null );
			forestDeleteMap = p1.getKey();
			treeDeleteMap = p1.getValue();

			Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > p2 =
					computeDeleteInsertCostsForestTree( tree2, null );
			forestInsertMap = p2.getKey();
			treeInsertMap = p2.getValue();
		}
		else
		{
			equivalenceClasses = getEquivalenceClasses( supertree, true );
			subtrees1.forEach( tree -> costTreeToNone.put( tree, getCosts( tree, null, costFunction ) ) );
			subtrees2.forEach( tree -> costTreeToNone.put( tree, getCosts( tree, null, costFunction ) ) );
			for ( Tree< Number > subtree1 : subtrees1 )
			{
				for ( Tree< Number > subtree2 : subtrees2 )
				{
					costTreeToTree.put( Pair.of( subtree1, subtree2 ),
							getCosts( subtree1, subtree2, costFunction ) );
				}
			}

			Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > p1 =
					computeDeleteInsertCostsForestTree( tree1, costTreeToNone );
			forestDeleteMap = p1.getKey();
			treeDeleteMap = p1.getValue();

			Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > p2 =
					computeDeleteInsertCostsForestTree( tree2, costTreeToNone );
			forestInsertMap = p2.getKey();
			treeInsertMap = p2.getValue();
		}
		int[][] forestDistances = new int[ subtrees1.size() ][ subtrees2.size() ];
		int[][] treeDistances = new int[ subtrees1.size() ][ subtrees2.size() ];

		for ( int[] row : forestDistances )
			Arrays.fill( row, -1 );
		for ( int[] row : treeDistances )
			Arrays.fill( row, -1 );

		if ( treeInsertMap == null || forestInsertMap == null || treeDeleteMap == null || forestDeleteMap == null )
		{
			throw new IllegalArgumentException( "One of the maps is null" );
		}

		int distance = distanceTree( tree1, tree2, costFunction, subtrees1, subtrees2, treeDistances, forestDistances, treeInsertMap,
				forestInsertMap,
				treeDeleteMap, forestDeleteMap, equivalenceClasses, costTreeToTree );

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
		for ( Tree< Number > subtree : subtrees1 )
		{
			logger.info( "tree deletion[{}] = {}", subtree, treeDeleteMap.get( subtree ) );
		}
		logger.info( "forest deletion costs (tree1):" );
		for ( Tree< Number > subtree : subtrees1 )
		{
			logger.info( "forest deletion[{}] = {}", subtree, forestDeleteMap.get( subtree ) );
		}
		logger.info( "tree insertion costs (tree2):" );
		for ( Tree< Number > subtree : subtrees2 )
		{
			logger.info( "tree insertion[{}] = {}", subtree, treeInsertMap.get( subtree ) );
		}
		logger.info( "forest insertion costs (tree2):" );
		for ( Tree< Number > subtree : subtrees2 )
		{
			logger.info( "forest insertion[{}] = {}", subtree, forestInsertMap.get( subtree ) );
		}

		return distance;
	}

	private static int getCosts( Tree< Number > tree1, Tree< Number > tree2, BiFunction< Number, Number, Integer > costFunction )
	{
		if ( tree2 == null )
			return costFunction.apply( tree1.getAttribute(), null );
		else
		{
			Number v1 = tree1.getAttribute();
			Number v2 = tree2.getAttribute();
			return costFunction.apply( v1, v2 );
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-trees
	 */
	private static int distanceTree( Tree< Number > tree1, Tree< Number > tree2,
			@Nullable BiFunction< Number, Number, Integer > costFunction, List< Tree< Number > > l1,
			List< Tree< Number > > l2, int[][] treeDistances, int[][] forestDistances,
			@Nullable Map< Tree< Number >, Integer > treeInsertMap, @Nullable Map< Tree< Number >, Integer > forestInsertMap,
			@Nullable Map< Tree< Number >, Integer > treeDeleteMap,
			@Nullable Map< Tree< Number >, Integer > forestDeleteMap, @Nullable Map< Tree< Number >, Integer > equivalenceClasses,
			Map< Pair< Tree< Number >, Tree< Number > >, Integer > costTreeToTree )
	{
		if ( equivalenceClasses != null && Objects.equals( equivalenceClasses.get( tree1 ), equivalenceClasses.get( tree2 ) ) )
		{
			treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
			forestDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
			return 0;
		}
		if ( treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] != -1 )
		{
			return treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ];
		}

		if ( tree1.isLeaf() && tree2.isLeaf() )
		{
			if ( costFunction == null )
			{
				treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
				forestDistances[ l2.indexOf( tree2 ) ][ l1.indexOf( tree1 ) ] = 0;
				return 0;
			}
			else
			{
				int value = costTreeToTree.get( Pair.of( tree1, tree2 ) );
				treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = value;
				return value;
			}
		}
		else
		{
			int a = 0;
			if ( treeInsertMap != null )
			{
				a = treeInsertMap.get( tree2 );
				List< Integer > l = new ArrayList<>();
				if ( !tree2.isLeaf() )
				{
					for ( Tree< Number > child : tree2.getChildren() )
					{
						int distanceZhangTree =
								distanceTree( tree1, child, costFunction, l1, l2, treeDistances, forestDistances, treeInsertMap,
										forestInsertMap, treeDeleteMap, forestDeleteMap,
										equivalenceClasses,
										costTreeToTree ) - treeInsertMap.get( child );
						l.add( distanceZhangTree );
					}
					a += Collections.min( l );
				}
			}
			int b = 0;
			if ( treeDeleteMap != null )
			{
				b = treeDeleteMap.get( tree1 );
				List< Integer > l = new ArrayList<>();
				if ( !tree1.isLeaf() )
				{
					for ( Tree< Number > child : tree1.getChildren() )
					{
						l.add( distanceTree( child, tree2, costFunction, l1, l2, treeDistances, forestDistances, treeInsertMap,
								forestInsertMap, treeDeleteMap, forestDeleteMap,
								equivalenceClasses,
								costTreeToTree ) - treeDeleteMap.get( child ) );
					}
					b += Collections.min( l );
				}
			}
			int c = distanceForest( tree1, tree2, l1, l2, forestDistances, treeDistances, forestInsertMap, forestDeleteMap, treeInsertMap,
					treeDeleteMap, equivalenceClasses );
			if ( costFunction != null )
			{
				c += costTreeToTree.get( Pair.of( tree1, tree2 ) );
			}

			if ( tree1.isLeaf() || tree2.isLeaf() )
			{
				treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = c;
				return c;
			}
			else
			{
				treeDistances[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = min( a, b, c );
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
	private static int distanceForest( Tree< Number > forest1, Tree< Number > forest2, List< Tree< Number > > l1,
			List< Tree< Number > > l2, int[][] forestDistances, int[][] treeDistances,
			@Nullable Map< Tree< Number >, Integer > forestInsertMap,
			@Nullable Map< Tree< Number >, Integer > forestDeleteMap, @Nullable Map< Tree< Number >, Integer > treeInsertMap,
			@Nullable Map< Tree< Number >, Integer > treeDeleteMap, @Nullable Map< Tree< Number >, Integer > equivalenceClasses )
	{
		// Calculate the zhang edit distance between two subforests
		if ( forestDistances[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] != -1 )
		{
			return forestDistances[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ];
		}
		else
		{
			if ( forestDeleteMap != null && !forest1.isLeaf() && forest2.isLeaf() )
			{
				forestDistances[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = forestDeleteMap.get( forest1 );
				return forestDeleteMap.get( forest1 );
			}

			if ( forestInsertMap != null && !forest2.isLeaf() && forest1.isLeaf() )
			{
				forestDistances[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = forestInsertMap.get( forest2 );
				return forestInsertMap.get( forest2 );
			}

			if ( forestInsertMap != null && forestDeleteMap != null && !forest2.isLeaf() && !forest1.isLeaf() )
			{
				int a = forestInsertMap.get( forest2 );
				List< Integer > l = new ArrayList<>();
				if ( !forest2.isLeaf() )
				{
					for ( Tree< Number > child : forest2.getChildren() )
					{
						l.add( distanceForest( forest1, child, l1, l2, forestDistances, treeDistances, forestInsertMap, forestDeleteMap,
								treeInsertMap, treeDeleteMap, equivalenceClasses )
								- forestInsertMap.get( child ) );
					}
					a += Collections.min( l );
				}
				int b = forestDeleteMap.get( forest1 );
				l = new ArrayList<>();
				if ( !forest1.isLeaf() )
				{
					for ( Tree< Number > child : forest1.getChildren() )
					{
						l.add( distanceForest( child, forest2, l1, l2, forestDistances, treeDistances, forestInsertMap, forestDeleteMap,
								treeInsertMap, treeDeleteMap, equivalenceClasses )
								- forestDeleteMap.get( child ) );
					}
					b += Collections.min( l );
				}
				int c = minCostMaxFlow( forest1, forest2, treeDistances, l1, l2, treeDeleteMap, treeInsertMap, equivalenceClasses );
				forestDistances[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
		return 0;
	}

	private static int minCostMaxFlow( Tree< Number > forest1, Tree< Number > forest2, int[][] treeDistances,
			@Nullable List< Tree< Number > > l1,
			@Nullable List< Tree< Number > > l2, @Nullable Map< Tree< Number >, Integer > treeDeleteMap,
			@Nullable Map< Tree< Number >, Integer > treeInsertMap,
			@Nullable Map< Tree< Number >, Integer > equivalenceClasses )
	{
		int numberOfEquivalenceClasses1;
		int numberOfEquivalenceClasses2;
		Map< Number, List< Tree< Number > > > equivalenceClassToTrees1 = new LinkedHashMap<>();
		Map< Number, List< Tree< Number > > > equivalenceClassToTrees2 = new LinkedHashMap<>();

		if ( equivalenceClasses != null )
		{
			for ( Tree< Number > tree1 : forest1.getChildren() )
			{
				int equivalenceClass = equivalenceClasses.get( tree1 );
				if ( equivalenceClassToTrees1.containsKey( equivalenceClass ) )
				{
					List< Tree< Number > > trees = equivalenceClassToTrees1.get( equivalenceClass );
					trees.add( tree1 );
				}
				else
				{
					List< Tree< Number > > list = new ArrayList<>();
					list.add( tree1 );
					equivalenceClassToTrees1.put( equivalenceClass, list );
				}
			}

			for ( Tree< Number > tree2 : forest2.getChildren() )
			{
				int equivalenceClass = equivalenceClasses.get( tree2 );
				if ( equivalenceClassToTrees2.containsKey( equivalenceClass ) )
				{
					List< Tree< Number > > trees = equivalenceClassToTrees2.get( equivalenceClass );
					trees.add( tree2 );
				}
				else
				{
					List< Tree< Number > > list = new ArrayList<>();
					list.add( tree2 );
					equivalenceClassToTrees2.put( equivalenceClass, list );
				}
			}
		}

		numberOfEquivalenceClasses1 = equivalenceClassToTrees1.keySet().size();
		numberOfEquivalenceClasses2 = equivalenceClassToTrees2.keySet().size();

		List< Integer > numberOfTreesWithEquivalenceClass1 = new ArrayList<>();
		int sumEquivalenceClass1 = 0;
		for ( Map.Entry< Number, List< Tree< Number > > > treesWithEquivalenceClass : equivalenceClassToTrees1.entrySet() )
		{
			int numberOfTreesWithEquivalenceClass = treesWithEquivalenceClass.getValue().size();
			sumEquivalenceClass1 += numberOfTreesWithEquivalenceClass;
			numberOfTreesWithEquivalenceClass1.add( numberOfTreesWithEquivalenceClass );
		}

		List< Integer > numberOfTreesWithEquivalenceClass2 = new ArrayList<>();
		int sumEquivalenceClass2 = 0;
		for ( Map.Entry< Number, List< Tree< Number > > > treesWithEquivalenceClass : equivalenceClassToTrees2.entrySet() )
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
			Tree< Number > s1 = null;
			for ( int j = 0; j < numberOfEquivalenceClasses2; j++ )
			{
				Object[] keys1 = equivalenceClassToTrees1.keySet().toArray();
				Object key1 = keys1[ i ];
				List< Tree< Number > > trees1 = equivalenceClassToTrees1.get( key1 );
				s1 = trees1.get( 0 );

				Object[] keys2 = equivalenceClassToTrees2.keySet().toArray();
				Object key2 = keys2[ j ];
				List< Tree< Number > > trees2 = equivalenceClassToTrees2.get( key2 );
				Tree< Number > s2 = trees2.get( 0 );

				int edgeWeight = treeDistances[ l1.indexOf( s1 ) ][ l2.indexOf( s2 ) ];
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
			List< Tree< Number > > trees2 = equivalenceClassToTrees2.get( key2 );
			Tree< Number > s2 = trees2.get( 0 );

			DefaultWeightedEdge edge = graph.addEdge( emptyTree1, numberOfEquivalenceClasses1 + j + 1 );
			Integer weight = treeInsertMap.get( s2 );
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, sumEquivalenceClass2 - Math.min( sumEquivalenceClass1, sumEquivalenceClass2 ) );

			edge = graph.addEdge( numberOfEquivalenceClasses1 + j + 1, sink );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, numberOfTreesWithEquivalenceClass2.get( j ) );
		}

		return ( int ) JGraphtTools.maxFlowMinCost( graph, capacities, 0, sink );
	}

	private static int min( int a, int b, int c )
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
	private static Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > computeDeleteInsertCostsForestTree(
			Tree< Number > tree, @Nullable Map< Tree< Number >, Integer > costTreeToNone )
	{

		Map< Tree< Number >, Integer > deleteInsertCostForest = new HashMap<>();
		Map< Tree< Number >, Integer > deleteInsertCostTree = new HashMap<>();

		if ( tree.isLeaf() )
		{
			deleteInsertCostForest.put( tree, 0 );
			if ( costTreeToNone == null )
			{
				deleteInsertCostTree.put( tree, 1 );
			}
			else
			{
				deleteInsertCostTree.put( tree, costTreeToNone.get( tree ) );
			}
		}
		else
		{
			int v = 0;
			for ( Tree< Number > child : tree.getChildren() )
			{
				Map< Tree< Number >, Integer > dfi;
				Map< Tree< Number >, Integer > dti;
				Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > result =
						computeDeleteInsertCostsForestTree( child, costTreeToNone );
				dfi = result.getKey();
				dti = result.getValue();
				v += dti.get( child );
				deleteInsertCostForest.putAll( dfi );
				deleteInsertCostTree.putAll( dti );
			}

			deleteInsertCostForest.put( tree, v );
			if ( costTreeToNone == null )
			{
				deleteInsertCostTree.put( tree, v + 1 );
			}
			else
			{
				deleteInsertCostTree.put( tree, v + costTreeToNone.get( tree ) );
			}
		}
		return Pair.of( deleteInsertCostForest, deleteInsertCostTree );
	}

	private static int postOrder( Tree< Number > tree, Map< Integer, Map< Number, List< Tree< Number > > > > graphDepthToClassifiedTrees,
			boolean useAttribute )
	{
		int depth = 0;
		List< Integer > depths = new ArrayList<>();
		for ( Tree< Number > child : tree.getChildren() )
		{
			int d = postOrder( child, graphDepthToClassifiedTrees, useAttribute );
			depths.add( d );
			depth = Collections.max( depths );
		}

		Map< Number, List< Tree< Number > > > attributeToTrees = graphDepthToClassifiedTrees.computeIfAbsent( depth, k -> new HashMap<>() );
		Number value = useAttribute ? tree.getAttribute() : null;
		List< Tree< Number > > treesWithSameAttribute = attributeToTrees.get( value );
		if ( treesWithSameAttribute == null )
		{
			treesWithSameAttribute = new ArrayList<>();
		}
		treesWithSameAttribute.add( tree );
		attributeToTrees.put( value, treesWithSameAttribute );
		graphDepthToClassifiedTrees.put( depth, attributeToTrees );

		return depth + 1;
	}

	private static Map< Tree< Number >, Integer > getEquivalenceClasses( Tree< Number > inputTree, boolean useAttribute )
	{
		Map< Tree< Number >, Integer > equivalenceClasses = new HashMap<>();
		Map< Integer, Map< Number, List< Tree< Number > > > > graphDepthToClassifiedTrees = new LinkedHashMap<>();

		postOrder( inputTree, graphDepthToClassifiedTrees, useAttribute );

		boolean subtreeClassNumberIncremented = false;
		Iterator< Tree< Number > > iterator = inputTree.getChildren().iterator();
		Tree< Number > subtree1 = iterator.next();
		Tree< Number > subtree2 = iterator.next();

		int classNumber = 0;
		for ( Map.Entry< Integer, Map< Number, List< Tree< Number > > > > graphDepth : graphDepthToClassifiedTrees.entrySet() )
		{
			for ( Map.Entry< Number, List< Tree< Number > > > treesWithSameAttributeAtGraphDepth : graphDepth.getValue().entrySet() )
			{
				List< Tree< Number > > treesWithSameAttribute = treesWithSameAttributeAtGraphDepth.getValue();
				// NB: we do not add the given tree itself
				treesWithSameAttribute.remove( inputTree );
				for ( Tree< Number > tree : treesWithSameAttributeAtGraphDepth.getValue() )
				{
					equivalenceClasses.put( tree, classNumber );
					// NB: ensure that the two subtrees of the given tree have different class numbers, if the attribute is used
					if ( ( tree.equals( subtree1 ) || tree.equals( subtree2 ) ) && !subtreeClassNumberIncremented && useAttribute )
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
