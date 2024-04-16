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
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a UI view of a dendrogram.<br>
 * It encapsulates a {@link Classification} object and a headline that can describe the classification, e.g. the parameters that were used for it.
 * @param <T> the type of the objects that are classified
 */
public class DendrogramView< T > implements TagSetModel.TagSetModelListener
{

	private final JLabel headlineLabel;

	private final Model model;

	private final JFrame frame;

	private final JPanel canvas = new JPanel( new MigLayout( "fill" ) );

	private final JCheckBox showThresholdCheckBox = new JCheckBox( "Show classification threshold" );

	private final JCheckBox showMedianCheckBox = new JCheckBox( "Show median of tree similarities" );

	private final JCheckBox showRootLabelsCheckBox = new JCheckBox( "Show root labels" );

	private final JCheckBox showTagLabelsCheckBox = new JCheckBox( "Show tag labels" );

	private final JComboBox< TagSetElement > tagSetComboBox = new JComboBox<>();

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
		if ( null != model )
			this.model.getTagSetModel().listeners().add( this );
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
		updateTagSetOptions();
	}

	private void initBehavior()
	{
		showThresholdCheckBox.addActionListener( ignore -> dendrogramPanel.toggleShowThreshold() );
		showMedianCheckBox.addActionListener( ignore -> dendrogramPanel.toggleShowMedian() );
		ActionListener labelListener = ignore -> dendrogramPanel.setLeaveLabeling( showRootLabelsCheckBox.isSelected(),
				showTagLabelsCheckBox.isSelected(),
				tagSetComboBox.getSelectedItem() == null ? null : ( ( TagSetElement ) tagSetComboBox.getSelectedItem() ).tagSet );
		showRootLabelsCheckBox.addActionListener( labelListener );
		showTagLabelsCheckBox.addActionListener( labelListener );
		tagSetComboBox.addActionListener( labelListener );

		showThresholdCheckBox.setSelected( true );
		dendrogramPanel.toggleShowThreshold();
		showMedianCheckBox.setSelected( true );
		dendrogramPanel.toggleShowMedian();
		showRootLabelsCheckBox.setSelected( true );
		dendrogramPanel.setLeaveLabeling( true, false, null );
	}

	private void initLayout()
	{
		canvas.add( headlineLabel, "wrap, align center" );
		canvas.add( showThresholdCheckBox, "split 2" );
		canvas.add( showMedianCheckBox, "wrap" );
		canvas.add( showRootLabelsCheckBox, "split 3" );
		canvas.add( showTagLabelsCheckBox );
		canvas.add( tagSetComboBox, "wrap" );
		dendrogramPanel.setBackground( Color.WHITE );
		JScrollPane scrollPane = new JScrollPane( dendrogramPanel );
		canvas.add( scrollPane, "grow, push" );
		canvas.setBorder( BorderFactory.createEtchedBorder() );
	}

	private static int getDefaultFontSize()
	{
		return new JLabel().getFontMetrics( new JLabel().getFont() ).getHeight();
	}

	@Override
	public void tagSetStructureChanged()
	{
		updateTagSetOptions();
	}

	private void updateTagSetOptions()
	{
		if ( null == model )
			return;
		TagSetElement tagSetElement = ( TagSetElement ) tagSetComboBox.getSelectedItem();
		tagSetComboBox.removeAllItems();
		List< TagSetStructure.TagSet > tagSets = model.getTagSetModel().getTagSetStructure().getTagSets();
		List< TagSetElement > choices = new ArrayList<>();
		tagSets.forEach( tagSet -> choices.add( new TagSetElement( tagSet ) ) );
		choices.forEach( tagSetComboBox::addItem );
		if ( null != tagSetElement )
			for ( TagSetElement element : choices )
				if ( element.tagSet.equals( tagSetElement.tagSet ) )
					tagSetComboBox.setSelectedItem( element );
	}

	private static class TagSetElement
	{
		private final TagSetStructure.TagSet tagSet;

		public TagSetElement( final TagSetStructure.TagSet tagSet )
		{
			this.tagSet = tagSet;
		}

		@Override
		public String toString()
		{
			return tagSet.getName();
		}
	}
}
