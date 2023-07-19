package org.mastodon.mamut.treesimilarity;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.mamut.treesimilarity.tree.Tree;

/**
 * Helper class for {@link ZhangUnorderedTreeEditDistance}.
 * <p>
 * Used to represent a mapping between the nodes of two {@link Tree trees}
 * together with the associated costs / edit distance.
 *
 * @param <T> Attribute type for the trees.
 *
 * @see NodeMappings
 */
interface NodeMapping< T >
{

	/**
	 * @return The cost of this mapping.
	 */
	double getCost();

	/**
	 * This method is needed for an efficient implementation of the
	 * {@link #asMap()} method. It is not meant to be used directly,
	 * use {@link #asMap()} instead.
	 */
	void writeToMap( Map< Tree< T >, Tree< T > > map );

	/**
	 * @return The mapping as a {@link Map} from nodes of the first tree to
	 * 	   nodes of the second tree.
	 */
	default Map< Tree< T >, Tree< T > > asMap()
	{
		final Map< Tree< T >, Tree< T > > map = new HashMap<>();
		writeToMap( map );
		return map;
	}
}
