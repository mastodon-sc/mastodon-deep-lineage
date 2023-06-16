package org.mastodon.mamut.treesimilarity;

import java.util.ArrayList;
import java.util.List;

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
			throw new IllegalArgumentException( "Given tree must not be null" );
		List< Tree< T > > list = new ArrayList<>();
		list.add( tree );
		for ( Tree< T > child : tree.getChildren() )
		{
			list.addAll( listOfSubtrees( child ) );
		}
		return list;
	}

	/**
	 * Gets the number of descendant subtrees of this {@link Tree}, including itself.
	 * @return the number
	 */
	public static < T > int size( final Tree< T > tree )
	{
		return listOfSubtrees( tree ).size();
	}
}
