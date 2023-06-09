package org.mastodon.mamut.treesimilarity;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

public class ZhangUnorderedTreeEditDistance
{

	/**
	 * Calculate the Zhang edit distance between two (labeled) unordered trees.
	 *
	 * @param tree1 Tree object representing the first tree.
	 * @param tree2 Tree object representing the second tree.
	 * @param label_attribute Tuple representing the label attribute.
	 * @param local_distance Optional cost function.
	 *
	 * @return The Zhang edit distance between tree1 and tree2 as an integer.
	 */
	public static int zhang_edit_distance( Tree tree1, @Nullable Tree tree2, String label_attribute,
			@Nullable BiFunction< Object, Object, Integer > local_distance, boolean verbose )
	{

		if ( tree2 == null )
		{
			// TODO implementation missing for the case local_distance == null
			if ( local_distance != null )
			{
				int s = 0;
				for ( Tree subtree : tree1.listOfSubtrees() )
				{
					s += cost_function( subtree, null, label_attribute, local_distance );
				}
				return s;
			}
		}

		Map< Tree, Integer > dico_ins_tree;
		Map< Tree, Integer > dico_ins_forest;
		Map< Tree, Integer > dico_supp_tree;
		Map< Tree, Integer > dico_supp_forest;

		Map< UUID, Integer > cost_tree_to_none = new HashMap<>();
		Map< Pair< UUID, UUID >, Integer > cost_tree_to_tree = new HashMap<>();

		// TODO implementation missing for the case local_distance == null
		if ( local_distance != null )
		{
			Tree supertree = new Tree();
			supertree.addAttributeToId( label_attribute, 1 );

			supertree.getMyChildren().add( tree1 );
			supertree.getMyChildren().add( tree2 );

			Map< UUID, Integer > dic_class = getUnorderedEquivalenceClassWithAttribute( supertree, label_attribute );

			// list of nodes of tree1
			List< Tree > l1 = tree1.listOfSubtrees();
			// list of nodes of tree2
			List< Tree > l2 = tree2.listOfSubtrees();

			for ( Tree st : l1 )
			{
				cost_tree_to_none.put( st.getMyId(), cost_function( st, null, label_attribute, local_distance ) );
			}
			for ( Tree st : l2 )
			{
				cost_tree_to_none.put( st.getMyId(), cost_function( st, null, label_attribute, local_distance ) );
			}
			for ( Tree st : l1 )
			{
				for ( Tree st2 : l2 )
				{
					cost_tree_to_tree.put( Pair.of( st.getMyId(), st2.getMyId() ),
							cost_function( st, st2, label_attribute, local_distance ) );
				}
			}

			Pair< Map< Tree, Integer >, Map< Tree, Integer > > p1 = calcul_cost_sup_ins_forest_tree( tree1, cost_tree_to_none );
			dico_supp_forest = p1.getKey();
			dico_supp_tree = p1.getValue();

			Pair< Map< Tree, Integer >, Map< Tree, Integer > > p2 = calcul_cost_sup_ins_forest_tree( tree2, cost_tree_to_none );
			dico_ins_forest = p2.getKey();
			dico_ins_tree = p2.getValue();

			int[][] gridf = new int[ l1.size() ][ l2.size() ];
			int[][] gridt = new int[ l1.size() ][ l2.size() ];

			for ( int[] row : gridf )
				Arrays.fill( row, -1 );
			for ( int[] row : gridt )
				Arrays.fill( row, -1 );

			// TODO add verbose == true
			if ( dico_ins_tree == null || dico_ins_forest == null || dico_supp_tree == null || dico_supp_forest == null )
			{
				throw new IllegalArgumentException(
						"dico_ins_tree == null || dico_ins_forest == null || dico_supp_tree == null || dico_supp_forest == null" );
			}
			int distance =
					distance_zhang_tree( tree1, tree2, local_distance, l1, l2, gridt, gridf, dico_ins_tree, dico_ins_forest, dico_supp_tree,
							dico_supp_forest, dic_class, verbose, cost_tree_to_tree );
			return distance;
		}
		return 0;
	}

