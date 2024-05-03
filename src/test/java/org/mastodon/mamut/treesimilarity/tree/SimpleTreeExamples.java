/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.treesimilarity.tree;

public class SimpleTreeExamples
{
	public static Tree emptyTree()
	{
		return new SimpleTree( 0d );
	}

	/**
	 * <pre>
	 *                             node1(node_weight=20)
	 *                    ┌-─────────┴─────────────┐
	 *                    │                        │
	 *                  node2(node_weight=10)    node3(node_weight=30)
	 * </pre>
	 */
	public static Tree tree1()
	{
		SimpleTree node1 = new SimpleTree( 20d );

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
	public static Tree tree2()
	{
		SimpleTree node1 = new SimpleTree( 30d );

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
	public static Tree tree3()
	{
		SimpleTree node1 = new SimpleTree( 1d );

		SimpleTree node2 = addNode( 1d, node1 );
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
	public static Tree tree4()
	{
		SimpleTree node1 = new SimpleTree( 1d );

		SimpleTree node2 = addNode( 100d, node1 );
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
	public static Tree tree5()
	{
		SimpleTree node1 = new SimpleTree( 13d );

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
	public static Tree tree6()
	{
		SimpleTree node1 = new SimpleTree( 12d );

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
	public static Tree tree7()
	{
		SimpleTree node1 = new SimpleTree( 12d );

		addNode( 227d, node1 );
		SimpleTree node3 = addNode( 227d, node1 );

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
	public static SimpleTree tree8()
	{
		SimpleTree node1 = new SimpleTree( 3d );

		SimpleTree node2 = addNode( 8d, node1 );
		SimpleTree node3 = addNode( 8d, node1 );

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
	public static SimpleTree tree9()
	{
		SimpleTree node1 = new SimpleTree( 3d );

		SimpleTree node2 = addNode( 8d, node1 );
		SimpleTree node3 = addNode( 8d, node1 );

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
	 *       1              10
	 * </pre>
	 */
	public static SimpleTree tree10()
	{
		SimpleTree node1 = new SimpleTree( 1d );

		addNode( 1d, node1 );
		addNode( 10d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ─ 10 ─
	 * </pre>
	 */
	public static SimpleTree tree11()
	{
		return new SimpleTree( 10d );
	}

	/**
	 * <pre>
	 *       ┌────── 1000 ──────┐
	 *       │                  │
	 *     ┌─1 ───┐             1
	 *     │      │
	 *    100    200
	 * </pre>
	 */
	public static SimpleTree tree12()
	{
		SimpleTree node1 = new SimpleTree( 1000d );

		SimpleTree node2 = addNode( 1d, node1 );
		addNode( 1d, node1 );

		addNode( 100d, node2 );
		addNode( 200d, node2 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 1000 ──────┐
	 *       │                  │
	 *      100                200
	 * </pre>
	 */
	public static SimpleTree tree13()
	{
		SimpleTree node1 = new SimpleTree( 1_000d );

		addNode( 100d, node1 );
		addNode( 200d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 10_000 ───┐
	 *       │                 │
	 *       2                 4
	 * </pre>
	 */
	public static SimpleTree tree14()
	{
		SimpleTree node1 = new SimpleTree( 10_000d );

		addNode( 2d, node1 );
		addNode( 4d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌──────────────10_001──────────────┐
	 *       │                │                 │
	 *       3                5             1_000_000
	 * </pre>
	 */
	public static SimpleTree nonBinaryTree()
	{
		SimpleTree node1 = new SimpleTree( 10_001d );

		addNode( 3d, node1 );
		addNode( 5d, node1 );
		addNode( 1_000_000d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 1000 ──────┐
	 *       │                  │
	 *      100               ┌─1───┐
	 *                        │     │
	 *                       200   300
	 * </pre>
	 */
	public static SimpleTree tree15()
	{
		SimpleTree node1 = new SimpleTree( 1_000d );
		addNode( 100d, node1 );

		SimpleTree node2 = addNode( 1d, node1 );
		addNode( 200d, node2 );
		addNode( 300d, node2 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 1000 ──────┐
	 *       │                  │
	 *     ┌─2 ───┐            300
	 *     │      │
	 *    100    200
	 * </pre>
	 */
	public static SimpleTree tree16()
	{
		SimpleTree node1 = new SimpleTree( 1_000d );
		SimpleTree node2 = addNode( 2d, node1 );

		addNode( 100d, node2 );
		addNode( 200d, node2 );
		addNode( 300d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 1000 ───┐
	 *       │               │
	 *      200             300
	 * </pre>
	 */
	public static SimpleTree tree17()
	{
		SimpleTree node1 = new SimpleTree( 1_000d );

		addNode( 200d, node1 );
		addNode( 300d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *       ┌────── 100 ───┐
	 *       │              │
	 *      200            300
	 * </pre>
	 */
	public static SimpleTree tree18()
	{
		SimpleTree node1 = new SimpleTree( 100d );

		addNode( 200d, node1 );
		addNode( 300d, node1 );

		return node1;
	}

	/**
	 * <pre>
	 *               ┌────── 100 ──────┐
	 *               │                 │
	 *             ┌─1────────┐        2
	 *             │          │
	 *           ┌─3────┐     4
	 *           │      │
	 *         ┌─5──┐   6
	 *         │    │
	 *        200  300
	 * </pre>
	 */
	public static SimpleTree tree19()
	{
		SimpleTree node1 = new SimpleTree( 100d );

		SimpleTree node2 = addNode( 1d, node1 );
		addNode( 2d, node1 );

		SimpleTree node3 = addNode( 3d, node2 );
		addNode( 4d, node2 );

		SimpleTree node4 = addNode( 5d, node3 );
		addNode( 6d, node3 );

		addNode( 200d, node4 );
		addNode( 300d, node4 );

		return node1;
	}

	/**
	 * <pre>
	 *     	 ┌────── 50 ───────┐
	 *     	 │                 │
	 *     	 40           ┌─── 20 ─────┐
	 *     	              │            │
	 *                   200           80
	 * </pre>
	 */
	public static SimpleTree tree20()
	{
		SimpleTree node1 = new SimpleTree( 50d );

		SimpleTree node2 = addNode( 40d, node1 );
		SimpleTree node3 = addNode( 20d, node1 );
		SimpleTree node4 = addNode( 200d, node3 );
		SimpleTree node5 = addNode( 80d, node3 );

		return node1;
	}

	/**
	 * <pre>
	 *     	 ┌────── 50 ───────┐
	 *     	 │                 │
	 *      40             ┌─ 20 ──┐
	 *                     │       │
	 *                    30      80
	 * </pre>
	 */
	public static SimpleTree tree21()
	{
		SimpleTree node1 = new SimpleTree( 50d );

		SimpleTree node2 = addNode( 40d, node1 );
		SimpleTree node3 = addNode( 20d, node1 );
		SimpleTree node4 = addNode( 30d, node3 );
		SimpleTree node5 = addNode( 80d, node3 );

		return node1;
	}

	/**
	 * <pre>
	 *     	 ┌────── 60 ───────┐
	 *     	 │                 │
	 *      50             ┌─ 40 ──┐
	 *                     │       │
	 *                    80      100
	 * </pre>
	 */
	public static SimpleTree tree22()
	{
		SimpleTree node1 = new SimpleTree( 60d );

		SimpleTree node2 = addNode( 50d, node1 );
		SimpleTree node3 = addNode( 40d, node1 );
		SimpleTree node4 = addNode( 80d, node3 );
		SimpleTree node5 = addNode( 100d, node3 );

		return node1;
	}

	/**
	 * <pre>
	 *     	 ┌────── 60 ───────┐
	 *     	 │                 │
	 *    ┌─ 50 ──┐           40
	 *    │       │
	 *   80      100
	 * </pre>
	 */
	public static SimpleTree tree23()
	{
		SimpleTree node1 = new SimpleTree( 60d );

		SimpleTree node2 = addNode( 50d, node1 );
		SimpleTree node3 = addNode( 40d, node1 );
		SimpleTree node4 = addNode( 80d, node2 );
		SimpleTree node5 = addNode( 100d, node2 );

		return node1;
	}

	public static SimpleTree tree1a111()
	{
		SimpleTree node1 = new SimpleTree( 72d );

		addNode( 349d, node1 );
		SimpleTree node3 = addNode( 61d, node1 );
		addNode( 287d, node3 );
		SimpleTree node5 = addNode( 115d, node3 );
		SimpleTree node6 = addNode( 79d, node5 );
		addNode( 171d, node5 );
		SimpleTree node8 = addNode( 90d, node6 );
		addNode( 91d, node6 );
		addNode( 0d, node8 );
		addNode( 0d, node8 );

		return node1;
	}

	public static SimpleTree tree2c2()
	{
		SimpleTree node1 = new SimpleTree( 34d );

		SimpleTree node2 = addNode( 55d, node1 );
		SimpleTree node3 = addNode( 118d, node1 );
		SimpleTree node4 = addNode( 51d, node2 );
		SimpleTree node5 = addNode( 159d, node2 );
		addNode( 268d, node3 );
		addNode( 268d, node3 );
		SimpleTree node8 = addNode( 49d, node4 );
		addNode( 279d, node4 );
		addNode( 171d, node5 );
		addNode( 171d, node5 );
		addNode( 229d, node8 );
		SimpleTree node13 = addNode( 46d, node8 );
		SimpleTree node14 = addNode( 50d, node13 );
		SimpleTree node15 = addNode( 140d, node13 );
		SimpleTree node16 = addNode( 56d, node14 );
		addNode( 131d, node14 );
		addNode( 41d, node15 );
		addNode( 41d, node15 );
		addNode( 74d, node16 );
		addNode( 74d, node16 );

		return node1;
	}

	private static SimpleTree addNode( double attribute, SimpleTree parentTree )
	{
		SimpleTree node = new SimpleTree( attribute );
		parentTree.addChild( node );
		return node;
	}
}
