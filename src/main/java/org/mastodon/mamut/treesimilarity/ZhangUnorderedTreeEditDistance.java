package org.mastodon.mamut.treesimilarity;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import javax.annotation.Nullable;
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
			@Nullable BiFunction< Number, Number, Integer > costFunction, boolean verbose )
	{

		if ( tree2 == null )
		{
			if ( costFunction == null )
			{
				return tree1.size();
			}
			else
			{
				int distance = 0;
				for ( Tree< Number > subtree : tree1.listOfSubtrees() )
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
		List< Tree< Number > > subtrees1 = tree1.listOfSubtrees();
		// list of nodes of tree2
		List< Tree< Number > > subtrees2 = tree2.listOfSubtrees();

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
		int[][] gridf = new int[ subtrees1.size() ][ subtrees2.size() ];
		int[][] gridt = new int[ subtrees1.size() ][ subtrees2.size() ];

		for ( int[] row : gridf )
			Arrays.fill( row, -1 );
		for ( int[] row : gridt )
			Arrays.fill( row, -1 );

		// TODO add verbose == true
		if ( treeInsertMap == null || forestInsertMap == null || treeDeleteMap == null || forestDeleteMap == null )
		{
			throw new IllegalArgumentException( "One of the maps is null" );
		}
		return distanceZhangTree( tree1, tree2, costFunction, subtrees1, subtrees2, gridt, gridf, treeInsertMap, forestInsertMap,
				treeDeleteMap, forestDeleteMap, equivalenceClasses, verbose, costTreeToTree );
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
	private static int distanceZhangTree( Tree< Number > tree1, Tree< Number > tree2,
			@Nullable BiFunction< Number, Number, Integer > costFunction, List< Tree< Number > > l1,
			List< Tree< Number > > l2, int[][] mt, int[][] mf,
			@Nullable Map< Tree< Number >, Integer > dit, @Nullable Map< Tree< Number >, Integer > dif,
			@Nullable Map< Tree< Number >, Integer > dst,
			@Nullable Map< Tree< Number >, Integer > dsf, @Nullable Map< Tree< Number >, Integer > equivalenceClasses, boolean verbose,
			Map< Pair< Tree< Number >, Tree< Number > >, Integer > costTreeToTree )
	{
		if ( equivalenceClasses != null && Objects.equals( equivalenceClasses.get( tree1 ), equivalenceClasses.get( tree2 ) ) && !verbose )
		{
			mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
			mf[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
			return 0;
		}
		if ( mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] != -1 )
		{
			return mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ];
		}

		if ( tree1.isLeaf() && tree2.isLeaf() )
		{
			if ( costFunction == null )
			{
				mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
				mf[ l2.indexOf( tree2 ) ][ l1.indexOf( tree1 ) ] = 0;
				return 0;
			}
			else
			{
				int value = costTreeToTree.get( Pair.of( tree1, tree2 ) );
				mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = value;
				return value;
			}
		}
		else
		{
			int a = 0;
			if ( dit != null )
			{
				a = dit.get( tree2 );
				List< Integer > l = new ArrayList<>();
				if ( !tree2.isLeaf() )
				{
					for ( Tree< Number > child : tree2.getChildren() )
					{
						int distanceZhangTree =
								distanceZhangTree( tree1, child, costFunction, l1, l2, mt, mf, dit, dif, dst, dsf, equivalenceClasses,
										verbose,
										costTreeToTree ) - dit.get( child );
						l.add( distanceZhangTree );
					}
					a += Collections.min( l );
				}
			}
			int b = 0;
			if ( dst != null )
			{
				b = dst.get( tree1 );
				List< Integer > l = new ArrayList<>();
				if ( !tree1.isLeaf() )
				{
					for ( Tree< Number > child : tree1.getChildren() )
					{
						l.add( distanceZhangTree( child, tree2, costFunction, l1, l2, mt, mf, dit, dif, dst, dsf, equivalenceClasses,
								verbose,
								costTreeToTree ) - dst.get( child ) );
					}
					b += Collections.min( l );
				}
			}
			int c = distanceZhangForest( tree1, tree2, costFunction, l1, l2, mf, mt, dif, dsf, dit, dst, equivalenceClasses );
			if ( costFunction != null )
			{
				c += costTreeToTree.get( Pair.of( tree1, tree2 ) );
			}

			if ( tree1.isLeaf() || tree2.isLeaf() )
			{
				mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = c;
				return c;
			}
			else
			{
				mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
	}

	private static int distanceZhangForest( Tree< Number > forest1, Tree< Number > forest2,
			BiFunction< Number, Number, Integer > costFunction,
			List< Tree< Number > > l1,
			List< Tree< Number > > l2, int[][] mf, int[][] mt, @Nullable Map< Tree< Number >, Integer > dif,
			@Nullable Map< Tree< Number >, Integer > dsf,
			@Nullable Map< Tree< Number >, Integer > dit, @Nullable Map< Tree< Number >, Integer > dst,
			@Nullable Map< Tree< Number >, Integer > equivalenceClasses )
	{
		// Calculate the zhang edit distance between two subforests
		if ( mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] != -1 )
		{
			return mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ];
		}
		else
		{
			if ( dsf != null && !forest1.isLeaf() && forest2.isLeaf() )
			{
				mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = dsf.get( forest1 );
				return dsf.get( forest1 );
			}

			if ( dif != null && !forest2.isLeaf() && forest1.isLeaf() )
			{
				mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = dif.get( forest2 );
				return dif.get( forest2 );
			}

			if ( dif != null && dsf != null && !forest2.isLeaf() && !forest1.isLeaf() )
			{
				int a = dif.get( forest2 );
				List< Integer > l = new ArrayList<>();
				if ( !forest2.isLeaf() )
				{
					for ( Tree< Number > child : forest2.getChildren() )
					{
						l.add( distanceZhangForest( forest1, child, costFunction, l1, l2, mf, mt, dif, dsf, dit, dst, equivalenceClasses )
								- dif.get( child ) );
					}
					a += Collections.min( l );
				}
				int b = dsf.get( forest1 );
				l = new ArrayList<>();
				if ( !forest1.isLeaf() )
				{
					for ( Tree< Number > child : forest1.getChildren() )
					{
						l.add( distanceZhangForest( child, forest2, costFunction, l1, l2, mf, mt, dif, dsf, dit, dst, equivalenceClasses )
								- dsf.get( child ) );
					}
					b += Collections.min( l );
				}
				int c = minCostMaxFlow( forest1, forest2, costFunction, mt, l1, l2, dst, dit, equivalenceClasses );
				mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
		return 0;
	}

	private static int minCostMaxFlow( Tree< Number > forest1, Tree< Number > forest2,
			@Nullable BiFunction< Number, Number, Integer > costFunction,
			int[][] mt,
			@Nullable List< Tree< Number > > l1, @Nullable List< Tree< Number > > l2, @Nullable Map< Tree< Number >, Integer > dst,
			@Nullable Map< Tree< Number >, Integer > dit, @Nullable Map< Tree< Number >, Integer > equivalenceClasses )
	{
		int n1;
		int n2;
		Map< Object, List< Tree< Number > > > dico1 = new LinkedHashMap<>();
		Map< Object, List< Tree< Number > > > dico2 = new LinkedHashMap<>();

		if ( equivalenceClasses != null )
		{
			for ( Tree< Number > tree1 : forest1.getChildren() )
			{
				int equivalenceClass = equivalenceClasses.get( tree1 );
				if ( dico1.containsKey( equivalenceClass ) )
				{
					List< Tree< Number > > trees = dico1.get( equivalenceClass );
					trees.add( tree1 );
				}
				else
				{
					List< Tree< Number > > list = new ArrayList<>();
					list.add( tree1 );
					dico1.put( equivalenceClass, list );
				}
			}

			for ( Tree< Number > tree2 : forest2.getChildren() )
			{
				int equivalenceClass = equivalenceClasses.get( tree2 );
				if ( dico2.containsKey( equivalenceClass ) )
				{
					List< Tree< Number > > trees = dico2.get( equivalenceClass );
					trees.add( tree2 );
				}
				else
				{
					List< Tree< Number > > list = new ArrayList<>();
					list.add( tree2 );
					dico2.put( equivalenceClass, list );
				}
			}
		}

		n1 = dico1.keySet().size();
		n2 = dico2.keySet().size();

		List< Integer > N1 = new ArrayList<>();
		for ( Object key : dico1.keySet() )
		{
			N1.add( dico1.get( key ).size() );
		}
		int N1s = N1.stream().mapToInt( Integer::intValue ).sum();

		List< Integer > N2 = new ArrayList<>();
		for ( Object key : dico2.keySet() )
		{
			N2.add( dico2.get( key ).size() );
		}
		int N2s = N2.stream().mapToInt( Integer::intValue ).sum();

		// Construction of graph for max flow min cost algorithm
		// 0 source
		// (n1+n2+1) sink
		// (n1+n2+2) empty tree 1
		// (n1+n2+3) empty tree 2

		SimpleDirectedWeightedGraph< Integer, DefaultWeightedEdge > graph = new SimpleDirectedWeightedGraph<>( DefaultWeightedEdge.class );
		graph.addVertex( 0 );
		graph.addVertex( n1 + n2 + 1 );
		graph.addVertex( n1 + n2 + 2 );
		graph.addVertex( n1 + n2 + 3 );

		DefaultWeightedEdge e1 = graph.addEdge( 0, n1 + n2 + 2 );
		DefaultWeightedEdge e2 = graph.addEdge( n1 + n2 + 3, n1 + n2 + 1 );
		DefaultWeightedEdge e3 = graph.addEdge( n1 + n2 + 2, n1 + n2 + 3 );

		graph.setEdgeWeight( e1, 0 );
		graph.setEdgeWeight( e2, 0 );
		graph.setEdgeWeight( e3, 0 );

		Map< DefaultWeightedEdge, Integer > capacities = new HashMap<>();
		capacities.put( e1, N2s - Math.min( N1s, N2s ) );
		capacities.put( e2, N1s - Math.min( N1s, N2s ) );
		capacities.put( e3, Math.max( N1s, N2s ) - Math.min( N1s, N2s ) );

		for ( int i = 0; i < n1; i++ )
		{
			if ( !graph.containsVertex( i + 1 ) )
				graph.addVertex( i + 1 );
			DefaultWeightedEdge edge = graph.addEdge( 0, i + 1 );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, N1.get( i ) );
			Tree< Number > s1 = null;
			for ( int j = 0; j < n2; j++ )
			{
				Object[] keys1 = dico1.keySet().toArray();
				Object key1 = keys1[ i ];
				List< Tree< Number > > trees1 = dico1.get( key1 );
				s1 = trees1.get( 0 );

				Object[] keys2 = dico2.keySet().toArray();
				Object key2 = keys2[ j ];
				List< Tree< Number > > trees2 = dico2.get( key2 );
				Tree< Number > s2 = trees2.get( 0 );

				int distance_a_calculer = mt[ l1.indexOf( s1 ) ][ l2.indexOf( s2 ) ];
				Integer source = i + 1;
				Integer target = n1 + j + 1;
				if ( !graph.containsVertex( source ) )
					graph.addVertex( source );
				if ( !graph.containsVertex( target ) )
					graph.addVertex( target );
				edge = graph.addEdge( ( i + 1 ), ( n1 + j + 1 ) );
				graph.setEdgeWeight( edge, distance_a_calculer );
				capacities.put( edge, N1.get( i ) );
			}
			edge = graph.addEdge( ( i + 1 ), ( n1 + n2 + 3 ) );
			graph.setEdgeWeight( edge, dst.get( s1 ) );
			capacities.put( edge, N1.get( i ) );
		}
		for ( int j = 0; j < n2; j++ )
		{
			Object[] keys2 = dico2.keySet().toArray();
			Object key2 = keys2[ j ];
			List< Tree< Number > > trees2 = dico2.get( key2 );
			Tree< Number > s2 = trees2.get( 0 );

			DefaultWeightedEdge edge = graph.addEdge( n1 + n2 + 2, n1 + j + 1 );
			Integer weight = dit.get( s2 );
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, N2s - Math.min( N1s, N2s ) );

			edge = graph.addEdge( n1 + j + 1, n1 + n2 + 1 );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, N2.get( j ) );
		}

		return ( int ) JGraphtTools.maxFlowMinCost( graph, capacities, 0, n1 + n2 + 1 );
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
	 * @param tree
	 * @param costTreeToNone
	 * @return
	 */
	private static Pair< Map< Tree< Number >, Integer >, Map< Tree< Number >, Integer > > computeDeleteInsertCostsForestTree(
			Tree< Number > tree, @Nullable Map< Tree< Number >, Integer > costTreeToNone )
	{

		Map< Tree< Number >, Integer > df = new HashMap<>();
		Map< Tree< Number >, Integer > dt = new HashMap<>();

		if ( tree.isLeaf() )
		{
			df.put( tree, 0 );
			if ( costTreeToNone == null )
			{
				dt.put( tree, 1 );
			}
			else
			{
				dt.put( tree, costTreeToNone.get( tree ) );
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
				df.putAll( dfi );
				dt.putAll( dti );
			}

			df.put( tree, v );
			if ( costTreeToNone == null )
			{
				dt.put( tree, v + 1 );
			}
			else
			{
				dt.put( tree, v + costTreeToNone.get( tree ) );
			}
		}
		return Pair.of( df, dt );
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

	private static Map< Tree< Number >, Integer > getEquivalenceClasses( Tree< Number > tree, boolean useAttribute )
	{
		Map< Tree< Number >, Integer > equivalenceClasses = new HashMap<>();
		Map< Integer, Map< Number, List< Tree< Number > > > > graphDepthToClassifiedTrees = new LinkedHashMap<>();

		postOrder( tree, graphDepthToClassifiedTrees, useAttribute );

		boolean ensureDifferentClassNumber = false;
		Iterator< Tree< Number > > iterator = tree.getChildren().iterator();
		Tree< Number > tree1 = iterator.next();
		Tree< Number > tree2 = iterator.next();

		int classNumber = 0;
		for ( Map.Entry< Integer, Map< Number, List< Tree< Number > > > > graphDepth : graphDepthToClassifiedTrees.entrySet() )
		{
			for ( Map.Entry< Number, List< Tree< Number > > > treesWithSameAttributeAtGraphDepth : graphDepth.getValue().entrySet() )
			{
				for ( Tree< Number > t : treesWithSameAttributeAtGraphDepth.getValue() )
				{
					// NB: we do not add the class number of the given tree itself
					if ( tree.equals( t ) )
						continue;
					equivalenceClasses.put( t, classNumber );
					if ( ( t.equals( tree1 ) || t.equals( tree2 ) ) && !ensureDifferentClassNumber && useAttribute )
					{
						classNumber++;
						ensureDifferentClassNumber = true;
					}
				}
				classNumber++;
			}
		}
		return equivalenceClasses;
	}
}
