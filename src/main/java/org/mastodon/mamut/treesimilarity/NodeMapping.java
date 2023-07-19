package org.mastodon.mamut.treesimilarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.mamut.treesimilarity.tree.Tree;

abstract class NodeMapping< T >
{
	public static < T > NodeMapping< T > empty( double cost )
	{
		return new EmptyNodeMapping<>( cost );
	}

	public static < T > NodeMapping< T > singleton( double cost, Tree< T > tree1, Tree< T > tree2 )
	{
		return new SingletonNodeMapping<>( tree1, tree2, cost );
	}

	@SafeVarargs
	public static < T > NodeMapping< T > compose( NodeMapping< T >... children )
	{
		return compose( Arrays.asList( children ) );
	}

	public static < T > NodeMapping< T > compose( List< NodeMapping< T > > children )
	{
		return new ComposedNodeMapping<>( children );
	}

	private final double cost;

	protected NodeMapping( double cost )
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

	static class EmptyNodeMapping< T > extends NodeMapping< T >
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

	static class SingletonNodeMapping< T > extends NodeMapping< T >
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

	private static class ComposedNodeMapping< T > extends NodeMapping< T >
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