	private static int cost_function( Tree tree1, Tree tree2, String label_attribute, BiFunction< Object, Object, Integer > local_distance )
	{
		if ( tree2 == null )
			return local_distance.apply( tree1.getMyAttributes().get( label_attribute ), null );
		else
		{
			Object v1 = tree1.getMyAttributes().get( label_attribute );
			Object v2 = tree2.getMyAttributes().get( label_attribute );
			return local_distance.apply( v1, v2 );
		}
	}

	/**
	 * Calculate the zhang edit distance between two sub-trees
	 */
	private static int distance_zhang_tree( Tree tree1, Tree tree2, BiFunction< Object, Object, Integer > local_distance, List< Tree > l1,
			List< Tree > l2, int[][] mt, int[][] mf,
			@Nullable Map< Tree, Integer > dit, @Nullable Map< Tree, Integer > dif, @Nullable Map< Tree, Integer > dst,
			@Nullable Map< Tree, Integer > dsf, @Nullable Map< UUID, Integer > dic_class, boolean verbose,
			Map< Pair< UUID, UUID >, Integer > cost_tree_to_tree )
	{
		if ( dic_class != null && Objects.equals( dic_class.get( tree1.getMyId() ), dic_class.get( tree2.getMyId() ) ) && !verbose )
		{
			mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
			mf[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = 0;
			return 0;
		}
		if ( mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] != -1 )
		{
			return mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ];
		}

