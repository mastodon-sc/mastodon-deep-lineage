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

	private final Map< Tree< T >, Double > treeInsertCosts;

	private final Map< Tree< T >, Double > forestInsertCosts;

	private final Map< Tree< T >, Double > treeDeleteCosts;

	private final Map< Tree< T >, Double > forestDeleteCosts;

	private final Double[][] forestDistances;

	private final Double[][] treeDistances;

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
	public static < T > double distance( final Tree< T > tree1, final @Nullable Tree< T > tree2,
			final @Nullable BiFunction< T, T, Double > costFunction )
	{
		// trivial cases
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
		ZhangUnorderedTreeEditDistance< T > zhang = new ZhangUnorderedTreeEditDistance<>( tree1, tree2, costFunction );
		return zhang.compute( tree1, tree2, costFunction );
	}

	private ZhangUnorderedTreeEditDistance( final Tree< T > tree1, final Tree< T > tree2,
			final @Nullable BiFunction< T, T, Double > costFunction )
	{
		subtrees1 = TreeUtils.listOfSubtrees( tree1 );
		subtrees2 = TreeUtils.listOfSubtrees( tree2 );

		if ( costFunction == null )
		{
			equivalenceClasses = getEquivalenceClasses( tree1, tree2, false );
			costTreeToTree = Collections.emptyMap();

			ChangeCosts deleteCosts = getChangeCosts( tree1, null );
			treeDeleteCosts = deleteCosts.treeCosts;
			forestDeleteCosts = deleteCosts.forestCosts;

			ChangeCosts insertCosts = getChangeCosts( tree2, null );
			treeInsertCosts = insertCosts.treeCosts;
			forestInsertCosts = insertCosts.forestCosts;
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

			ChangeCosts deleteCosts = getChangeCosts( tree1, costTreeToNone );
			treeDeleteCosts = deleteCosts.treeCosts;
			forestDeleteCosts = deleteCosts.forestCosts;

			ChangeCosts insertCosts = getChangeCosts( tree2, costTreeToNone );
			treeInsertCosts = insertCosts.treeCosts;
			forestInsertCosts = insertCosts.forestCosts;
		}

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
	private double compute( final Tree< T > tree1, final Tree< T > tree2, final @Nullable BiFunction< T, T, Double > costFunction )
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
			logger.info( "tree deletion[{}] = {}", subtree, treeDeleteCosts.get( subtree ) );
		}
		logger.info( "forest deletion costs (tree1):" );
		for ( Tree< T > subtree : subtrees1 )
		{
			logger.info( "forest deletion[{}] = {}", subtree, forestDeleteCosts.get( subtree ) );
		}
		logger.info( "tree insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
		{
			logger.info( "tree insertion[{}] = {}", subtree, treeInsertCosts.get( subtree ) );
		}
		logger.info( "forest insertion costs (tree2):" );
		for ( Tree< T > subtree : subtrees2 )
		{
			logger.info( "forest insertion[{}] = {}", subtree, forestInsertCosts.get( subtree ) );
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-trees
	 */
	private double distanceTree( final Tree< T > tree1, final Tree< T > tree2, final @Nullable BiFunction< T, T, Double > costFunction )
	{
		if ( Objects.equals( equivalenceClasses.get( tree1 ), equivalenceClasses.get( tree2 ) ) )
		{
			treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0d;
			forestDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0d;
			return 0;
		}
		double distance = treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ];
		if ( distance != -1 )
			return distance;

		if ( tree1.isLeaf() && tree2.isLeaf() )
		{
			if ( costFunction == null )
			{
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = 0d;
				forestDistances[ subtrees2.indexOf( tree2 ) ][ subtrees1.indexOf( tree1 ) ] = 0d;
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
			double a = treeInsertCosts.get( tree2 );
			List< Double > l1 = new ArrayList<>();
			if ( !tree2.isLeaf() )
			{
				for ( Tree< T > child : tree2.getChildren() )
					l1.add( distanceTree( tree1, child, costFunction ) - treeInsertCosts.get( child ) );
				a += Collections.min( l1 );
			}

			double b = treeDeleteCosts.get( tree1 );
			List< Double > l2 = new ArrayList<>();
			if ( !tree1.isLeaf() )
			{
				for ( Tree< T > child : tree1.getChildren() )
					l2.add( distanceTree( child, tree2, costFunction ) - treeDeleteCosts.get( child ) );
				b += Collections.min( l2 );
			}
			double c = distanceForest( tree1, tree2 );
			if ( costFunction != null )
				c += costTreeToTree.get( Pair.of( tree1, tree2 ) );

			if ( tree1.isLeaf() || tree2.isLeaf() )
			{
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = c;
				return c;
			}
			else
			{
				double minValue = min( a, b, c );
				treeDistances[ subtrees1.indexOf( tree1 ) ][ subtrees2.indexOf( tree2 ) ] = minValue;
				return minValue;
			}
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-forests.
	 * What is a forest?
	 * "Suppose that we have a numbering for each tree.
	 * Let t[i] be the i<sup>th</sup> node of tree T in the given numbering.
	 * Let T[i] be the subtree rooted at t[i]
	 * Let F[i] be the unordered forest obtained by deleting t[i] from T[i]."
	 * Algorithmica (1996) 15:208
	 */
	private double distanceForest( final Tree< T > forest1, final Tree< T > forest2 )
	{
		double distance = forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ];
		if ( distance != -1 )
			return distance;
		else
		{
			if ( !forest1.isLeaf() && forest2.isLeaf() )
			{
				forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = forestDeleteCosts.get( forest1 );
				return forestDeleteCosts.get( forest1 );
			}

			if ( !forest2.isLeaf() && forest1.isLeaf() )
			{
				forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = forestInsertCosts.get( forest2 );
				return forestInsertCosts.get( forest2 );
			}

			if ( forestDeleteCosts != null && !forest2.isLeaf() && !forest1.isLeaf() )
			{
				double a = forestInsertCosts.get( forest2 );
				List< Double > l = new ArrayList<>();
				if ( !forest2.isLeaf() )
				{
					for ( Tree< T > child : forest2.getChildren() )
						l.add( distanceForest( forest1, child ) - forestInsertCosts.get( child ) );
					a += Collections.min( l );
				}
				double b = forestDeleteCosts.get( forest1 );
				l = new ArrayList<>();
				if ( !forest1.isLeaf() )
				{
					for ( Tree< T > child : forest1.getChildren() )
						l.add( distanceForest( child, forest2 ) - forestDeleteCosts.get( child ) );
					b += Collections.min( l );
				}
				double c = minCostMaxFlow( forest1, forest2 );
				forestDistances[ subtrees1.indexOf( forest1 ) ][ subtrees2.indexOf( forest2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
		return 0;
	}

	private int minCostMaxFlow( final Tree< T > forest1, final Tree< T > forest2 )
	{
		Map< Integer, List< Tree< T > > > equivalenceClassesTree1 = new LinkedHashMap<>();
		for ( Tree< T > tree1 : forest1.getChildren() )
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

		Map< Integer, List< Tree< T > > > equivalenceClassesTree2 = new LinkedHashMap<>();
		for ( Tree< T > tree2 : forest2.getChildren() )
		{
			int equivalenceClass = equivalenceClasses.get( tree2 );
			if ( equivalenceClassesTree2.containsKey( equivalenceClass ) )
			{
				List< Tree< T > > trees = equivalenceClassesTree2.get( equivalenceClass );
				trees.add( tree2 );
			}
			else
			{
				List< Tree< T > > list = new ArrayList<>();
				list.add( tree2 );
				equivalenceClassesTree2.put( equivalenceClass, list );
			}
		}

		int numberOfEquivalenceClasses1 = equivalenceClassesTree1.keySet().size();
		int numberOfEquivalenceClasses2 = equivalenceClassesTree2.keySet().size();

		List< Integer > numberOfTreesWithEquivalenceClass1 = new ArrayList<>();
		int sumEquivalenceClass1 = 0;
		for ( Map.Entry< Integer, List< Tree< T > > > treesWithEquivalenceClass : equivalenceClassesTree1.entrySet() )
		{
			int numberOfTreesWithEquivalenceClass = treesWithEquivalenceClass.getValue().size();
			sumEquivalenceClass1 += numberOfTreesWithEquivalenceClass;
			numberOfTreesWithEquivalenceClass1.add( numberOfTreesWithEquivalenceClass );
		}

		List< Integer > numberOfTreesWithEquivalenceClass2 = new ArrayList<>();
		int sumEquivalenceClass2 = 0;
		for ( Map.Entry< Integer, List< Tree< T > > > treesWithEquivalenceClass : equivalenceClassesTree2.entrySet() )
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
				Object[] keys1 = equivalenceClassesTree1.keySet().toArray();
				Object key1 = keys1[ i ];
				List< Tree< T > > trees1 = equivalenceClassesTree1.get( key1 );
				s1 = trees1.get( 0 );

				Object[] keys2 = equivalenceClassesTree2.keySet().toArray();
				Object key2 = keys2[ j ];
				List< Tree< T > > trees2 = equivalenceClassesTree2.get( key2 );
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
			graph.setEdgeWeight( edge, treeDeleteCosts.get( s1 ) );
			capacities.put( edge, numberOfTreesWithEquivalenceClass1.get( i ) );
		}
		for ( int j = 0; j < numberOfEquivalenceClasses2; j++ )
		{
			Object[] keys2 = equivalenceClassesTree2.keySet().toArray();
			Object key2 = keys2[ j ];
			List< Tree< T > > trees2 = equivalenceClassesTree2.get( key2 );
			Tree< T > s2 = trees2.get( 0 );

			DefaultWeightedEdge edge = graph.addEdge( emptyTree1, numberOfEquivalenceClasses1 + j + 1 );
			Double weight = treeInsertCosts.get( s2 );
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, sumEquivalenceClass2 - Math.min( sumEquivalenceClass1, sumEquivalenceClass2 ) );

			edge = graph.addEdge( numberOfEquivalenceClasses1 + j + 1, sink );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, numberOfTreesWithEquivalenceClass2.get( j ) );
		}

		return ( int ) JGraphtTools.maxFlowMinCost( graph, capacities, 0, sink );
	}

	private static double min( final double a, final double b, final double c )
	{
		return Math.min( Math.min( a, b ), c );
	}

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
	 * @return the change costs
	 */
	private ChangeCosts getChangeCosts( final Tree< T > tree, final @Nullable Map< Tree< T >, Double > costTreeToNone )
	{

		Map< Tree< T >, Double > treeCosts = new HashMap<>();
		Map< Tree< T >, Double > forestCosts = new HashMap<>();

		if ( tree.isLeaf() )
		{
			forestCosts.put( tree, 0d );
			if ( costTreeToNone == null )
				treeCosts.put( tree, 1d );
			else
				treeCosts.put( tree, costTreeToNone.get( tree ) );
		}
		else
		{
			double cost = 0;
			for ( Tree< T > child : tree.getChildren() )
			{
				Map< Tree< T >, Double > childForestChangeCosts;
				Map< Tree< T >, Double > childTreeChangeCosts;
				ChangeCosts costs = getChangeCosts( child, costTreeToNone );
				childTreeChangeCosts = costs.treeCosts;
				childForestChangeCosts = costs.forestCosts;

				cost += childTreeChangeCosts.get( child );
				treeCosts.putAll( childTreeChangeCosts );
				forestCosts.putAll( childForestChangeCosts );
			}

			forestCosts.put( tree, cost );
			if ( costTreeToNone == null )
				treeCosts.put( tree, cost + 1 );
			else
				treeCosts.put( tree, cost + costTreeToNone.get( tree ) );
		}
		return new ChangeCosts( treeCosts, forestCosts );
	}

	private static < T > int postOrder( final Tree< T > tree, final Map< Integer, Map< T, List< Tree< T > > > > graphDepthToClassifiedTrees,
			final boolean useAttribute )
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

	private static < T > Map< Tree< T >, Integer > getEquivalenceClasses( final Tree< T > tree1, final Tree< T > tree2,
			final boolean useAttribute )
	{
		Map< Tree< T >, Integer > equivalenceClasses = new HashMap<>();
		Map< Integer, Map< T, List< Tree< T > > > > graphDepthToClassifiedTrees = new LinkedHashMap<>();

		postOrder( tree1, graphDepthToClassifiedTrees, useAttribute );
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

	private class ChangeCosts
	{
		private final Map< Tree< T >, Double > treeCosts;

		private final Map< Tree< T >, Double > forestCosts;

		private ChangeCosts( final Map< Tree< T >, Double > treeCosts, final Map< Tree< T >, Double > forestCosts )
		{
			this.treeCosts = treeCosts;
			this.forestCosts = forestCosts;
		}
	}
}
