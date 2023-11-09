package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.VCoord;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomizedClusterComponentTest
{

	@Test
	public void testCustomizedClusterComponent()
	{
		Classification< String > classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(),
						3
				);
		Cluster cluster = classification.getAlgorithmResult();
		assertNotNull( cluster );
		CustomizedClusterComponent< String > customizedClusterComponent =
				new CustomizedClusterComponent<>( cluster, cluster.isLeaf(), new VCoord( 200, 200 ), 0.5d, Color.BLUE, classification );

		int width = 400;
		int height = 400;
		Image image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics graphics = image.getGraphics();
		Color defaultColor = Color.WHITE;
		graphics.setColor( defaultColor );

		customizedClusterComponent.paint( ( Graphics2D ) graphics, 0, 0, 1d, 1d, false );
		// this tests only if the paint method resets the color to default and runs without exceptions, not if the dendrogram is drawn correctly
		assertEquals( defaultColor, graphics.getColor() );
	}
}
