/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.util.HierarchicalClusteringResult;
import org.mastodon.mamut.clustering.util.HierarchicalClusteringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomizedClusterComponentTest
{

	/**
	 * this tests only if the paint method resets the color to default and runs without exceptions, not if the dendrogram is drawn correctly
	 */
	@Test
	void testPaint()
	{
		HierarchicalClusteringResult< String > hierarchicalClusteringResult =
				HierarchicalClusteringUtils.getClusteringUsingClusterNumber( ClusterData.example1.getKey(), ClusterData.example1.getValue(),
						new AverageLinkageStrategy(),
						3
				);
		Cluster cluster = hierarchicalClusteringResult.getRootCluster();
		CustomizedClusterComponent customizedClusterComponent =
				new CustomizedClusterComponent( cluster, hierarchicalClusteringResult.getGroups() );

		int width = 400;
		int height = 400;
		Image image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics graphics = image.getGraphics();
		Color defaultColor = Color.WHITE;
		graphics.setColor( defaultColor );

		customizedClusterComponent.paint( ( Graphics2D ) graphics, 0, 0, 1d, 1d, false );
		assertEquals( defaultColor, graphics.getColor() );
	}
}
