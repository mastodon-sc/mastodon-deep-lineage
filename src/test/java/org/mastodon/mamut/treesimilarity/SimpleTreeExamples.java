package org.mastodon.mamut.treesimilarity;

public class SimpleTreeExamples
{
	static Tree< Number > emptyTree()
	{
		return new SimpleTree<>( 0d );
	}

	/**
	 * <pre>
	 *        					   node1(node_weight=20)
	 * 		              ┌-─────────┴─────────────┐
	 * 		              │                        │
	 * 		            node2(node_weight=10)    node3(node_weight=30)
	 * </pre>
	 */
	static Tree< Number > tree1()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 20d );

		SimpleTree< Number > node2 = new SimpleTree<>( 10d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 30d );
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
	static Tree< Number > tree2()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 30d );

		SimpleTree< Number > node2 = new SimpleTree<>( 10d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 20d );
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
	static Tree< Number > tree3()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 1d );

		SimpleTree< Number > node2 = new SimpleTree<>( 1d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 1d );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>( 1d );
		node2.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>( 100d );
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
	static Tree< Number > tree4()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 1d );

		SimpleTree< Number > node2 = new SimpleTree<>( 100d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 1d );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>( 1d );
		node2.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>( 1d );
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
	static Tree< Number > tree5()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 13d );

		SimpleTree< Number > node2 = new SimpleTree<>( 203d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 203d );
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
	static Tree< Number > tree6()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 12d );

		SimpleTree< Number > node2 = new SimpleTree<>( 227d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 227d );
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
	static Tree< Number > tree7()
	{
		SimpleTree< Number > node1 = new SimpleTree<>( 12d );

		SimpleTree< Number > node2 = new SimpleTree<>( 227d );
		node1.addSubtree( node2 );

		SimpleTree< Number > node3 = new SimpleTree<>( 227d );
		node1.addSubtree( node3 );

		SimpleTree< Number > node4 = new SimpleTree<>( 10d );
		node3.addSubtree( node4 );

		SimpleTree< Number > node5 = new SimpleTree<>( 10d );
		node3.addSubtree( node5 );

		return node1;
	}
}
