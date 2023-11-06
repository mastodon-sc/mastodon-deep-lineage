package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertNotNull;

public class CustomizedClusterComponentTest
{

	@Test
	public void testCustomizedClusterComponent()
	{
		Cluster cluster = null;
		CustomizedClusterComponent customizedClusterComponent = new CustomizedClusterComponent( cluster, true, null, Color.BLUE );
		assertNotNull( customizedClusterComponent );
	}
}
