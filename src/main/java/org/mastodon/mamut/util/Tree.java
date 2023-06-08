package org.mastodon.mamut.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Tree
{
	/**
	 * The main structure of the treex library.
	 */

	private static final AtomicInteger idCounter = new AtomicInteger( 0 );

	private final UUID myId;

	private final List< Tree > myChildren;

	private final Map< String, Object > myProperties;

	private final Map< String, Object > myAttributes;

	private Tree myParent;

	public Tree()
	{
		myId = UUID.randomUUID();
		myChildren = new ArrayList<>();
		myProperties = new HashMap<>();
		myAttributes = new HashMap<>();
		myParent = null;
	}

	/**
	 * Get the unique id attributed to the tree.
	 *
	 * @return The unique id of the tree.
	 */
	public UUID getMyId()
	{
		return myId;
	}

	/**
	 * Get the children of the tree.
	 *
	 * @return The list of child Tree objects.
	 */
	public List< Tree > getMyChildren()
	{
		return myChildren;
	}

	/**
	 * Get the properties of the tree.
	 *
	 * @return The map of properties.
	 */
	public Map< String, Object > getMyProperties()
	{
		return myProperties;
	}

	/**
	 * Get the attributes of the tree.
	 *
	 * @return The map of attributes.
	 */
	public Map< String, Object > getMyAttributes()
	{
		return myAttributes;
	}

	/**
	 * Get the parent tree.
	 *
	 * @return The parent Tree object.
	 */
	public Tree getMyParent()
	{
		return myParent;
	}

	/**
	 * Set the parent tree.
	 *
	 * @param parent The parent Tree object.
	 */
	public void setMyParent( Tree parent )
	{
		myParent = parent;
	}

	/**
	 * Add an attribute to a node in the tree.
	 *
	 * @param attributeName The name of the new attribute.
	 * @param attributeValue The value of the new attribute.
	 */
	public void addAttributeToId( String attributeName, Number attributeValue )
	{
		myAttributes.put( attributeName, attributeValue );
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
		for ( Tree child : myChildren )
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
		this.myChildren.add( subtree );
		subtree.setMyParent( this );
	}
}
