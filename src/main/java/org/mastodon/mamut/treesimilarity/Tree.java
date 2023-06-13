package org.mastodon.mamut.treesimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree< T >
{

	private final List< Tree< T > > children;

	private final Map< String, T > attributes;

	public Tree()
	{
		children = new ArrayList<>();
		attributes = new HashMap<>();
	}

	/**
	 * Get the children of the tree.
	 *
	 * @return The list of child Tree objects.
	 */
	public List< Tree< T > > getChildren()
	{
		return children;
	}

	/**
	 * Get the attributes of the tree.
	 *
	 * @return The map of attributes.
	 */
	public Map< String, T > getAttributes()
	{
		return attributes;
	}

	/**
	 * Returns the list of subtrees (as Tree class objects) of the tree and all of its descendants.
	 *
	 * @return The list of subtrees.
	 */
	public List< Tree< T > > listOfSubtrees()
	{
		List< Tree< T > > list = new ArrayList<>();
		list.add( this );
		for ( Tree< T > child : children )
		{
			list.addAll( child.listOfSubtrees() );
		}
		return list;
	}

	/**
	 * Add an attribute to a node in the tree.
	 *
	 * @param attributeName The name of the new attribute.
	 * @param attributeValue The value of the new attribute.
	 */
	public void addAttribute( String attributeName, T attributeValue )
	{
		attributes.put( attributeName, attributeValue );
	}


	/**
	 * Add the given tree as a child of the main tree
	 * @param subtree the tree to add as a child
	 */
	public void addSubtree( Tree< T > subtree )
	{
		this.children.add( subtree );
	}
}
