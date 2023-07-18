package org.mastodon.mamut.treesimilarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.mamut.treesimilarity.tree.Tree;

abstract class TreeMatching< T >
{
	public static < T > TreeMatching< T > empty( double cost )
	{
		return new EmptyTreeMatching<>( cost );
	}

	public static < T > TreeMatching< T > singleton( double cost, Tree< T > tree1, Tree< T > tree2 )
	{
		return new SingletonTreeMatching<>( tree1, tree2, cost );
	}

	@SafeVarargs
	public static < T > TreeMatching< T > compose( TreeMatching< T >... children )
	{
		return compose( Arrays.asList( children ) );
	}

	public static < T > TreeMatching< T > compose( List< TreeMatching< T > > children )
	{
		return new ComposedTreeMatching<>( children );
	}

	private final double cost;

	protected TreeMatching( double cost )
	{
		this.cost = cost;
	}

	public double getCost()
	{
		return cost;
	}

	abstract protected void writeToMap( Map< Tree< T >, Tree< T > > map );

	public Map< Tree< T >, Tree< T > > asMap()
	{
		final Map< Tree< T >, Tree< T > > map = new HashMap<>();
		writeToMap( map );
		return map;
	}

	static class EmptyTreeMatching< T > extends TreeMatching< T >
	{
		private EmptyTreeMatching( double cost )
		{
			super( cost );
		}

		@Override
		public void writeToMap( Map< Tree< T >, Tree< T > > map )
		{
			// do nothing
		}
	}

	static class SingletonTreeMatching< T > extends TreeMatching< T >
	{
		private final Tree< T > tree1;

		private final Tree< T > tree2;

		private SingletonTreeMatching( Tree< T > tree1, Tree< T > tree2, double cost )
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

	private static class ComposedTreeMatching< T > extends TreeMatching< T >
	{
		private final List< TreeMatching< T > > children;

		private ComposedTreeMatching( List< TreeMatching< T > > children )
		{
			super( children.stream().mapToDouble( TreeMatching::getCost ).sum() );
			this.children = children;
		}

		@Override
		public void writeToMap( Map< Tree< T >, Tree< T > > map )
		{
			children.forEach( child -> child.writeToMap( map ) );
		}
	}
}
