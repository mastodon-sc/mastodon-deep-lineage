package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.Distance;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;
import org.mockito.Mockito;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DendrogramPanelTest
{
	private Classification< String > classification;

	private Graphics2D graphics;

	@Before
	public void setUp()
	{
		classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(),
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
	public void testCountZerosAfterDecimalPoint()
	{
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 5 ) );
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 0.1 ) );
		assertEquals( 1, DendrogramPanel.countZerosAfterDecimalPoint( 0.01 ) );
		assertEquals( 2, DendrogramPanel.countZerosAfterDecimalPoint( -0.003 ) );
	}

	@Test
	public void testDendrogramPanel()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		assertNotNull( dendrogramPanel );
	}

	@Test
	public void testDendrogramPanelAxis()
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
	public void testDendrogramPanelAxisSmallerOne()
	{
		Cluster cluster = classification.getRootCluster();
		assertNotNull( cluster );
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
	public void testPaint()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		int width = 600;
		int height = 600;
		Image image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics graphics = image.getGraphics();
		Color defaultColor = Color.WHITE;
		graphics.setColor( defaultColor );
		dendrogramPanel.paint( graphics );
		assertEquals( DendrogramPanel.CLUSTER_LINE_COLOR, graphics.getColor() );
	}

	@Test
	public void testGetVerticalLine()
	{
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		Line2D line = dendrogramPanel.getVerticalLine( 25d, dendrogramPanel.new DisplayMetrics( 507, 426, graphics ) );
		assertEquals( 312d, line.getX1(), 0.01 );
	}

	private void adaptClusterValues( Cluster cluster )
	{
		cluster.getChildren().forEach( this::adaptClusterValues );
		cluster.setDistance( new Distance( cluster.getDistanceValue() / 100d ) );
	}
}
