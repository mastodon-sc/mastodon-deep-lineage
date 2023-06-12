package org.mastodon.mamut.treesimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Tree
{

	private final UUID id;

	private final List< Tree > children;

	private final Map< String, Object > attributes;

	public Tree()
	{
		id = UUID.randomUUID();
		children = new ArrayList<>();
		attributes = new HashMap<>();
	}

	/**
	 * Get the unique id attributed to the tree.
	 *
	 * @return The unique id of the tree.
	 */
	public UUID getId()
	{
		return id;
	}

	/**
	 * Get the children of the tree.
	 *
	 * @return The list of child Tree objects.
	 */
	public List< Tree > getChildren()
	{
		return children;
	}

	/**
	 * Get the attributes of the tree.
	 *
	 * @return The map of attributes.
	 */
	public Map< String, Object > getAttributes()
	{
		return attributes;
	}

	/**
	 * Add an attribute to a node in the tree.
	 *
	 * @param attributeName The name of the new attribute.
	 * @param attributeValue The value of the new attribute.
	 */
	public void addAttribute( String attributeName, Number attributeValue )
	{
		attributes.put( attributeName, attributeValue );
	}

	/**
	 * Returns the list of subtrees (as Tree class objects) of the tree and all of its descendants.
	 *
	 * @return The list of subtrees.
	 */
	public List< Tree > listOfSubtrees()
	{
		List< Tree > list = new ArrayList<>();
		list.add( this );
		for ( Tree child : children )
		{
			list.addAll( child.listOfSubtrees() );
		}
		return list;
	}

	/**
	 * Add the given tree as a child of the main tree
	 * @param subtree the tree to add as a child
	 */
	public void addSubtree( Tree subtree )
	{
		this.children.add( subtree );
	}
}
