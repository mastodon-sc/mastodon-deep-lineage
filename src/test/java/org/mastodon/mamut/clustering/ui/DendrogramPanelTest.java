/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
import com.apporiented.algorithm.clustering.Distance;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;
import org.mockito.Mockito;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DendrogramPanelTest
{
	private Classification< String > classification;

	private Graphics2D graphics;

	@BeforeEach
	void setUp()
	{
		classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.example1.getKey(), ClusterData.example1.getValue(),
						new AverageLinkageStrategy(),
						3
				);

		// The graphics object is mocked to avoid side effects that would influence the test caused by different default font settings on different platforms
		graphics = Mockito.mock( Graphics2D.class );
		FontMetrics fontMetrics = Mockito.mock( FontMetrics.class );
		Rectangle2D rectangle2D = new Rectangle2D.Double( 0, 0, 15, 10 );
		Mockito.when( graphics.getFontMetrics() ).thenReturn( fontMetrics );
		Mockito.when( fontMetrics.getStringBounds( Mockito.anyString(), Mockito.any() ) ).thenReturn( rectangle2D );
		Mockito.when( fontMetrics.getHeight() ).thenReturn( 16 );
	}


	@Test
	void testCountZerosAfterDecimalPoint()
	{
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 5 ) );
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 0.1 ) );
		assertEquals( 1, DendrogramPanel.countZerosAfterDecimalPoint( 0.01 ) );
		assertEquals( 2, DendrogramPanel.countZerosAfterDecimalPoint( -0.003 ) );
	}

	@Test
	void testDendrogramPanel()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		assertNotNull( dendrogramPanel );
	}

	@Test
	void testDendrogramPanelAxis()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		DendrogramPanel< String >.Axis axis =
				dendrogramPanel.new Axis( dendrogramPanel.new DisplayMetrics( 507, 426, graphics ) );
		Set< String > expectedTickValues = new HashSet<>( Arrays.asList( "0", "7", "14", "21", "28", "35", "42", "49", "56", "63", "70" ) );
		Set< String > actualTickValues = axis.ticks.stream().map( Pair::getValue ).collect( Collectors.toSet() );
		assertEquals( 11, axis.ticks.size() );
		assertEquals( expectedTickValues, actualTickValues );
	}

	@Test
	void testDendrogramPanelAxisSmallerOne()
	{
		Cluster cluster = classification.getRootCluster();
		adaptClusterValues( cluster );
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		DendrogramPanel< String >.Axis axis =
				dendrogramPanel.new Axis( dendrogramPanel.new DisplayMetrics( 507, 426, graphics ) );
		Set< Double > expectedTickValuesDouble = new HashSet<>( Arrays.asList( 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7 ) );
		Set< String > expectedTickValues =
				expectedTickValuesDouble.stream().map( d -> String.format( "%.1f", d ) ).collect( Collectors.toSet() );
		Set< String > actualTickValues = axis.ticks.stream().map( Pair::getValue ).collect( Collectors.toSet() );
		assertEquals( 8, axis.ticks.size() );
		assertEquals( expectedTickValues, actualTickValues );
	}

	@Test
	void testPaint()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		int width = 600;
		int height = 600;
		Image image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics graphics = image.getGraphics();
		Color defaultColor = Color.WHITE;
		graphics.setColor( defaultColor );
		dendrogramPanel.showMedian( true );
		dendrogramPanel.showThreshold( true );
		dendrogramPanel.setLeaveLabeling( true, true, null );
		dendrogramPanel.paint( graphics );
		assertEquals( DendrogramPanel.CLUSTER_LINE_COLOR, graphics.getColor() );
	}

	@Test
	void testPaintNoData()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( null );
		int width = 600;
		int height = 600;
		Image image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics graphics = image.getGraphics();
		assertDoesNotThrow( () -> dendrogramPanel.paint( graphics ) );
	}

	@Test
	void testGetVerticalLine()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		Line2D line = dendrogramPanel.getVerticalLine( 25d, dendrogramPanel.new DisplayMetrics( 507, 426, graphics ) );
		assertEquals( 312d, line.getX1(), 0.01 );
	}

	@Test
	void testExport() throws IOException
	{
		int width = 600;
		int height = 600;
		int screenResolution = 96;
		int outputWidth = width * DendrogramPanel.PRINT_RESOLUTION / screenResolution;
		int outputHeight = height * DendrogramPanel.PRINT_RESOLUTION / screenResolution;

		File tempFilePng = File.createTempFile( "dendrogram", ".png" );
		tempFilePng.deleteOnExit();

		File tempFileSvg = File.createTempFile( "dendrogram", ".svg" );
		tempFileSvg.deleteOnExit();

		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		dendrogramPanel.setSize( width, height );
		dendrogramPanel.exportPng( tempFilePng, screenResolution );
		dendrogramPanel.exportSvg( tempFileSvg );

		Image readPng = ImageIO.read( tempFilePng );
		assertEquals( outputWidth, readPng.getWidth( null ) );
		assertEquals( outputHeight, readPng.getHeight( null ) );
		assertTrue( tempFileSvg.exists() );
		assertTrue( tempFileSvg.length() > 0 );
	}

	private void adaptClusterValues( Cluster cluster )
	{
		cluster.getChildren().forEach( this::adaptClusterValues );
		cluster.setDistance( new Distance( cluster.getDistanceValue() / 100d ) );
	}
}
