package org.mastodon.mamut.treesimilarity.tree;

import java.util.Collection;

public interface Tree< T >
{
	/**
	 * Get the children of this {@link Tree}.
	 *
	 * @return The list of child {@link Tree} objects.
	 */
	Collection< Tree< T > > getChildren();

	/**
	 * Get the attribute of this {@link Tree}.
	 *
	 * @return the attribute.
	 */
	T getAttribute();

	/**
	 * Returns {@code true}, if this {@link Tree} is a leaf, i.e. has no children.
	 * @return {@code true} if this {@link Tree} is a leaf, {@code false} otherwise.
	 */
	default boolean isLeaf()
	{
		return getChildren().isEmpty();
	}
}
