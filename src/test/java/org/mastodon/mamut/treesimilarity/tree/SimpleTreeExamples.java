package org.mastodon.mamut.treesimilarity.tree;

public class SimpleTreeExamples
{
	public static Tree< Double > emptyTree()
	{
		return leaf( 0 );
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
		return node( 20, leaf( 10 ), leaf( 30 ) );
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
		return node( 30, leaf( 10 ), leaf( 20 ) );
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
		return node( 1, node( 1, leaf( 1 ), leaf( 100 ) ), leaf( 1 ) );
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
		return node( 1, node( 100, leaf( 1 ), leaf( 1 ) ), leaf( 1 ) );
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
		return node( 13, leaf( 203 ), leaf( 203 ) );
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
		return node( 12, leaf( 227 ), leaf( 227 ) );
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
		return node( 12, leaf( 227 ), node( 227, leaf( 10 ), leaf( 10 ) ) );
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
		return node( 3, node( 8, leaf( 5 ), leaf( 4 ) ), node( 8, leaf( 1 ), leaf( 2 ) ) );
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
		return node( 3, node( 8, leaf( 4 ), leaf( 4 ) ), node( 8, leaf( 1 ), leaf( 2 ) ) );
	}

	/**
	 * Creates a {@link SimpleTree} with the given attribute and children.
	 */
	private static SimpleTree< Double > node( double a, SimpleTree< Double > childA, SimpleTree< Double > childB )
	{
		SimpleTree< Double > node = new SimpleTree<>( a );
		node.addSubtree( childA );
		node.addSubtree( childB );
		return node;
	}

	/**
	 * Creates a {@link SimpleTree} with the given attribute and no children.
	 */
	private static SimpleTree< Double > leaf( double a )
	{
		return new SimpleTree<>( a );
	}
}
