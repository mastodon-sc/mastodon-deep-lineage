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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.mastodon.mamut.treesimilarity.ZhangUnorderedTreeEditDistance;
import org.mastodon.mamut.treesimilarity.tree.Tree;

/**
 * Utility class for {@link ZhangUnorderedTreeEditDistance} that provides
 * static factory methods for the easy creation of {@link NodeMapping}s.
 */
public class NodeMappings
{
	private NodeMappings()
	{
		// prevent from instantiation
	}

	/**
	 * @param cost the cost of the empty mapping.
	 * @param <T> The type of the tree nodes.
	 * @return An empty {@link NodeMapping} with the specified cost.
	 * (Please note that the costs for an empty mapping are almost never
	 * zero!)
	 */
	public static < T > NodeMapping< T > empty( double cost )
	{
		return new EmptyNodeMapping<>( cost );
	}

	/**
	 * @param cost the cost of the singleton mapping.
	 * @param tree1 the first tree.
	 * @param tree2 the second tree.
	 * @param <T> The type of the tree nodes.
	 * @return A {@link NodeMapping} that represents a singleton Map from
	 * tree1 to tree2 with the specified cost.
	 */
	public static < T > NodeMapping< T > singleton( double cost, Tree< T > tree1, Tree< T > tree2 )
	{
		return new SingletonNodeMapping<>( tree1, tree2, cost );
	}

	/**
	 * @return A {@link NodeMapping} that represents a composed mapping that
	 * contains all the map entries of the given {@code children}. The costs of
	 * the composed mapping is the sum of the costs of the children.
	 */
	@SafeVarargs
	public static < T > NodeMapping< T > compose( NodeMapping< T >... children )
	{
		return compose( Arrays.asList( children ) );
	}

	/**
	 * @return A {@link NodeMapping} that represents a composed mapping that
	 * contains all the map entries of the given {@code children}. The costs of
	 * the composed mapping is the sum of the costs of the children.
	 */
	public static < T > NodeMapping< T > compose( List< NodeMapping< T > > children )
	{
		return new ComposedNodeMapping<>( children );
	}

	private abstract static class AbstractNodeMapping< T > implements NodeMapping< T >
	{
		private final double cost;

		protected AbstractNodeMapping( double cost )
		{
			this.cost = cost;
		}

		@Override
		public double getCost()
		{
			return cost;
		}
	}

	private static class EmptyNodeMapping< T > extends AbstractNodeMapping< T >
	{
		private EmptyNodeMapping( double cost )
		{
			super( cost );
		}

		@Override
		public void writeToMap( Map< Tree< T >, Tree< T > > map )
		{
			// do nothing
		}
	}

	private static class SingletonNodeMapping< T > extends AbstractNodeMapping< T >
	{
		private final Tree< T > tree1;

		private final Tree< T > tree2;

		private SingletonNodeMapping( Tree< T > tree1, Tree< T > tree2, double cost )
		{
			super( cost );
			this.tree1 = tree1;
			this.tree2 = tree2;
		}

		@Override
		public void writeToMap( Map< Tree< T >, Tree< T > > map )
		{
			map.put( tree1, tree2 );
		}
	}

	private static class ComposedNodeMapping< T > extends AbstractNodeMapping< T >
	{
		private final List< NodeMapping< T > > children;

		private ComposedNodeMapping( List< NodeMapping< T > > children )
		{
			super( children.stream().mapToDouble( NodeMapping::getCost ).sum() );
			this.children = children;
		}

		@Override
		public void writeToMap( Map< Tree< T >, Tree< T > > map )
		{
			children.forEach( child -> child.writeToMap( map ) );
		}
	}
}
