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

import java.util.ArrayList;
import java.util.Collection;
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
	 * Gets the number of descendant subtrees of this {@link Tree}, including itself.
	 * @return the number
	 */
	public static < T > int size( final Tree< T > tree )
	{
		if ( tree == null )
			return 0;
		return getAllChildren( tree ).size();
	}

	/**
	 * Returns a complete {@link List} of the attributes of all descendant subtrees of the given {@code Tree}, including the attribute of the given {@code Tree} itself.
	 * @param tree the tree
	 * @param <T> the type of the tree
	 * @return the list of attributes
	 */
	public static < T > List< T > getAllAttributes( final Tree< T > tree )
	{
		List< T > attributes = new ArrayList<>();
		getAllChildren( tree ).forEach( subtree -> attributes.add( subtree.getAttribute() ) );
		return attributes;
	}

	/**
	 * Recursively collects all children of the given node, including the node itself.
	 *
	 * @param node The root node.
	 * @param <T>  The type of the node.
	 * @return A list of all children nodes.
	 */
	public static < T extends Node< T > > List< T > getAllChildren( T node )
	{
		if ( node == null )
			return Collections.emptyList();
		List< T > result = new ArrayList<>();
		result.add( node );
		getAllChildrenRecursive( node, result );
		return result;
	}

	private static < T extends Node< T > > void getAllChildrenRecursive( T node, List< T > result )
	{
		Collection< T > children = node.getChildren();
		if ( children != null )
		{
			for ( T child : children )
			{
				result.add( child );
				getAllChildrenRecursive( child, result );
			}
		}
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
