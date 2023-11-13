/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.VCoord;
import org.mastodon.mamut.clustering.util.Classification;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Set;

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

	public < T > CustomizedClusterComponent(
			final Cluster cluster, final Set< Classification.ObjectClassification< T > > objectClassifications
	)
	{
		this( cluster, cluster.isLeaf(), new VCoord( 0, 1d / 2d ), 1d, Color.BLACK, objectClassifications );
	}

	/**
	 * Creates a new {@link CustomizedClusterComponent} component.
	 *
	 * @param cluster the cluster to be represented by this component.
	 * @param printName whether the name of the cluster should be printed.
	 * @param splitPoint the starting point of this component.
	 * <ul>
	 *      <li>The x coordinate represents the distance of this split point in relation to the max distance of the dendrogram, i.e. the x-coordinate should be equivalent to <i>total distance of dendrogram - distance of the {@code cluster} represented by this component</i> </li>
	 *      <li>The y coordinate is between 0 and 1. A value of zero means the split point is at the very top of the dendrogram, a value of 1 means the split point is at the very bottom of the dendrogram</li>
	 * </ul>
	 * @param clusterHeight the virtual height of the cluster component.
	 * <ul>
	 *     <li>This has to be a value larger than 0 and less or equal to 1.</li>
	 *     <li>The value 1 means that the cluster component is as high as the dendrogram, which is the case for the root component.</li>
	 *     <li>Any value between 0 and 1 should represent the share between the number of leaves this components represents and the total number of leaves in the dendrogram</li>
	 * </ul>
	 *
	 * @param color the {@link Color} used to draw this component. May be null. May be overwritten, if the given {@code cluster} or one of its ancestors is contained in {@code coloredClusters} parameter.
	 */
	private < T > CustomizedClusterComponent(
			final Cluster cluster, final boolean printName, final VCoord splitPoint, final double clusterHeight, final Color color,
			final Set< Classification.ObjectClassification< T > > objectClassifications
	)
	{
		super( cluster, printName, splitPoint );
		getChildren();
		this.color = getClusterColor( cluster, color, objectClassifications );
		init( cluster, splitPoint, clusterHeight, objectClassifications );
	}

	private static < T > Color getClusterColor(
			final Cluster cluster, final Color color, final Set< Classification.ObjectClassification< T > > objectClassifications
	)
	{
		return objectClassifications.stream().filter( objectClassification -> objectClassification.getCluster().equals( cluster ) )
				.findFirst().map( coloredCluster -> new Color( coloredCluster.getColor() ) ).orElse( color );
	}

	private < T > void init(
			final Cluster cluster, final VCoord splitPoint, final double clusterHeight,
			final Set< Classification.ObjectClassification< T > > objectClassifications
	)
	{
		double leafHeight = clusterHeight / cluster.countLeafs();
		double yChild = splitPoint.getY() - ( clusterHeight / 2 );
		double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		for ( Cluster child : cluster.getChildren() )
		{
			int childLeafCount = child.countLeafs();
			double childHeight = childLeafCount * leafHeight;
			double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
			VCoord childInitCoord = new VCoord( splitPoint.getX() + ( distance - childDistance ), yChild + childHeight / 2.0 );
			yChild += childHeight;

			CustomizedClusterComponent childComponent =
					new CustomizedClusterComponent( child, child.isLeaf(), childInitCoord, childHeight, this.color, objectClassifications );
			childComponent.setLinkPoint( splitPoint );
			getChildren().add( childComponent );
		}
		setLinkPoint( splitPoint );
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
