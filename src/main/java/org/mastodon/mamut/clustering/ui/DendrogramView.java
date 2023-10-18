package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.ClusterComponent;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import com.apporiented.algorithm.clustering.visualization.VCoord;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.util.DendrogramUtils;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Map;

public class DendrogramView< T >
{
	@Nullable
	private final Cluster cluster;

	private final double cutoff;

	private final String headline;

	private final JFrame frame;

	public DendrogramView( @Nullable final Cluster cluster, @Nullable final Map< String, T > objectMapping, final double cutoff,
			final String headline )
	{
		this.cluster = cluster;
		this.cutoff = cutoff;
		this.headline = headline;
		if ( this.cluster != null && objectMapping != null )
			DendrogramUtils.mapLeaveNames( this.cluster, objectMapping );

		frame = new JFrame( "Hierarchical clustering of lineage trees" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setSize( 600, 700 );
		frame.setLayout( new MigLayout( "insets 10, fill" ) );
		frame.add( getPanel(), "grow" );
	}

	public void show()
	{
		frame.setVisible( true );
	}

	public JPanel getPanel()
	{
		DendrogramPanel dendrogramPanel;
		if ( cluster == null )
			dendrogramPanel = new DendrogramPanel(); // NB: empty dendrogram
		else
		{
			dendrogramPanel = new DendrogramPanelWithCutoffLine( cluster.getDistanceValue(), cutoff );
			dendrogramPanel.setModel( cluster );
			dendrogramPanel.setBackground( Color.WHITE );
			dendrogramPanel.setLineColor( Color.BLACK );
			if ( cluster.getDistanceValue() <= 1d )
				adaptScaleBar( dendrogramPanel );
		}

		JPanel panel = new JPanel( new MigLayout( "fill" ) );
		JLabel label = new JLabel( headline );

		panel.add( label, "wrap, align center" );
		panel.add( dendrogramPanel, "grow, push" );

		panel.setBorder( BorderFactory.createEtchedBorder() );

		return panel;
	}

	private void adaptScaleBar( DendrogramPanel dendrogramPanel )
	{
		if ( cluster == null )
			return;
		int zeros = DendrogramUtils.countZerosAfterDecimalPoint( cluster.getDistanceValue() );
		dendrogramPanel.setScaleValueInterval( Math.pow( 10, -( zeros + 1 ) ) );
		dendrogramPanel.setScaleValueDecimals( zeros + 1 );
	}

	private class DendrogramPanelWithCutoffLine extends DendrogramPanel
	{
		private final double maxValue;

		private final double cutoff;

		private DendrogramPanelWithCutoffLine( double maxValue, double cutoff )
		{
			super();
			this.maxValue = maxValue;
			this.cutoff = cutoff;
		}

		@Override
		public void paint( Graphics g )
		{
			super.paint( g );
			if ( cutoff == 0d )
				return;
			if ( !( g instanceof Graphics2D ) )
				return;
			if ( cluster == null )
				return;

			Graphics2D g2 = ( Graphics2D ) g;
			Stroke defaultStroke = g2.getStroke();

			try
			{
				Stroke stroke = new BasicStroke( 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0 );
				g2.setStroke( stroke );
				int yDendrogramOrigin = getBorderBottom() + getBorderTop();
				int yDendrogramEnd = getHeight() - getBorderBottom();
				ClusterComponent component = new ClusterComponent( cluster, false, new VCoord( 0, 0 ) );
				int maxLabelWidth = DendrogramUtils.getMaxLeafNameWidth( g2, cluster ) + component.getNamePadding();
				int screenWidth = getWidth() - getBorderLeft() - getBorderRight() - maxLabelWidth;
				double xFactor = screenWidth / maxValue;
				int xOffset = getBorderLeft();
				int cutoffX = xOffset + ( int ) ( ( maxValue - cutoff ) * xFactor );
				g.drawLine( cutoffX, yDendrogramOrigin, cutoffX, yDendrogramEnd );
			}
			finally
			{
				g2.setStroke( defaultStroke );
			}
		}
	}
}
