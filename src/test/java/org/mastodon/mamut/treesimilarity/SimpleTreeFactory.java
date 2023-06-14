package org.mastodon.mamut.treesimilarity;

public class SimpleTreeFactory
{
	static SimpleTree< Number > emptyTree()
	{
		return new SimpleTree<>( 0 );
	}

	/**
	 * <pre>
	 *        					   node1(node_weight=20)
	 * 		              ┌-─────────┴─────────────┐
	 * 		              │                        │
	 * 		            node2(node_weight=10)    node3(node_weight=30)
	 * </pre>
	 */
	static SimpleTree< Number > tree1()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 20 );

		SimpleTree< Number > node2 = new SimpleTree<>( 10 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 30 );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *    		  				     node1(node_weight=30)
	 * 		                ┌-─────────┴─────────────┐
	 * 		                │                        │
	 * 		              node2(node_weight=10)    node3(node_weight=20)
	 * </pre>
	 */
	static SimpleTree< Number > tree2()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 30 );

		SimpleTree< Number > node2 = new SimpleTree<>( 10 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 20 );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *                             node1(node_weight=1)
	 * 		               ┌-─────────┴─────────────┐
	 * 		               │                        │
	 * 		             node2(node_weight=1)     node3(node_weight=1)
	 * 		    ┌-─────────┴─────────────┐
	 * 		    │                        │
	 * 		  node4(node_weight=1)     node5(node_weight=100)
	 * </pre>
	 */
	static SimpleTree< Number > tree3()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 1 );

		SimpleTree< Number > node2 = new SimpleTree<>( 1 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 1 );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>( 1 );
		node2.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>( 100 );
		node2.addSubtree( node5 );

		return node1;
	}

	/**
	 * <pre>
	 *                            node1(node_weight=1)
	 * 		             ┌-─────────┴─────────────┐
	 * 		             │                        │
	 * 		           node2(node_weight=100)   node3(node_weight=1)
	 * 		  ┌-─────────┴─────────────┐
	 * 		  │                        │
	 * 		node4(node_weight=1)     node5(node_weight=1)
	 * </pre>
	 */
	static SimpleTree< Number > tree4()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 1 );

		SimpleTree< Number > node2 = new SimpleTree<>( 100 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 1 );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>( 1 );
		node2.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>( 1 );
		node2.addSubtree( node5 );

		return node1;
	}

	/**
	 * <pre>
	 *                      node1(node_weight=13)
	 * 		       ┌-─────────┴─────────────┐
	 * 		       │                        │
	 * 		     node2(node_weight=203)     node3(node_weight=203)
	 * </pre>
	 */
	static SimpleTree< Number > tree5()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 13 );

		SimpleTree< Number > node2 = new SimpleTree<>( 203 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 203 );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *                      node1(node_weight=12)
	 * 		       ┌-─────────┴─────────────┐
	 * 		       │                        │
	 * 		     node2(node_weight=227)     node3(node_weight=227)
	 * </pre>
	 */
	static SimpleTree< Number > tree6()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 12 );

		SimpleTree< Number > node2 = new SimpleTree<>( 227 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 227 );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 * 		                 node1(node_weight=12)
	 * 		        ┌-─────────┴─────────────┐
	 * 		        │                        │
	 * 		      node2(node_weight=227)   node3(node_weight=227)
	 * 		                      ┌-─────────┴─────────────┐
	 * 		                    node4(node_weight=10)    node5(node_weight=10)
	 * </pre>
	 */
	static SimpleTree< Number > tree7()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 12 );

		SimpleTree< Number > node2 = new SimpleTree<>( 227 );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 227 );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>( 10 );
		node3.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>( 10 );
		node3.addSubtree( node5 );

		return node1;
	}
}
