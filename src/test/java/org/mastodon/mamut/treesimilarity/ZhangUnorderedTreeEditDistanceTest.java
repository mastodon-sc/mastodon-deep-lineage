package org.mastodon.mamut.treesimilarity;

import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class ZhangUnorderedTreeEditDistanceTest
{

	private static final String ATTRIBUTE = "node_weight";

	@Test
	public void testZhangEditDistance()
	{
		BiFunction< Number, Number, Integer > costFunction = ( o1, o2 ) -> {
			if ( o2 == null )
				return ( Integer ) o1;
			else
				return Math.abs( ( Integer ) o1 - ( Integer ) o2 );
		};

		SimpleTree< Number > emptyTree = emptyTree();
		SimpleTree< Number > tree1 = tree1();
		SimpleTree< Number > tree2 = tree2();
		SimpleTree< Number > tree3 = tree3();
		SimpleTree< Number > tree4 = tree4();
		SimpleTree< Number > tree5 = tree5();
		SimpleTree< Number > tree6 = tree6();
		SimpleTree< Number > tree7 = tree7();

		// similarities with empty trees

		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( tree1, emptyTree, ATTRIBUTE, costFunction, false ) );
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree1, ATTRIBUTE, costFunction, false ) );
		// 60, because: 3 nodes with a total weight of 60 are added to the empty tree
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( tree2, emptyTree, ATTRIBUTE, costFunction, false ) );
		assertEquals( 60, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree2, ATTRIBUTE, costFunction, false ) );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( tree3, emptyTree, ATTRIBUTE, costFunction, false ) );
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree3, ATTRIBUTE, costFunction, false ) );
		// 104, because: 3 nodes with a total weight of 104 are added to the empty tree
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( tree4, emptyTree, ATTRIBUTE, costFunction, false ) );
		assertEquals( 104, ZhangUnorderedTreeEditDistance.distance( emptyTree, tree4, ATTRIBUTE, costFunction, false ) );

		// similarity of trees against other trees

		// 20, because: 2 nodes with a difference of 10 each need to be changed
		assertEquals( 20, ZhangUnorderedTreeEditDistance.distance( tree1, tree2, ATTRIBUTE, costFunction, false ) );
		assertEquals( 20, ZhangUnorderedTreeEditDistance.distance( tree2, tree1, ATTRIBUTE, costFunction, false ) );
		// 4, because:
		// 2 nodes are removed (weight 1 each), e.g. node 2 and node 3 in tree 3
		// 2 nodes are added (weight 1 each), e.g. node 4 and node 5 in tree 4
		assertEquals( 4, ZhangUnorderedTreeEditDistance.distance( tree3, tree4, ATTRIBUTE, costFunction, false ) );
		assertEquals( 4, ZhangUnorderedTreeEditDistance.distance( tree4, tree3, ATTRIBUTE, costFunction, false ) );

		// 49, because: one node has a difference of 1
		// two nodes have a difference of 24 each
		assertEquals( 49, ZhangUnorderedTreeEditDistance.distance( tree5, tree6, ATTRIBUTE, costFunction, false ) );
		assertEquals( 49, ZhangUnorderedTreeEditDistance.distance( tree6, tree5, ATTRIBUTE, costFunction, false ) );

		// 69, because:
		// one node has a difference of 1
		// two nodes have a difference of 24 each
		// two extra nodes are added with a weight of 10 each
		assertEquals( 69, ZhangUnorderedTreeEditDistance.distance( tree5, tree7, ATTRIBUTE, costFunction, false ) );
		assertEquals( 69, ZhangUnorderedTreeEditDistance.distance( tree7, tree5, ATTRIBUTE, costFunction, false ) );
	}

	private SimpleTree< Number > emptyTree()
	{
		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 0 );
		return node1;
	}

	private SimpleTree< Number > tree1()
	{
		//   					   node1(node_weight=20)
		//                ┌-─────────┴─────────────┐
		//                │                        │
		//              node2(node_weight=10)    node3(node_weight=30)
		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 20 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 10 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 30 );
		node1.addSubtree( node3 );

		return node1;
	}

	private SimpleTree< Number > tree2()
	{
		//  				       node1(node_weight=30)
		//                ┌-─────────┴─────────────┐
		//                │                        │
		//              node2(node_weight=10)    node3(node_weight=20)

		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 30 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 10 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 20 );
		node1.addSubtree( node3 );

		return node1;
	}

	private SimpleTree< Number > tree3()
	{
		//
		//                        node1(node_weight=1)
		//               ┌-─────────┴─────────────┐
		//               │                        │
		//             node2(node_weight=1)     node3(node_weight=1)
		//    ┌-─────────┴─────────────┐
		//    │                        │
		//  node4(node_weight=1)     node5(node_weight=100)

		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 1 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 1 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 1 );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>();
		node4.addAttribute( ATTRIBUTE, 1 );
		node2.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>();
		node5.addAttribute( ATTRIBUTE, 100 );
		node2.addSubtree( node5 );

		return node1;
	}

	private SimpleTree< Number > tree4()
	{
		//                       node1(node_weight=1)
		//              ┌-─────────┴─────────────┐
		//              │                        │
		//            node2(node_weight=100)   node3(node_weight=1)
		//   ┌-─────────┴─────────────┐
		//   │                        │
		// node4(node_weight=1)     node5(node_weight=1)

		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 1 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 100 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 1 );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>();
		node4.addAttribute( ATTRIBUTE, 1 );
		node2.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>();
		node5.addAttribute( ATTRIBUTE, 1 );
		node2.addSubtree( node5 );

		return node1;
	}

	private SimpleTree< Number > tree5()
	{
		//
		//                 node1(node_weight=13)
		//        ┌-─────────┴─────────────┐
		//        │                        │
		//      node2(node_weight=203)     node3(node_weight=203)

		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 13 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 203 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 203 );
		node1.addSubtree( node3 );

		return node1;
	}

	private SimpleTree< Number > tree6()
	{
		//                 node1(node_weight=12)
		//        ┌-─────────┴─────────────┐
		//        │                        │
		//      node2(node_weight=227)     node3(node_weight=227)

		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 12 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 227 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 227 );
		node1.addSubtree( node3 );

		return node1;
	}

	private SimpleTree< Number > tree7()
	{
		//
		//                 node1(node_weight=12)
		//        ┌-─────────┴─────────────┐
		//        │                        │
		//      node2(node_weight=227)   node3(node_weight=227)
		//                      ┌-─────────┴─────────────┐
		//                    node4(node_weight=10)    node5(node_weight=10)

		SimpleTree< Number > node1 = new SimpleTree<>();
		node1.addAttribute( ATTRIBUTE, 12 );

		SimpleTree< Number > node2 = new SimpleTree<>();
		node2.addAttribute( ATTRIBUTE, 227 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>();
		node3.addAttribute( ATTRIBUTE, 227 );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>();
		node4.addAttribute( ATTRIBUTE, 10 );
		node3.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>();
		node5.addAttribute( ATTRIBUTE, 10 );
		node3.addSubtree( node5 );

		return node1;
	}
}
