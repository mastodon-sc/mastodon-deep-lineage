package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.VCoord;
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
import java.util.Map;
import java.util.Set;

/**
 * Class for painting dendrograms derived from a {@link Cluster} object.<p>
 * In addition to {@link com.apporiented.algorithm.clustering.visualization.DendrogramPanel}, this class adds the possibility to paint an extra line on top of dendrogram (e.g. the cut-off line) and colorizing the clusters below this cut-off line.
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

	private static final Color CLUSTER_LINE_COLOR = Color.BLACK;

	private static final boolean SHOW_DISTANCE_VALUES = false;

	private static final boolean SHOW_SCALE = true;

	private static final int BORDER_TOP = 20;

	private static final int BORDER_LEFT = 20;

	private static final int BORDER_RIGHT = 20;

	private static final int BORDER_BOTTOM = 20;

	private static final int SCALE_PADDING = 10;

	private static final int SCALE_TICK_LENGTH = 4;

	private static final int SCALE_TICK_LABEL_PADDING = 4;

	public DendrogramPanel()
	{
		super();
		this.classification = null;
		this.component = null;
		this.modelMetrics = null;
	}

	public DendrogramPanel( final Classification< T > classification )
	{
		super();
		this.classification = classification;
		this.component = createComponent( classification.getAlgorithmResult() );
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

		int widthDisplay = getWidth() - BORDER_LEFT - BORDER_RIGHT;
		int heightDisplay = getHeight() - BORDER_TOP - BORDER_BOTTOM;

		if ( component != null )
		{
			int xDisplayOrigin = BORDER_LEFT;
			int yDisplayOrigin = BORDER_BOTTOM;
			int nameOffset = component.getMaxNameWidth( g2, false ) + component.getNamePadding();
			widthDisplay -= nameOffset;

			if ( SHOW_SCALE )
			{
				int scaleBarHeight = getScaleBarHeight( g2 );
				heightDisplay -= scaleBarHeight;
				yDisplayOrigin += scaleBarHeight;
			}

			/* Calculate conversion factor and offset for display */
			double xFactor = widthDisplay / modelMetrics.wModel;
			double yFactor = heightDisplay / modelMetrics.hModel;
			DisplayMetrics displayMetrics = new DisplayMetrics( xDisplayOrigin, yDisplayOrigin, widthDisplay, xFactor );
			int xOffset = ( int ) ( xDisplayOrigin - modelMetrics.xModelOrigin * xFactor );
			int yOffset = ( int ) ( yDisplayOrigin - modelMetrics.yModelOrigin * yFactor );
			component.paint( g2, xOffset, yOffset, xFactor, yFactor, SHOW_DISTANCE_VALUES );
			if ( SHOW_SCALE )
			{
				Scalebar scalebar = new Scalebar( displayMetrics );
				scalebar.paint( g2 );
			}
			paintCutoffLine( g2, displayMetrics );
		}
		else
		{
			/* No data available */
			String noData = "No data";
			Rectangle2D rect = g2.getFontMetrics().getStringBounds( noData, g2 );
			int x = ( int ) ( widthDisplay / 2.0 - rect.getWidth() / 2.0 );
			int y = ( int ) ( heightDisplay / 2.0 - rect.getHeight() / 2.0 );
			g2.drawString( noData, x, y );
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
			int yDendrogramOrigin = BORDER_BOTTOM + BORDER_TOP;
			int yDendrogramEnd = getHeight() - BORDER_BOTTOM;
			int lineX = getDisplayXCoordinate( xValue, displayMetrics );
			g2.drawLine( lineX, yDendrogramOrigin, lineX, yDendrogramEnd );
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

		double virtualModelHeight = 1d;
		VCoord initialCoordinate = new VCoord( 0, virtualModelHeight / 2d );

		CustomizedClusterComponent clusterComponent = createComponent( cluster, initialCoordinate, virtualModelHeight, Color.BLACK );
		clusterComponent.setLinkPoint( initialCoordinate );
		return clusterComponent;
	}

	private CustomizedClusterComponent createComponent(
			final Cluster cluster, final VCoord initialCoordinate, final double clusterHeight, Color color
	)
	{
		Map< Cluster, Integer > clusterColors = classification.getClusterColors();
		if ( clusterColors != null && ( clusterColors.containsKey( cluster ) ) )
			color = new Color( clusterColors.get( cluster ) );
		Map< String, T > leafMapping = classification.getLeafMapping();
		if ( leafMapping != null && cluster.isLeaf() && leafMapping.containsKey( cluster.getName() ) )
			cluster.setName( leafMapping.get( cluster.getName() ).toString() );
		CustomizedClusterComponent clusterComponent = new CustomizedClusterComponent( cluster, cluster.isLeaf(), initialCoordinate, color );
		double leafHeight = clusterHeight / cluster.countLeafs();
		double yChild = initialCoordinate.getY() - ( clusterHeight / 2 );
		double distance = cluster.getDistanceValue() == null ? 0 : cluster.getDistanceValue();
		for ( Cluster child : cluster.getChildren() )
		{
			int childLeafCount = child.countLeafs();
			double childHeight = childLeafCount * leafHeight;
			double childDistance = child.getDistanceValue() == null ? 0 : child.getDistanceValue();
			VCoord childInitCoord = new VCoord( initialCoordinate.getX() + ( distance - childDistance ), yChild + childHeight
					/ 2.0 );
			yChild += childHeight;

			/* Traverse cluster node tree */
			CustomizedClusterComponent childComponent = createComponent( child, childInitCoord, childHeight, color );
			childComponent.setLinkPoint( initialCoordinate );
			clusterComponent.getChildren().add( childComponent );
		}
		return clusterComponent;
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

		private ModelMetrics( double xModelOrigin, double yModelOrigin, double wModel, double hModel )
		{
			this.xModelOrigin = xModelOrigin;
			this.yModelOrigin = yModelOrigin;
			this.wModel = wModel;
			this.hModel = hModel;
		}
	}

	static class DisplayMetrics
	{
		private final int xDisplayOrigin;

		private final int yDisplayOrigin;

		private final int widthDisplay;

		private final double xFactor;

		DisplayMetrics( int xDisplayOrigin, int yDisplayOrigin, int widthDisplay, double xFactor )
		{
			this.xDisplayOrigin = xDisplayOrigin;
			this.yDisplayOrigin = yDisplayOrigin;
			this.widthDisplay = widthDisplay;
			this.xFactor = xFactor;
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
	static int countZerosAfterDecimalPoint( double number )
	{
		return ( int ) Math.max( 0, -Math.floor( Math.log10( Math.abs( number ) ) + 1 ) );
	}

	static int getDisplayXCoordinate( final double modelXCoordinate, final DisplayMetrics displayMetrics )
	{
		double xDisplayCoordinate = modelXCoordinate * displayMetrics.xFactor;
		return displayMetrics.xDisplayOrigin + displayMetrics.widthDisplay - ( int ) xDisplayCoordinate;
	}

	static int getScaleBarHeight( Graphics g )
	{
		Rectangle2D rect = g.getFontMetrics().getStringBounds( "0", g );
		return ( int ) rect.getHeight() + SCALE_PADDING + SCALE_PADDING + SCALE_TICK_LABEL_PADDING;
	}

	class Scalebar
	{
		final Line2D line;

		final Set< Pair< Line2D, String > > ticks;

		final DisplayMetrics displayMetrics;

		private double scaleValueInterval = 0;

		private int scaleValueDecimalDigits = 0;

		Scalebar( final DisplayMetrics displayMetrics )
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
			Cluster cluster = classification.getAlgorithmResult();
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

		void paint( Graphics2D g2 )
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
