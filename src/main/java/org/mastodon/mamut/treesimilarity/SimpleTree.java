package org.mastodon.mamut.treesimilarity;

import java.util.HashSet;
import java.util.Set;

public class SimpleTree< T > implements Tree< T >
{

	private final Set< Tree< T > > children;

	private final T attribute;

	public SimpleTree( T attribute )
	{
		children = new HashSet<>();
		this.attribute = attribute;
	}

	/**
	 * Get the children of the tree.
	 *
	 * @return The list of child Tree objects.
	 */
	public Set< Tree< T > > getChildren()
	{
		return children;
	}

	/**
	 * Get the attributes of the tree.
	 *
	 * @return The map of attributes.
	 */
	public T getAttribute()
	{
		return attribute;
	}

	/**
	 * Add the given tree as a child of the main tree
	 * @param subtree the tree to add as a child
	 */
	public void addSubtree( SimpleTree< T > subtree )
	{
		this.children.add( subtree );
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "@" + hashCode();
	}
}
