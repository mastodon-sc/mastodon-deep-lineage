package org.mastodon.mamut.treesimilarity.tree;

import java.util.HashSet;
import java.util.Set;

public class SimpleTree< T > implements Tree< T >
{

	private final Set< Tree< T > > children;

	private final T attribute;

	public SimpleTree( final T attribute )
	{
		children = new HashSet<>();
		this.attribute = attribute;
	}

	/**
	 * Get the children of the tree.
	 *
	 * @return The list of child Tree objects.
	 */
	@Override
	public Set< Tree< T > > getChildren()
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
	public void addSubtree( final SimpleTree< T > subtree )
	{
		this.children.add( subtree );
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "@" + hashCode();
	}
}
