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

		addNode( 10d, node1 );
		addNode( 30d, node1 );

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

		addNode( 10d, node1 );
		addNode( 20d, node1 );

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

		SimpleTree< Double > node2 = addNode( 1d, node1 );
		addNode( 1d, node1 );

		addNode( 1d, node2 );
		addNode( 100d, node2 );

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

		SimpleTree< Double > node2 = addNode( 100d, node1 );
		addNode( 1d, node1 );

		addNode( 1d, node2 );
		addNode( 1d, node2 );

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

		addNode( 203d, node1 );
		addNode( 203d, node1 );

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

		addNode( 227d, node1 );
		addNode( 227d, node1 );

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

		addNode( 227d, node1 );
		SimpleTree< Double > node3 = addNode( 227d, node1 );

		addNode( 10d, node3 );
		addNode( 10d, node3 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 3 ──────┐
	 *       │               │
	 *   ┌── 8 ──┐       ┌── 8 ──┐
	 *   │       │       │       │
	 *   5       4       1       2
	 * </pre>
	 */
	public static SimpleTree< Double > tree8()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 3d );

		SimpleTree< Double > node2 = addNode( 8d, node1 );
		SimpleTree< Double > node3 = addNode( 8d, node1 );

		addNode( 5d, node2 );
		addNode( 4d, node2 );

		addNode( 1d, node3 );
		addNode( 2d, node3 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 3 ──────┐
	 *       │               │
	 *   ┌── 8 ──┐       ┌── 8 ──┐
	 *   │       │       │       │
	 *   4       4       1       2
	 * </pre>
	 */
	public static SimpleTree< Double > tree9()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 3d );

		SimpleTree< Double > node2 = addNode( 8d, node1 );
		SimpleTree< Double > node3 = addNode( 8d, node1 );

		addNode( 4d, node2 );
		addNode( 4d, node2 );

		addNode( 1d, node3 );
		addNode( 2d, node3 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 1 ──────┐
	 *       │               │
	 *       1               2
	 * </pre>
	 */
	public static SimpleTree< Double > tree10()
	{
		SimpleTree< Double > node1 = new SimpleTree<>( 1d );

		addNode( 1d, node1 );
		addNode( 2d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ─ 2 ─
	 * </pre>
	 */
	public static SimpleTree< Double > tree11()
	{
		return new SimpleTree<>( 2d );
	}

	private static SimpleTree< Double > addNode( double attribute, SimpleTree< Double > parentTree )
	{
		SimpleTree< Double > node = new SimpleTree<>( attribute );
		parentTree.addSubtree( node );
		return node;
	}
}
