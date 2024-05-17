package org.mastodon.mamut.classification.treesimilarity.tree;

import java.util.Collection;

public interface Node< T >
{
	/**
	 * Get the children of this {@link Tree}.
	 *
	 * @return The list of child {@link Tree} objects.
	 */
	Collection< T > getChildren();
}
