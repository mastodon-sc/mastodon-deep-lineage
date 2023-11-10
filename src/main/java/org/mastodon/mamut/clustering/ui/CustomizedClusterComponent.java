package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.VCoord;
import org.mastodon.mamut.clustering.util.Classification;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

/**
 * This class extends the class {@link ClusterComponent} from the hierarchical clustering library.
 * @see <a href="https://github.com/lbehnke/hierarchical-clustering-java">hierarchical-clustering-java</a><p>
 * It is overriden because the implementation of the super class {@link ClusterComponent} does not allow to change the color of cluster components.
 * The class {@link ClusterComponent} is not designed to be extended. Therefore, this class has to duplicate some code from the super class.<p>
 *
 * This class can represent any subtree of a dendrogram. It is used by {@link DendrogramPanel} to draw the dendrogram.
 * It can be drawn with a different color than the rest of the dendrogram<p>
 *
 * The differenes between CustomizedClusterComponent and ClusterComponent are:
 * <ul>
 *     <li>{@link CustomizedClusterComponent} can be drawn with a different color than the rest of the dendrogram</li>
 *     <li>The initialization of sub components is directly part of the constructor of this class</li>
 * </ul>
 *
 * @author Stefan Hahmann
 */
public class CustomizedClusterComponent extends ClusterComponent
{
	private final Color color;

	public CustomizedClusterComponent(
			final Cluster cluster, final boolean printName, final VCoord initPoint, final double clusterHeight, final Color color,
			final Classification< ? > classification
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

	private void init( final Cluster cluster, final VCoord initPoint, final double clusterHeight, final Classification< ? > classification )
	{
		Map< String, ? > leafMapping = classification.getLeafMapping();
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

			CustomizedClusterComponent childComponent =
					new CustomizedClusterComponent( child, child.isLeaf(), childInitCoord, childHeight, this.color, classification );
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
