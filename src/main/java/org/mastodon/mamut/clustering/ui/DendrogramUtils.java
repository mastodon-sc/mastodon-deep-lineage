package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.*;
import java.util.Map;

public class DendrogramUtils
{
	private DendrogramUtils()
	{
		// prevent from instantiation
	}

	public static < T > void showDendrogram( @Nullable final Cluster cluster, @Nullable final Map< String, T > objectNames,
			final double cutoff )
	{
		if ( cluster != null && objectNames != null )
			renameClusters( cluster, objectNames );
		JPanel dendrogram = createDendrogram( "Dendrogram", cluster, cutoff );
		JFrame frame = new JFrame( "Hierarchical clustering of lineage trees" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setSize( 500, 300 );
		frame.setLayout( new MigLayout( "insets 10, fill" ) );
		frame.add( dendrogram, "grow" );
		frame.setVisible( true );
	}

	public static JPanel createDendrogram( String headline, Cluster cluster )

	{
		return createDendrogram( headline, cluster, 0.0d );
	}

	public static JPanel createDendrogram( String headline, Cluster cluster, double cutoff )

	{
		DendrogramPanel dendrogramPanel;
		if ( cluster == null )
			dendrogramPanel = new DendrogramPanel(); // NB: empty dendrogram
		else
		{
			dendrogramPanel = new DendrogramPanelWithCutoffLine( cluster.getDistanceValue(), cutoff );
			dendrogramPanel.setModel( cluster );
			if ( cluster.getDistanceValue() < 1d )
			{
				int zeros = countZerosAfterDecimal( cluster.getDistanceValue() );
				dendrogramPanel.setScaleValueInterval( Math.pow( 10, -( zeros + 1 ) ) );
				dendrogramPanel.setScaleValueDecimals( zeros + 1 );
			}
		}

		JPanel panel = new JPanel( new MigLayout( "fill" ) );
		JLabel label = new JLabel( headline );

		panel.add( label, "wrap, align center" );
		panel.add( dendrogramPanel, "grow, push" );

		panel.setBorder( BorderFactory.createEtchedBorder() );

		return panel;
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
				// a stroke for a dashed line with a thickness of 2
				Stroke stroke = new BasicStroke( 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5, 5 }, 0 );
				g2.setStroke( stroke );
				int yDendrogramOrigin = 2 * getBorderBottom();
				int dendrogramHeight = getHeight() - getBorderBottom();
				int dendrogramWidth = getWidth() - getBorderRight();
				int cutoffX = dendrogramWidth - ( int ) ( cutoff / maxDistance * dendrogramWidth );

				g.drawLine( cutoffX, yDendrogramOrigin, cutoffX, dendrogramHeight );
			}
			finally
			{
				g2.setStroke( defaultStroke );
			}
		}
	}

	public static int countZerosAfterDecimal( double number )
	{
		String numberString = String.valueOf( number );
		int decimalIndex = numberString.indexOf( '.' );
		if ( decimalIndex == -1 )
		{
			// No decimal point found, return 0
			return 0;
		}

		int zeroCount = 0;
		for ( int i = decimalIndex + 1; i < numberString.length(); i++ )
		{
			if ( numberString.charAt( i ) == '0' )
			{
				zeroCount++;
			}
			else
			{
				break;
			}
		}
		return zeroCount;
	}
}
