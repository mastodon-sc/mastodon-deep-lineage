/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.clustering.treesimilarity.tree;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleTreeTest
{

	@Test
	void testGetChildren()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		SimpleTree< Number > child1 = new SimpleTree<>( 1 );
		SimpleTree< Number > child2 = new SimpleTree<>( 2 );
		tree.addChild( child1 );
		tree.addChild( child2 );
		List< SimpleTree< Number > > expected = new ArrayList<>( Arrays.asList( child1, child2 ) );
		assertEquals( expected, tree.getChildren() );
	}

	@Test
	void testGetAttribute()
	{
		int attribute = 1;
		SimpleTree< Number > tree = new SimpleTree<>( attribute );
		assertEquals( attribute, tree.getAttribute() );
	}

	@Test
	void testAddSubtree()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		SimpleTree< Number > child1 = new SimpleTree<>( 1 );
		tree.addChild( child1 );
		assertEquals( child1, tree.getChildren().iterator().next() );
	}

	@Test
	void testTestToString()
	{
		SimpleTree< Number > tree = new SimpleTree<>( 0 );
		assertEquals( "SimpleTree@" + tree.hashCode(), tree.toString() );
	}
}
