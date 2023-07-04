package org.mastodon.mamut.clustering.ui.view;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.util.DendrogramUtils;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.*;
import java.util.Map;

public class DendrogramView< T >
{
	@Nullable
	private final Cluster cluster;

	@Nullable
	private final Map< String, T > objectMapping;

	private final double cutoff;

	private final String headline;

	public DendrogramView( @Nullable final Cluster cluster, @Nullable final Map< String, T > objectMapping, final double cutoff,
			final String headline )
	{
		this.cluster = cluster;
		this.objectMapping = objectMapping;
		this.cutoff = cutoff;
		this.headline = headline;
	}

	public void show()
	{
		if ( cluster != null && objectMapping != null )
			DendrogramUtils.renameLeaves( cluster, objectMapping );
		JFrame frame = new JFrame( "Hierarchical clustering of lineage trees" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setSize( 500, 300 );
		frame.setLayout( new MigLayout( "insets 10, fill" ) );
		frame.add( getPanel(), "grow" );
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
			if ( cluster.getDistanceValue() < 1d )
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

	private static class DendrogramPanelWithCutoffLine extends DendrogramPanel
	{
		private final double maxDistance;

		private final double cutoff;

		private DendrogramPanelWithCutoffLine( double maxDistance, double cutoff )
		{
			super();
			this.maxDistance = maxDistance;
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

			Graphics2D g2 = ( Graphics2D ) g;
			Stroke defaultStroke = g2.getStroke();

			try
			{
				Stroke stroke = new BasicStroke( 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0 );
				g2.setStroke( stroke );
				int yDendrogramOrigin = 2 * getBorderBottom();
				int height = getHeight() - getBorderBottom();
				int width = getWidth() - getBorderRight();
				int cutoffX = width - ( int ) ( cutoff / maxDistance * width );

				g.drawLine( cutoffX, yDendrogramOrigin, cutoffX, height );
			}
			finally
			{
				g2.setStroke( defaultStroke );
			}
		}
	}
}
