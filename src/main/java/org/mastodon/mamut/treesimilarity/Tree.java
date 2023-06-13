package org.mastodon.mamut.treesimilarity;

import java.util.List;
import java.util.Set;

public interface Tree< T >
{
	/**
	 * Get the children of this {@link Tree}.
	 *
	 * @return The list of child {@link Tree} objects.
	 */
	Set< Tree< T > > getChildren();

	/**
	 * Get the attribute of this {@link Tree}.
	 *
	 * @return the attribute.
	 */
	T getAttribute();

	/**
	 * Returns a list of all descendant subtrees of this {@link Tree}, including itself.
	 *
	 * @return The list of subtrees.
	 */
	List< Tree< T > > listOfSubtrees();

	/**
	 * Returns {@code true}, if this {@link Tree} is a leaf, i.e. has no children.
	 * @return {@code true} if this {@link Tree} is a leaf, {@code false} otherwise.
	 */
	default boolean isLeaf()
	{
		return getChildren().isEmpty();
	}

	/**
	 * Gets the number of descendant subtrees of this {@link Tree}, including itself.
	 * @return the number
	 */
	default int size()
	{
		return listOfSubtrees().size();
	}
}
