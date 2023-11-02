package org.mastodon.mamut.clustering.ui;

import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.util.Classification;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Color;

public class DendrogramView< T >
{

	private final Classification< T > classification;

	private final String headline;

	private final JFrame frame;

	public DendrogramView( final Classification< T > classification, final String headline )
	{
		this.classification = classification;
		this.headline = headline;

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
		DendrogramPanel< T > dendrogramPanel;
		if ( classification == null )
			dendrogramPanel = new DendrogramPanel<>(); // NB: empty dendrogram
		else
		{
			dendrogramPanel = new DendrogramPanel<>( classification );
			dendrogramPanel.setBackground( Color.WHITE );
		}

		JPanel panel = new JPanel( new MigLayout( "fill" ) );
		JLabel label = new JLabel( headline );

		panel.add( label, "wrap, align center" );
		panel.add( dendrogramPanel, "grow, push" );

		panel.setBorder( BorderFactory.createEtchedBorder() );

		return panel;
	}
}
