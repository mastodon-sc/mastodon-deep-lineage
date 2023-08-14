package org.mastodon.mamut.treesimilarity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.mastodon.mamut.treesimilarity.tree.Tree;

/**
 * Utility class for {@link ZhangUnorderedTreeEditDistance} that provides
 * static factory methods for the easy creation of {@link NodeMapping}s.
 */
class NodeMappings
{
	private NodeMappings()
	{
		// prevent from instantiation
	}

	/**
	 * @return An empty {@link NodeMapping} with the specified cost.
	 * (Please note that the costs for an empty mapping are almost never
	 * zero!)
	 */
	public static < T > NodeMapping< T > empty( double cost )
	{
		return new EmptyNodeMapping<>( cost );
	}

	/**
	 * @return A {@link NodeMapping} that represents a singleton Map from
	 * tree1 to tree2 with the specified cost.
	 */
	public static < T > NodeMapping< T > singleton( double cost, Tree< T > tree1, Tree< T > tree2 )
	{
		return new SingletonNodeMapping<>( tree1, tree2, cost );
	}

	/**
	 * @return A {@link NodeMapping} that represents a composed that contains
	 * all the map entries of the given {@code children}. The costs of the
	 * composed mapping is the sum of the costs of the children.
	 */
	@SafeVarargs
	public static < T > NodeMapping< T > compose( NodeMapping< T >... children )
	{
		return compose( Arrays.asList( children ) );
	}

	/**
	 * @return A {@link NodeMapping} that represents a composed that contains
	 * all the map entries of the given {@code children}. The costs of the
	 * composed mapping is the sum of the costs of the children.
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
