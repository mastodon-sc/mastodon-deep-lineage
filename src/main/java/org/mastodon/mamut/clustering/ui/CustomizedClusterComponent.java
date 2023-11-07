package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.VCoord;
import org.mastodon.mamut.clustering.util.Classification;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

/**
 * Class that extends {@link ClusterComponent} to customize the color of the dendrogram part represented by it.
 *
 * @author Stefan Hahmann
 */
public class CustomizedClusterComponent< T > extends ClusterComponent
{
	private final Color color;

	public CustomizedClusterComponent(
			final Cluster cluster, final boolean printName, final VCoord initPoint, final double clusterHeight, final Color color,
			final Classification< T > classification
	)
	{
		super( cluster, printName, initPoint );
		getChildren();
		Map< Cluster, Integer > clusterColors = classification.getClusterColors();
		if ( clusterColors != null && ( clusterColors.containsKey( cluster ) ) )
			this.color = new Color( clusterColors.get( cluster ) );
		else
			this.color = color;
		init( cluster, initPoint, clusterHeight, classification );
	}

	private void init( Cluster cluster, VCoord initPoint, double clusterHeight, Classification< T > classification )
	{
		Map< String, T > leafMapping = classification.getLeafMapping();
		if ( leafMapping != null && cluster.isLeaf() && leafMapping.containsKey( cluster.getName() ) )
			cluster.setName( leafMapping.get( cluster.getName() ).toString() );

		double leafHeight = clusterHeight / cluster.countLeafs();
		double yChild = initPoint.getY() - ( clusterHeight / 2 );
		double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		for ( Cluster child : cluster.getChildren() )
		{
			int childLeafCount = child.countLeafs();
			double childHeight = childLeafCount * leafHeight;
			double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
			VCoord childInitCoord = new VCoord( initPoint.getX() + ( distance - childDistance ), yChild + childHeight / 2.0 );
			yChild += childHeight;

			CustomizedClusterComponent< T > childComponent =
					new CustomizedClusterComponent<>( child, child.isLeaf(), childInitCoord, childHeight, this.color, classification );
			childComponent.setLinkPoint( initPoint );
			getChildren().add( childComponent );
		}
		setLinkPoint( initPoint );
	}

	@Override
	public void paint(
			final Graphics2D g2, final int xDisplayOffset, final int yDisplayOffset, final double xDisplayFactor,
			final double yDisplayFactor, final boolean decorated
	)
	{
		Color defaultColor = g2.getColor();
		try
		{
			g2.setColor( color );
			super.paint( g2, xDisplayOffset, yDisplayOffset, xDisplayFactor, yDisplayFactor, decorated );
		}
		finally
		{
			g2.setColor( defaultColor );
		}
	}
}
