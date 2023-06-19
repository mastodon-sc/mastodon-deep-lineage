package org.mastodon.mamut.treesimilarity.tree;

public class SimpleTreeExamples
{
	public static Tree< Double > emptyTree()
	{
		return new SimpleTree<>( 0d );
	}

	/**
	 * <pre>
	 *                             node1(node_weight=20)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  node2(node_weight=10)    node3(node_weight=30)
	 * </pre>
	 */
	public static Tree< Double > tree1()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 20d );

		SimpleTree< Double > node2 = new SimpleTree<>( 10d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 30d );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *                               node1(node_weight=30)
	 *                      ┌-─────────┴─────────────┐
	 *                      │                        │
	 *                    node2(node_weight=10)    node3(node_weight=20)
	 * </pre>
	 */
	public static Tree< Double > tree2()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 30d );

		SimpleTree< Double > node2 = new SimpleTree<>( 10d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 20d );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *                              node1(node_weight=1)
	 *                     ┌-─────────┴─────────────┐
	 *                     │                        │
	 *                   node2(node_weight=1)     node3(node_weight=1)
	 *          ┌-─────────┴─────────────┐
	 *          │                        │
	 *        node4(node_weight=1)     node5(node_weight=100)
	 * </pre>
	 */
	public static Tree< Double > tree3()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 1d );

		SimpleTree< Double > node2 = new SimpleTree<>( 1d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 1d );
		node1.addSubtree( node3 );

		SimpleTree< Double > node4 = new SimpleTree<>( 1d );
		node2.addSubtree( node4 );

		SimpleTree< Double > node5 = new SimpleTree<>( 100d );
		node2.addSubtree( node5 );

		return node1;
	}

	/**
	 * <pre>
	 *                            node1(node_weight=1)
	 *                   ┌-─────────┴─────────────┐
	 *                   │                        │
	 *                 node2(node_weight=100)   node3(node_weight=1)
	 *        ┌-─────────┴─────────────┐
	 *        │                        │
	 *      node4(node_weight=1)     node5(node_weight=1)
	 * </pre>
	 */
	public static Tree< Double > tree4()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 1d );

		SimpleTree< Double > node2 = new SimpleTree<>( 100d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 1d );
		node1.addSubtree( node3 );

		SimpleTree< Double > node4 = new SimpleTree<>( 1d );
		node2.addSubtree( node4 );

		SimpleTree< Double > node5 = new SimpleTree<>( 1d );
		node2.addSubtree( node5 );

		return node1;
	}

	/**
	 * <pre>
	 *                      node1(node_weight=13)
	 *             ┌-─────────┴─────────────┐
	 *             │                        │
	 *           node2(node_weight=203)     node3(node_weight=203)
	 * </pre>
	 */
	public static Tree< Double > tree5()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 13d );

		SimpleTree< Double > node2 = new SimpleTree<>( 203d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 203d );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *                      node1(node_weight=12)
	 *             ┌-─────────┴─────────────┐
	 *             │                        │
	 *           node2(node_weight=227)     node3(node_weight=227)
	 * </pre>
	 */
	public static Tree< Double > tree6()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 12d );

		SimpleTree< Double > node2 = new SimpleTree<>( 227d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 227d );
		node1.addSubtree( node3 );

		return node1;
	}

	/**
	 * <pre>
	 *                       node1(node_weight=12)
	 *              ┌-─────────┴─────────────┐
	 *              │                        │
	 *            node2(node_weight=227)   node3(node_weight=227)
	 *                            ┌-─────────┴─────────────┐
	 *                          node4(node_weight=10)    node5(node_weight=10)
	 * </pre>
	 */
	public static Tree< Double > tree7()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 12d );

		SimpleTree< Double > node2 = new SimpleTree<>( 227d );
		node1.addSubtree( node2 );

		SimpleTree< Double > node3 = new SimpleTree<>( 227d );
		node1.addSubtree( node3 );

		SimpleTree< Double > node4 = new SimpleTree<>( 10d );
		node3.addSubtree( node4 );

		SimpleTree< Double > node5 = new SimpleTree<>( 10d );
		node3.addSubtree( node5 );

		return node1;
	}
}
