package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.VCoord;

import java.awt.Color;
import java.awt.Graphics2D;

public class CustomizedClusterComponent extends ClusterComponent
{
	private final Color color;

	public CustomizedClusterComponent( final Cluster cluster, final boolean printName, final VCoord initPoint, final Color color )
	{
		super( cluster, printName, initPoint );
		super.getChildren();
		this.color = color;
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