		if ( tree1.getMyChildren().size() == 0 && tree2.getMyChildren().size() == 0 )
		{
			// TODO implementation missing for the case local_distance == null
			if ( local_distance != null )
			{
				int value = cost_tree_to_tree.get( Pair.of( tree1.getMyId(), tree2.getMyId() ) );
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
				if ( tree2.getMyChildren().size() > 0 )
				{
					for ( Tree child : tree2.getMyChildren() )
					{
						int distance_zhang_tree =
								distance_zhang_tree( tree1, child, local_distance, l1, l2, mt, mf, dit, dif, dst, dsf, dic_class, verbose,
										cost_tree_to_tree ) - dit.get( child );
						l.add( distance_zhang_tree );
					}
					a += Collections.min( l );
				}
			}
			int b = 0;
			if ( dst != null )
			{
				b = dst.get( tree1 );
				List< Integer > l = new ArrayList<>();
				if ( tree1.getMyChildren().size() > 0 )
				{
					for ( Tree child : tree1.getMyChildren() )
					{
						l.add( distance_zhang_tree( child, tree2, local_distance, l1, l2, mt, mf, dit, dif, dst, dsf, dic_class, verbose,
								cost_tree_to_tree ) - dst.get( child ) );
					}
					b += Collections.min( l );
				}
			}
			int c = 0;
			// TODO implementation missing for the case local_distance == null
			if ( local_distance != null )
			{
				c += distance_zhang_forest( tree1, tree2, local_distance, l1, l2, mf, mt, dif, dsf, dit, dst );
				c += cost_tree_to_tree.get( Pair.of( tree1.getMyId(), tree2.getMyId() ) );
			}

			if ( tree1.getMyChildren().size() == 0 || tree2.getMyChildren().size() == 0 )
			{
				mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = c;
				return c;
			}
			else
			{
				mt[ l1.indexOf( tree1 ) ][ l2.indexOf( tree2 ) ] = min( a, b, c );
			}
			int cost = min( a, b, c );
			return cost;
		}
		return 0;
	}

	private static int distance_zhang_forest( Tree forest1, Tree forest2, BiFunction< Object, Object, Integer > local_distance,
			List< Tree > l1,
			List< Tree > l2, int[][] mf, int[][] mt, @Nullable Map< Tree, Integer > dif, @Nullable Map< Tree, Integer > dsf,
			@Nullable Map< Tree, Integer > dit, @Nullable Map< Tree, Integer > dst )
	{
		// Calculate the zhang edit distance between two subforests
		if ( mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] != -1 )
		{
			return mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ];
		}
		else
		{
			if ( dsf != null && forest1.getMyChildren().size() > 0 && forest2.getMyChildren().size() == 0 )
			{
				mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = dsf.get( forest1 );
				return dsf.get( forest1 );
			}

			if ( dif != null && forest2.getMyChildren().size() > 0 && forest1.getMyChildren().size() == 0 )
			{
				mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = dif.get( forest2 );
				return dif.get( forest2 );
			}

			if ( dif != null && dsf != null && forest2.getMyChildren().size() > 0 && forest1.getMyChildren().size() > 0 )
			{
				int a = dif.get( forest2 );
				List< Integer > l = new ArrayList<>();
				if ( forest2.getMyChildren().size() > 0 )
				{
					for ( Tree child : forest2.getMyChildren() )
					{
						l.add( distance_zhang_forest( forest1, child, local_distance, l1, l2, mf, mt, dif, dsf, dit, dst )
								- dif.get( child ) );
					}
					a += Collections.min( l );
				}
				int b = dsf.get( forest1 );
				l = new ArrayList<>();
				if ( forest1.getMyChildren().size() > 0 )
				{
					for ( Tree child : forest1.getMyChildren() )
					{
						l.add( distance_zhang_forest( child, forest2, local_distance, l1, l2, mf, mt, dif, dsf, dit, dst )
								- dsf.get( child ) );
					}
					b += Collections.min( l );
				}
				int c = mincostmaxflot( forest1, forest2, local_distance, mt, l1, l2, dst, dit );
				mf[ l1.indexOf( forest1 ) ][ l2.indexOf( forest2 ) ] = min( a, b, c );
				return min( a, b, c );
			}
		}
		return 0;
	}

	private static int mincostmaxflot( Tree forest1, Tree forest2, @Nullable BiFunction< Object, Object, Integer > local_distance,
			int[][] mt,
			@Nullable List< Tree > l1, @Nullable List< Tree > l2, @Nullable Map< Tree, Integer > dst, @Nullable Map< Tree, Integer > dit )
	{
		int n1 = forest1.getMyChildren().size();
		int n2 = forest2.getMyChildren().size();
		Map< Object, List< Tree > > dico1 = new LinkedHashMap<>();
		Map< Object, List< Tree > > dico2 = new LinkedHashMap<>();

		// TODO add implementation for the case local_distance == null
		if ( local_distance != null )
		{
			for ( Tree tree1 : forest1.getMyChildren() )
			{
				int unordered_equivalence_class_with_attribute =
						( int ) tree1.getMyAttributes().get( "unordered_equivalence_class_with_attribute" );
				if ( dico1.containsKey( unordered_equivalence_class_with_attribute ) )
				{
					List< Tree > trees = dico1.get( unordered_equivalence_class_with_attribute );
					trees.add( tree1 );
				}
				else
				{
					List< Tree > list = new ArrayList<>();
					list.add( tree1 );
					dico1.put( unordered_equivalence_class_with_attribute, list );
				}
			}

			for ( Tree tree2 : forest2.getMyChildren() )
			{
				int unordered_equivalence_class_with_attribute =
						( int ) tree2.getMyAttributes().get( "unordered_equivalence_class_with_attribute" );
				if ( dico2.containsKey( unordered_equivalence_class_with_attribute ) )
				{
					List< Tree > trees = dico2.get( unordered_equivalence_class_with_attribute );
					trees.add( tree2 );
				}
				else
				{
					List< Tree > list = new ArrayList<>();
					list.add( tree2 );
					dico2.put( unordered_equivalence_class_with_attribute, list );
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
			Tree s1 = null;
			for ( int j = 0; j < n2; j++ )
			{
				Object[] keys1 = dico1.keySet().toArray();
				Object key1 = keys1[ i ];
				List< Tree > trees1 = dico1.get( key1 );
				s1 = trees1.get( 0 );

				Object[] keys2 = dico2.keySet().toArray();
				Object key2 = keys2[ j ];
				List< Tree > trees2 = dico2.get( key2 );
				Tree s2 = trees2.get( 0 );

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
			List< Tree > trees2 = dico2.get( key2 );
			Tree s2 = trees2.get( 0 );

			DefaultWeightedEdge edge = graph.addEdge( n1 + n2 + 2, n1 + j + 1 );
			Integer weight = dit.get( s2 );
			graph.setEdgeWeight( edge, weight );
			capacities.put( edge, N2s - Math.min( N1s, N2s ) );

			edge = graph.addEdge( n1 + j + 1, n1 + n2 + 1 );
			graph.setEdgeWeight( edge, 0 );
			capacities.put( edge, N2.get( j ) );
		}

		int mincost = ( int ) JGraphtTools.maxFlowMinCost( graph, capacities, 0, n1 + n2 + 1 );

		return mincost;
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
	 * @param s
	 * @param costTreeToNone
	 * @return
	 */
	private static Pair< Map< Tree, Integer >, Map< Tree, Integer > > calcul_cost_sup_ins_forest_tree( Tree s,
			Map< UUID, Integer > costTreeToNone )
	{

		Map< Tree, Integer > df = new HashMap<>();
		Map< Tree, Integer > dt = new HashMap<>();

		if ( s.getMyChildren().isEmpty() )
		{
			df.put( s, 0 );
			if ( costTreeToNone == null )
			{
				dt.put( s, 1 );
			}
			else
			{
				dt.put( s, costTreeToNone.get( s.getMyId() ) );
			}
		}
		else
		{
			int v = 0;
			for ( Tree child : s.getMyChildren() )
			{
				Map< Tree, Integer > dfi;
				Map< Tree, Integer > dti;
				Pair< Map< Tree, Integer >, Map< Tree, Integer > > result = calcul_cost_sup_ins_forest_tree( child, costTreeToNone );
				dfi = result.getKey();
				dti = result.getValue();
				v += dti.get( child );
				df.putAll( dfi );
				dt.putAll( dti );
			}

			df.put( s, v );
			if ( costTreeToNone == null )
			{
				dt.put( s, v + 1 );
			}
			else
			{
				dt.put( s, v + costTreeToNone.get( s.getMyId() ) );
			}
		}
		return Pair.of( df, dt );
	}

	private static int post_order( Tree tree, String attribute, Map< Integer, Map< Object, List< Tree > > > graphDepthToClassifiedTrees )
	{
		int depth = 0;
		List< Integer > depths = new ArrayList<>();
		for ( Tree child : tree.getMyChildren() )
		{
			int d = post_order( child, attribute, graphDepthToClassifiedTrees );
			depths.add( d );
			depth = Collections.max( depths );
		}

		Map< Object, List< Tree > > attributeToTrees = graphDepthToClassifiedTrees.computeIfAbsent( depth, k -> new HashMap<>() );
		Object value = tree.getMyAttributes().get( attribute );
		List< Tree > treesWithSameAttribute = attributeToTrees.get( value );
		if ( treesWithSameAttribute == null )
		{
			treesWithSameAttribute = new ArrayList<>();
		}
		treesWithSameAttribute.add( tree );
		attributeToTrees.put( value, treesWithSameAttribute );
		graphDepthToClassifiedTrees.put( depth, attributeToTrees );
		tree.addAttributeToId( "height", depth );

		return depth + 1;
	}

	private static Map< UUID, Integer > getUnorderedEquivalenceClassWithAttribute( Tree tree, String attribute )
	{
		Map< UUID, Integer > dicClass = new HashMap<>();
		Map< Integer, Map< Object, List< Tree > > > graphDepthToClassifiedTrees = new LinkedHashMap<>();

		post_order( tree, attribute, graphDepthToClassifiedTrees );

		boolean ensureDifferentClassNumber = false;
		Tree tree1 = tree.getMyChildren().get( 0 );
		Tree tree2 = tree.getMyChildren().get( 1 );

		int classNumber = 0;
		for ( Map.Entry< Integer, Map< Object, List< Tree > > > graphDepth : graphDepthToClassifiedTrees.entrySet() )
		{
			for ( Map.Entry< Object, List< Tree > > treesWithSameAttributeAtGraphDepth : graphDepth.getValue().entrySet() )
			{
				for ( Tree t : treesWithSameAttributeAtGraphDepth.getValue() )
				{
					// NB: we do not add the class number to the tree itself
					if ( tree.getMyId().equals( t.getMyId() ) )
						continue;
					dicClass.put( t.getMyId(), classNumber );
					t.addAttributeToId( "unordered_equivalence_class_with_attribute", classNumber );
					if ( t.equals( tree1 ) || t.equals( tree2 ) && !ensureDifferentClassNumber )
					{
						classNumber++;
						ensureDifferentClassNumber = true;
					}
				}
				classNumber++;
			}
		}

		return dicClass;
	}
}
