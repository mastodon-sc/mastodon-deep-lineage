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

import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.model.Model;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;

/**
 * A class that represents a UI view of a dendrogram.<br>
 * It encapsulates a {@link Classification} object and a headline that can describe the classification, e.g. the parameters that were used for it.
 * @param <T> the type of the objects that are classified
 */
public class DendrogramView< T >
{

	private final JLabel headlineLabel;

	private final Model model;

	private final JFrame frame;

	private final JPanel canvas = new JPanel( new MigLayout( "fill" ) );

	private final JCheckBox showThresholdCheckBox = new JCheckBox( "Show classification threshold" );

	private final JCheckBox showMedianCheckBox = new JCheckBox( "Show median of tree similarities" );

	private final DendrogramPanel< T > dendrogramPanel;

	public DendrogramView( final Classification< T > classification, final String headline )
	{
		this( classification, headline, null );
	}

	public DendrogramView( final Classification< T > classification, final String headline, final Model model )
	{
		this.model = model;

		frame = new JFrame( "Hierarchical clustering of lineage trees" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setSize( 1000, 700 );
		frame.setLayout( new MigLayout( "insets 10, fill" ) );
		frame.add( canvas, "grow" );

		headlineLabel = new JLabel( headline );
		dendrogramPanel = new DendrogramPanel<>( classification );
		dendrogramPanel.setPreferredSize( new Dimension( -1, minDendrogramHeight ) );

		initCanvas();
	}

	/**
	 * Sets the visibility of the frame to {@code true}.
	 */
	public void show()
	{
		frame.setVisible( true );
	}

	public JPanel getCanvas()
	{
		return canvas;
	}

	void initCanvas()
	{
		initLayout();
		initBehavior();
	}

	private void initBehavior()
	{
		showThresholdCheckBox.addActionListener( ignore -> dendrogramPanel.toggleShowThreshold() );
		showMedianCheckBox.addActionListener( ignore -> dendrogramPanel.toggleShowMedian() );

		showThresholdCheckBox.setSelected( true );
		dendrogramPanel.toggleShowThreshold();
		showMedianCheckBox.setSelected( true );
		dendrogramPanel.toggleShowMedian();
	}

	private void initLayout()
	{
		canvas.add( headlineLabel, "wrap, align center" );
		canvas.add( showThresholdCheckBox, "split 2" );
		canvas.add( showMedianCheckBox, "wrap" );
		dendrogramPanel.setBackground( Color.WHITE );
		JScrollPane scrollPane = new JScrollPane( dendrogramPanel );
		canvas.add( scrollPane, "grow, push" );
		canvas.setBorder( BorderFactory.createEtchedBorder() );
	}

	private static int getDefaultFontSize()
	{
		return new JLabel().getFontMetrics( new JLabel().getFont() ).getHeight();
	}
}
