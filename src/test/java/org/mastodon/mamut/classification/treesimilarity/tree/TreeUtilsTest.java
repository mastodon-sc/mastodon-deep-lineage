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
package org.mastodon.mamut.classification.treesimilarity.tree;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeUtilsTest
{

	@Test
	void testGetAllChildren()
	{
		Tree< Double > emptyTree = SimpleTreeExamples.emptyTree();

		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		List< Tree< Double > > subtrees1 = new ArrayList<>();
		subtrees1.add( tree1 );
		subtrees1.addAll( tree1.getChildren() );

		assertEquals( Collections.singletonList( emptyTree ), TreeUtils.getAllChildren( emptyTree ) );
		assertEquals( subtrees1, TreeUtils.getAllChildren( tree1 ) );
	}

	@Test
	void testSize()
	{
		assertEquals( 0, TreeUtils.size( null ) );
		assertEquals( 1, TreeUtils.size( SimpleTreeExamples.emptyTree() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree1() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree2() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeExamples.tree3() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeExamples.tree4() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree5() ) );
		assertEquals( 3, TreeUtils.size( SimpleTreeExamples.tree6() ) );
		assertEquals( 5, TreeUtils.size( SimpleTreeExamples.tree7() ) );
	}

	@Test
	void testPrintTree()
	{
		String className = SimpleTree.class.getSimpleName();
		String expected = className + "< Double > node0 = new " + className + "<>( 3.0 );" + System.lineSeparator()
				+ className + "< Double > node00 = addNode( 8.0, node0 );" + System.lineSeparator()
				+ "addNode( 4.0, node00 );" + System.lineSeparator()
				+ "addNode( 4.0, node00 );" + System.lineSeparator()
				+ className + "< Double > node01 = addNode( 8.0, node0 );" + System.lineSeparator()
				+ "addNode( 1.0, node01 );" + System.lineSeparator()
				+ "addNode( 2.0, node01 );" + System.lineSeparator()
				+ "return node0;";
		assertEquals( expected, TreeUtils.printTree( SimpleTreeExamples.tree9() ) );
	}
}
