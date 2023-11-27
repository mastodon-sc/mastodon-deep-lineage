/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeUtils
{
	private TreeUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Returns a complete list of all descendant subtrees of the given {@code Tree}, including itself.
	 *
	 * @return The list of subtrees.
	 */
	public static < T > List< Tree< T > > listOfSubtrees( final Tree< T > tree )
	{
		if ( tree == null )
			return Collections.emptyList();
		List< Tree< T > > list = new ArrayList<>();
		list.add( tree );
		for ( Tree< T > child : tree.getChildren() )
			list.addAll( listOfSubtrees( child ) );
		return list;
	}

	/**
	 * Gets the number of descendant subtrees of this {@link Tree}, including itself.
	 * @return the number
	 */
	public static < T > int size( final Tree< T > tree )
	{
		if ( tree == null )
			return 0;
		return listOfSubtrees( tree ).size();
	}

	/**
	 * Creates a String of the given tree as a Java code snippet that can be used to create the tree.
	 * @param tree The tree to print.
	 * @return the code snippet
	 * @param <T> the type of the tree
	 */
	public static < T > String printTree( final Tree< T > tree )
	{
		return printTree( tree, true, String.valueOf( 0 ), new StringBuilder() );
	}

	private static < T > String printTree( final Tree< T > tree, boolean isRoot, final String suffix, final StringBuilder output )
	{
		String treeName = "node" + suffix;
		String className = SimpleTree.class.getSimpleName();
		if ( isRoot )
			output.append( className ).append( "< Double > " ).append( treeName ).append( " = new " ).append( className ).append( "<>( " )
					.append( tree.getAttribute() )
					.append( " );" ).append( System.lineSeparator() );
		AtomicInteger childIndex = new AtomicInteger( 0 );
		tree.getChildren().forEach( child -> {
			String childSuffix = suffix + childIndex.getAndIncrement();
			String childName = "node" + childSuffix;
			String print = "addNode( " + child.getAttribute() + ", " + treeName + " );";
			if ( !child.isLeaf() )
				print = className + "< Double > " + childName + " = " + print;
			output.append( print ).append( System.lineSeparator() );
			printTree( child, false, childSuffix, output );
		} );
		if ( isRoot )
			output.append( "return " ).append( treeName ).append( ";" );

		return output.toString();
	}

}
