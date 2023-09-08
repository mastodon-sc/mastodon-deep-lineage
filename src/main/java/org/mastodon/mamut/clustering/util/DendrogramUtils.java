package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;

import java.awt.Graphics2D;
import java.util.Map;

public class DendrogramUtils
{

	private DendrogramUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Counts the number of zeros after the decimal point of the given number before the first non-zero digit.<p>
	 * For numbers greater or equal to 1, 0 is returned.
	 * E.g.
	 * <ul>
	 *     <li>5.01 -> 0</li>
	 *     <li>0.1 -> 0</li>
	 *     <li>0.01 -> 1</li>
	 *     <li>0.001 -> 2</li>
	 *
	 * </ul>
	 * @param number the number to count the zeros after the decimal point
	 * @return the number of zeros after the decimal point of the given number before the first non-zero digit
	 */
	public static int countZerosAfterDecimalPoint( double number )
	{
		return ( int ) Math.max( 0, -Math.floor( Math.log10( Math.abs( number ) ) + 1 ) );
	}

	/**
	 * Maps the leave names of the given cluster according to the given object mapping.<p>
	 * @param cluster the cluster to map the leave names
	 * @param objectMapping the mapping from cluster names to objects
	 * @param <T> the type of the objects
	 */
	public static < T > void mapLeaveNames( final Cluster cluster, final Map< String, T > objectMapping )
	{
		if ( cluster == null )
			throw new IllegalArgumentException( "Given cluster must not be null" );
		if ( objectMapping == null )
			throw new IllegalArgumentException( "Given objectMapping must not be null" );
		if ( cluster.isLeaf() && objectMapping.containsKey( cluster.getName() ) )
			cluster.setName( objectMapping.get( cluster.getName() ).toString() );
		for ( Cluster child : cluster.getChildren() )
			mapLeaveNames( child, objectMapping );
	}

	/**
	 * Gets the maximum width of the leave names in screen coordinates of the given cluster.<p>
	 * @param g the graphics context to provide the font metrics
	 * @param cluster the cluster to get the maximum leave name width
	 * @return the maximum width of the leave names of the given cluster
	 */
	public static int getMaxLeafNameWidth( final Graphics2D g, final Cluster cluster )
	{
		int nameWidth = cluster.isLeaf() ? ( int ) g.getFontMetrics().getStringBounds( cluster.getName(), g ).getWidth() : 0;
		for ( Cluster child : cluster.getChildren() )
		{
			int childWidth = getMaxLeafNameWidth( g, child );
			if ( childWidth > nameWidth )
				nameWidth = childWidth;
		}
		return nameWidth;
	}
}
