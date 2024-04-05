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

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BranchSpotTreeTest
{

	@Test
	void testGetChildren()
	{
		assertEquals( 2, BranchSpotTreeExamples.tree1().getChildren().size() );
		assertEquals( 2, BranchSpotTreeExamples.tree2().getChildren().size() );
	}

	@Test
	void testGetAttribute()
	{
		assertEquals( 20d, BranchSpotTreeExamples.tree1().getAttribute(), 0d );
		assertEquals( 30d, BranchSpotTreeExamples.tree2().getAttribute(), 0d );
	}

	@Test
	void testIsLeaf()
	{
		Tree< Double > tree1 = BranchSpotTreeExamples.tree1();
		assertFalse( tree1.isLeaf() );
		Iterator< Tree< Double > > iterator1 = tree1.getChildren().iterator();
		assertTrue( iterator1.next().isLeaf() );
		assertTrue( iterator1.next().isLeaf() );

		Tree< Double > tree2 = BranchSpotTreeExamples.tree2();
		assertFalse( tree2.isLeaf() );
		Iterator< Tree< Double > > iterator2 = tree1.getChildren().iterator();
		assertTrue( iterator2.next().isLeaf() );
		assertTrue( iterator2.next().isLeaf() );
	}

	@Test
	void testGetBranchSpot()
	{
		assertEquals( 0d, BranchSpotTreeExamples.emptyTree().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 19d, BranchSpotTreeExamples.tree1().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 29d, BranchSpotTreeExamples.tree2().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 0d, BranchSpotTreeExamples.tree3().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 0d, BranchSpotTreeExamples.tree4().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 12d, BranchSpotTreeExamples.tree5().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 11d, BranchSpotTreeExamples.tree6().getBranchSpot().getTimepoint(), 0d );
		assertEquals( 11d, BranchSpotTreeExamples.tree7().getBranchSpot().getTimepoint(), 0d );
	}

	@Test
	void testToString()
	{
		ExampleGraph2 example = new ExampleGraph2();
		BranchSpotTree tree = new BranchSpotTree( example.branchSpotB, 20 );
		// Note: spot3 is the first spot of branchSpotB.
		assertEquals( example.spot3.getLabel(), tree.toString() );
	}

	@Test
	@SuppressWarnings("all")
	void testBranchSpot()
	{
		ExampleGraph2 example = new ExampleGraph2();
		// NB: 2 is not a timepoint of branchSpotB, it only starts at 4.
		assertThrows( IllegalArgumentException.class, () -> new BranchSpotTree( example.branchSpotB, 2 ) );
		assertThrows( IllegalArgumentException.class, () -> new BranchSpotTree( null, 0 ) );
	}
}
