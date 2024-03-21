/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.treesimilarity.util;

import java.util.HashMap;
import java.util.Map;

import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
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
public interface NodeMapping< T >
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
