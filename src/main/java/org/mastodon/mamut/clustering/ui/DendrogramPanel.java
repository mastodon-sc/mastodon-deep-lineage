/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 N/A
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
import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.clustering.util.Classification;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for painting dendrograms derived from a {@link Classification} object.<p>
 * It is a re-implementation of the class {@link com.apporiented.algorithm.clustering.visualization.DendrogramPanel} from the hierarchical clustering library.<p>
 * In addition to {@link com.apporiented.algorithm.clustering.visualization.DendrogramPanel}, this class adds:
 * <ul>
 *     <li>the possibility to paint an extra line on top of dendrogram (e.g. the cut-off line)</li>
 *     <li>colorizing the clusters below this cut-off line</li>
 * </ul>
 *
 * @author Stefan Hahmann
 */
public class DendrogramPanel< T > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final Classification< T > classification;

	private final CustomizedClusterComponent component;

	private final ModelMetrics modelMetrics;

	private static final BasicStroke CLUSTER_LINE_STROKE = new BasicStroke( 1.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND );

	private static final BasicStroke CUT_OFF_LINE_STROKE =
			new BasicStroke( 1.75f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0 );

	static final Color CLUSTER_LINE_COLOR = Color.BLACK;

	private static final boolean SHOW_DISTANCE_VALUES = false;

	private static final boolean SHOW_SCALE = true;

	private static final int BORDER_TOP = 20;

	private static final int BORDER_LEFT = 20;

	private static final int BORDER_RIGHT = 20;

	private static final int BORDER_BOTTOM = 20;

	private static final int SCALE_PADDING = 10;

	private static final int SCALE_TICK_LENGTH = 4;

	private static final int SCALE_TICK_LABEL_PADDING = 4;

	private static final String NO_DATA_AVAILABLE = "No cluster data available.";

	/**
	 * Creates an empty {@link DendrogramPanel}.
	 */
	public DendrogramPanel()
	{
		super();
		this.classification = null;
		this.component = null;
		this.modelMetrics = null;
	}

	/**
	 * Creates a {@link DendrogramPanel} for the given {@link Classification} object.
	 *
	 * @param classification the {@link Classification} object to be visualized by this {@link DendrogramPanel}
	 */
	public DendrogramPanel( final Classification< T > classification )
	{
		super();
		this.classification = classification;
		this.component = createComponent( classification.getRootCluster() );
		this.modelMetrics = createModelMetrics( this.component );
	}

	@Override
	public void paint( Graphics g )
	{
		super.paint( g );
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.setColor( CLUSTER_LINE_COLOR );
		g2.setStroke( CLUSTER_LINE_STROKE );
		int componentWidth = getWidth();
		int componentHeight = getHeight();
		DisplayMetrics metrics = new DisplayMetrics( componentWidth, componentHeight, g2 );

		if ( component != null )
		{
			component.paint(
					g2, metrics.xOffset, metrics.yOffset, metrics.xConversionFactor, metrics.yConversionFactor, SHOW_DISTANCE_VALUES );
			if ( SHOW_SCALE )
			{
				Axis axis = new Axis( metrics );
				axis.paint( g2 );
			}
			paintCutoffLine( g2, metrics );
		}
		else
		{
			Rectangle2D rect = g2.getFontMetrics().getStringBounds( NO_DATA_AVAILABLE, g2 );
			int x = ( int ) ( metrics.widthDisplay / 2.0 - rect.getWidth() / 2.0 );
			int y = ( int ) ( metrics.heightDisplay / 2.0 - rect.getHeight() / 2.0 );
			g2.drawString( NO_DATA_AVAILABLE, x, y );
		}
	}

	private void paintCutoffLine( final Graphics2D g2, final DisplayMetrics displayMetrics )
	{
		paintVerticalLine( g2, CUT_OFF_LINE_STROKE, classification.getCutoff(), displayMetrics );
	}

	private void paintVerticalLine(
			final Graphics2D g2, final Stroke stroke, final double xModelValue, final DisplayMetrics displayMetrics
	)
	{
		Stroke defaultStroke = g2.getStroke();
		try
		{
			g2.setStroke( stroke );
			g2.draw( getVerticalLine( xModelValue, displayMetrics ) );
		}
		finally
		{
			g2.setStroke( defaultStroke );
		}
	}

	private CustomizedClusterComponent createComponent( final Cluster cluster )
	{
		if ( cluster == null )
			return null;

		return new CustomizedClusterComponent( cluster, classification.getObjectClassifications() );
	}

	Line2D getVerticalLine( final double xModelValue, final DisplayMetrics displayMetrics )
	{
		int yDendrogramOrigin = BORDER_BOTTOM + BORDER_TOP;
		int yDendrogramEnd = getHeight() - BORDER_BOTTOM;
		int lineX = getDisplayXCoordinate( xModelValue, displayMetrics );
		return new Line2D.Float( lineX, yDendrogramOrigin, lineX, yDendrogramEnd );
	}

	private ModelMetrics createModelMetrics( final ClusterComponent component )
	{
		if ( component == null )
			return null;
		double minX = component.getRectMinX();
		double maxX = component.getRectMaxX();
		double minY = component.getRectMinY();
		double maxY = component.getRectMaxY();

		return new ModelMetrics( minX, minY, maxX - minX, maxY - minY );
	}

	private static class ModelMetrics
	{
		private final double xModelOrigin;

		private final double yModelOrigin;

		private final double wModel;

		private final double hModel;

		private ModelMetrics( final double xModelOrigin, final double yModelOrigin, final double wModel, final double hModel )
		{
			this.xModelOrigin = xModelOrigin;
			this.yModelOrigin = yModelOrigin;
			this.wModel = wModel;
			this.hModel = hModel;
		}
	}

	class DisplayMetrics
	{
		private final int widthDisplay;

		private final int heightDisplay;

		private final int xDisplayOrigin;

		private final int yDisplayOrigin;

		private final double xConversionFactor;

		private final double yConversionFactor;

		private final int xOffset;

		private final int yOffset;

		DisplayMetrics( final int componentWidth, final int componentHeight, final Graphics2D g2 )
		{
			int nameOffset = 0;
			int axisHeight = 0;
			if ( component != null )
			{
				nameOffset = component.getMaxNameWidth( g2, false ) + component.getNamePadding();
				if ( SHOW_SCALE )
					axisHeight = getAxisHeight( g2 );
			}

			widthDisplay = componentWidth - BORDER_LEFT - BORDER_RIGHT - nameOffset;
			heightDisplay = componentHeight - BORDER_TOP - BORDER_BOTTOM - axisHeight;

			xDisplayOrigin = BORDER_LEFT;
			yDisplayOrigin = BORDER_BOTTOM + axisHeight;

			xConversionFactor = widthDisplay / modelMetrics.wModel;
			yConversionFactor = heightDisplay / modelMetrics.hModel;

			xOffset = ( int ) ( xDisplayOrigin - modelMetrics.xModelOrigin * xConversionFactor );
			yOffset = ( int ) ( yDisplayOrigin - modelMetrics.yModelOrigin * yConversionFactor );
		}
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
	static int countZerosAfterDecimalPoint( final double number )
	{
		return ( int ) Math.max( 0, -Math.floor( Math.log10( Math.abs( number ) ) + 1 ) );
	}

	int getDisplayXCoordinate( final double modelXCoordinate, final DisplayMetrics displayMetrics )
	{
		double xDisplayCoordinate = modelXCoordinate * displayMetrics.xConversionFactor;
		return displayMetrics.xDisplayOrigin + displayMetrics.widthDisplay - ( int ) xDisplayCoordinate;
	}

	static int getAxisHeight( final Graphics g )
	{
		return g.getFontMetrics().getHeight() + 2 * SCALE_PADDING + SCALE_TICK_LABEL_PADDING;
	}

	class Axis
	{
		final Line2D line;

		final Set< Pair< Line2D, String > > ticks;

		final DisplayMetrics displayMetrics;

		private double scaleValueInterval = 0;

		private int scaleValueDecimalDigits = 0;

		Axis( final DisplayMetrics displayMetrics )
		{
			this.displayMetrics = displayMetrics;
			adaptScaleBar();
			line = createLine();
			ticks = createTicks();
		}

		private void adaptScaleBar()
		{
			if ( classification == null )
				return;
			Cluster cluster = classification.getRootCluster();
			if ( cluster == null )
				return;
			if ( cluster.getDistanceValue() > 1d )
				return;
			int zeros = countZerosAfterDecimalPoint( cluster.getDistanceValue() );
			scaleValueInterval = Math.pow( 10, -( zeros + 1 ) );
			scaleValueDecimalDigits = zeros + 1;
		}

		private Line2D createLine()
		{
			int xStart = displayMetrics.xDisplayOrigin;
			int xEnd = displayMetrics.xDisplayOrigin + displayMetrics.widthDisplay;
			int y = displayMetrics.yDisplayOrigin - SCALE_PADDING;
			return new Line2D.Float( xStart, y, xEnd, y );
		}

		private Set< Pair< Line2D, String > > createTicks()
		{
			Set< Pair< Line2D, String > > tickSet = new HashSet<>();

			double xModelInterval = getTickModelInterval();
			int xDisplayTick = displayMetrics.xDisplayOrigin + displayMetrics.widthDisplay;
			float yDisplayTick = ( float ) line.getY1() - SCALE_TICK_LENGTH;
			double tickValue = 0;
			while ( xDisplayTick >= displayMetrics.xDisplayOrigin )
			{
				String format = "%." + scaleValueDecimalDigits + "f";
				String tickValueString = String.format( format, tickValue );
				tickSet.add(
						Pair.of( new Line2D.Float( xDisplayTick, ( float ) line.getY1(), xDisplayTick, yDisplayTick ), tickValueString ) );
				tickValue += xModelInterval;
				xDisplayTick = getDisplayXCoordinate( tickValue, displayMetrics );
			}
			return tickSet;
		}

		private double getTickModelInterval()
		{
			double scaleModelWidth = component.getCluster().getTotalDistance();
			if ( scaleValueInterval <= 0 )
				return Math.round( scaleModelWidth / 10.0 );
			else
				return scaleValueInterval;
		}

		private void paint( final Graphics2D g2 )
		{
			g2.draw( line );
			for ( Pair< Line2D, String > tick : ticks )
			{
				Line2D tickLine = tick.getLeft();
				g2.draw( tickLine );
				String tickValue = tick.getRight();
				Rectangle2D bounds = g2.getFontMetrics().getStringBounds( tickValue, g2 );
				g2.drawString(
						tickValue, ( int ) ( tickLine.getX1() - ( bounds.getWidth() / 2 ) ),
						( int ) tickLine.getY2() - SCALE_TICK_LABEL_PADDING
				);
			}
		}
	}
}
