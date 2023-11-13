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
import java.util.Collection;

public class SimpleTree< T > implements Tree< T >
{

	private final Collection< Tree< T > > children;

	private final T attribute;

	public SimpleTree( final T attribute )
	{
		children = new ArrayList<>();
		this.attribute = attribute;
	}

	/**
	 * Get the children of the tree.
	 *
	 * @return The list of child Tree objects.
	 */
	@Override
	public Collection< Tree< T > > getChildren()
	{
		return children;
	}

	/**
	 * Get the attributes of the tree.
	 *
	 * @return The map of attributes.
	 */
	@Override
	public T getAttribute()
	{
		return attribute;
	}

	/**
	 * Add the given tree as a child of the main tree
	 * @param subtree the tree to add as a child
	 */
	public void addChild( final SimpleTree< T > subtree )
	{
		this.children.add( subtree );
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "@" + hashCode();
	}
}
