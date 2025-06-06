/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.mastodon.mamut.clustering.util.HierarchicalClusteringResult;
import org.mastodon.mamut.model.Model;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.scijava.prefs.PrefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that represents a UI view of a dendrogram.<br>
 * It encapsulates a {@link HierarchicalClusteringResult} object and a headline that write the parameters that were used for it.
 * @param <T> the type of the objects that are clustered
 */
public class DendrogramView< T > extends JFrame implements TagSetModel.TagSetModelListener
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String SHOW_THRESHOLD = "showThreshold";

	private static final String SHOW_MEDIAN = "showMedian";

	private static final String SHOW_ROOT_LABELS = "showRootLabels";

	private static final String SHOW_TAG_LABELS = "showTagLabels";

	private static final String TAG_SET_NAME = "tagSetName";

	static final String PNG_EXTENSION = "png";

	private static final String SVG_EXTENSION = "svg";

	private static final String CSV_EXTENSION = "csv";

	private final JLabel headlineLabel;

	private final Model model;

	private final PrefService prefs;

	private final JPanel canvas = new JPanel( new MigLayout( "fill" ) );

	private final JCheckBox showThresholdCheckBox = new JCheckBox( "Show clustering threshold" );

	private final JCheckBox showMedianCheckBox = new JCheckBox( "Show median of tree similarities" );

	private final JCheckBox showRootLabelsCheckBox = new JCheckBox( "Show root labels" );

	private final JCheckBox showTagLabelsCheckBox = new JCheckBox( "Show tag labels" );

	private final JComboBox< TagSetElement > tagSetComboBox = new JComboBox<>();

	private final JButton menuButton = new JButton( "⋮" );

	private final DendrogramPanel< T > dendrogramPanel;

	private final HierarchicalClusteringResult< T > hierarchicalClusteringResult;

	private TagSetStructure.TagSet selectedTagSet;

	private final String projectName;

	public DendrogramView( final HierarchicalClusteringResult< T > hierarchicalClusteringResult, final String headline )
	{
		this( hierarchicalClusteringResult, headline, null, null, null );
	}

	public DendrogramView( final HierarchicalClusteringResult< T > hierarchicalClusteringResult, final String headline, final Model model,
			final PrefService prefs,
			final String projectName )
	{
		super( "Hierarchical clustering of lineage trees" );

		this.hierarchicalClusteringResult = hierarchicalClusteringResult;
		this.model = model;
		this.prefs = prefs;
		this.projectName = projectName;


		int minHeight = 50;
		int initialDendrogramHeight = hierarchicalClusteringResult == null ? minHeight
				: ( hierarchicalClusteringResult.getObjectCount() ) * getDefaultFontHeight() + DendrogramPanel.DENDROGRAM_VERTICAL_OFFSET;
		initialDendrogramHeight += 200;
		initialDendrogramHeight = Math.min( initialDendrogramHeight, 1000 );
		setSize( 1000, initialDendrogramHeight );
		setLayout( new MigLayout( "insets 10, fill" ) );
		add( canvas, "grow" );
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		headlineLabel = new JLabel( headline );
		dendrogramPanel = new DendrogramPanel<>( hierarchicalClusteringResult );
		int minDendrogramHeight = hierarchicalClusteringResult == null ? minHeight
				: ( hierarchicalClusteringResult.getObjectCount() ) * getDefaultFontSize() + DendrogramPanel.DENDROGRAM_VERTICAL_OFFSET;
		dendrogramPanel.setPreferredSize( new Dimension( -1, minDendrogramHeight ) );

		initCanvas();
		if ( null != model )
			this.model.getTagSetModel().listeners().add( this );
	}

	public JPanel getCanvas()
	{
		return canvas;
	}

	void initCanvas()
	{
		initLayout();
		updateTagSetOptions();
		initSettings();
		initBehavior();
	}

	private void initLayout()
	{
		canvas.add( headlineLabel, "align left, growx" );
		canvas.add( menuButton, "align right, wrap" );
		canvas.add( showThresholdCheckBox, "split 2" );
		canvas.add( showMedianCheckBox, "wrap" );
		canvas.add( showRootLabelsCheckBox, "split 3" );
		canvas.add( showTagLabelsCheckBox );
		canvas.add( tagSetComboBox, "wrap" );
		dendrogramPanel.setBackground( Color.WHITE );
		JScrollPane scrollPane = new JScrollPane( dendrogramPanel );
		canvas.add( scrollPane, "grow, push, span, wrap" );
		canvas.setBorder( BorderFactory.createEtchedBorder() );
	}

	private void initSettings()
	{
		boolean showThresholdDefault = true;
		boolean showMedianDefault = true;
		boolean showRootLabelsDefault = true;
		boolean showTagLabelsDefault = false;
		boolean showThreshold =
				prefs == null ? showThresholdDefault : prefs.getBoolean( DendrogramView.class, SHOW_THRESHOLD, showThresholdDefault );
		boolean showMedian = prefs == null ? showMedianDefault : prefs.getBoolean( DendrogramView.class, SHOW_MEDIAN, showMedianDefault );
		boolean showRootLabels =
				prefs == null ? showRootLabelsDefault : prefs.getBoolean( DendrogramView.class, SHOW_ROOT_LABELS, showRootLabelsDefault );
		boolean showTagLabels =
				prefs == null ? showTagLabelsDefault : prefs.getBoolean( DendrogramView.class, SHOW_TAG_LABELS, showTagLabelsDefault );
		showThresholdCheckBox.setSelected( showThreshold );
		showMedianCheckBox.setSelected( showMedian );
		showRootLabelsCheckBox.setSelected( showRootLabels );
		showTagLabelsCheckBox.setSelected( showTagLabels );
		selectedTagSet = tagSetComboBox.getSelectedItem() == null ? null : ( ( TagSetElement ) tagSetComboBox.getSelectedItem() ).tagSet;
		dendrogramPanel.showThreshold( showThreshold );
		dendrogramPanel.showMedian( showMedian );
		dendrogramPanel.setLeaveLabeling( showRootLabels, showTagLabels, selectedTagSet );
	}

	private void initBehavior()
	{
		ActionListener showThresholdListener = ignore -> showThreshold( showThresholdCheckBox.isSelected() );
		showThresholdCheckBox.addActionListener( showThresholdListener );
		ActionListener showMedianListener = ignore -> showMedian( showMedianCheckBox.isSelected() );
		showMedianCheckBox.addActionListener( showMedianListener );
		ActionListener tagSetListener = event -> {
			selectedTagSet =
					tagSetComboBox.getSelectedItem() == null ? null : ( ( TagSetElement ) tagSetComboBox.getSelectedItem() ).tagSet;
			setLeaveLabeling( showRootLabelsCheckBox.isSelected(), showTagLabelsCheckBox.isSelected(), selectedTagSet );
		};
		showRootLabelsCheckBox.addActionListener( tagSetListener );
		showTagLabelsCheckBox.addActionListener( tagSetListener );
		tagSetComboBox.addActionListener( tagSetListener );
		JPopupMenu popupMenu = new PopupMenu();
		ActionListener menuListener = event -> popupMenu.show( menuButton, 0, menuButton.getHeight() );
		menuButton.addActionListener( menuListener );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent windowEvent )
			{
				showThresholdCheckBox.removeActionListener( showThresholdListener );
				showMedianCheckBox.removeActionListener( showMedianListener );
				showRootLabelsCheckBox.removeActionListener( tagSetListener );
				showTagLabelsCheckBox.removeActionListener( tagSetListener );
				tagSetComboBox.removeActionListener( tagSetListener );
				menuButton.removeActionListener( menuListener );
				if ( null != model )
					model.getTagSetModel().listeners().remove( DendrogramView.this );
			}
		} );
	}

	private void showThreshold( final boolean showThreshold )
	{
		if ( prefs != null )
			prefs.put( DendrogramView.class, SHOW_THRESHOLD, showThreshold );
		dendrogramPanel.showThreshold( showThreshold );
	}

	private void showMedian( final boolean showMedian )
	{
		if ( prefs != null )
			prefs.put( DendrogramView.class, SHOW_MEDIAN, showMedian );
		dendrogramPanel.showMedian( showMedian );
	}

	private void setLeaveLabeling( final boolean showRootLabels, final boolean showTagLabels, final TagSetStructure.TagSet tagSet )
	{
		if ( prefs != null )
		{
			prefs.put( DendrogramView.class, SHOW_ROOT_LABELS, showRootLabels );
			prefs.put( DendrogramView.class, SHOW_TAG_LABELS, showTagLabels );
			prefs.put( DendrogramView.class, TAG_SET_NAME, tagSet == null ? "" : tagSet.getName() );
		}
		dendrogramPanel.setLeaveLabeling( showRootLabels, showTagLabels, tagSet );
	}

	private static int getDefaultFontSize()
	{
		return new JLabel().getFontMetrics( new JLabel().getFont() ).getFont().getSize();
	}

	private static int getDefaultFontHeight()
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
		TagSetElement selectedTagSetElement = ( TagSetElement ) tagSetComboBox.getSelectedItem();
		tagSetComboBox.removeAllItems();
		List< TagSetStructure.TagSet > tagSets = model.getTagSetModel().getTagSetStructure().getTagSets();
		List< TagSetElement > choices = new ArrayList<>();
		tagSets.forEach( tagSet -> choices.add( new TagSetElement( tagSet ) ) );
		choices.forEach( tagSetComboBox::addItem );
		if ( selectedTagSetElement == null )
		{
			String tagSetName = prefs == null ? null : prefs.get( DendrogramView.class, TAG_SET_NAME, null );
			for ( int i = 0; i < tagSetComboBox.getItemCount(); i++ )
			{
				final TagSetElement element = tagSetComboBox.getItemAt( i );
				if ( element.tagSet.getName().equals( tagSetName ) )
					selectedTagSetElement = element;
			}
		}
		if ( selectedTagSetElement != null )
			for ( TagSetElement element : choices )
				if ( element.tagSet.equals( selectedTagSetElement.tagSet ) )
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

	private final class PopupMenu extends JPopupMenu
	{
		private PopupMenu()
		{
			String exportText = "Export dendrogram as ";
			JMenuItem pngItem = new JMenuItem( exportText + PNG_EXTENSION.toUpperCase() );
			pngItem.addActionListener( actionEvent -> chooseFileAndExport(
					PNG_EXTENSION, projectName + "_dendrogram",
					( file, value ) -> dendrogramPanel.exportPng( file, value, PNG_EXTENSION ) ) );
			add( pngItem );
			JMenuItem svgItem = new JMenuItem( exportText + SVG_EXTENSION.toUpperCase() );
			svgItem.addActionListener( actionEvent -> chooseFileAndExport(
					SVG_EXTENSION, projectName + "_dendrogram", ( file, value ) -> dendrogramPanel.exportSvg( file ) ) );
			add( svgItem );
			JMenuItem csvItem = new JMenuItem( "Export clustering result to CSV" );
			csvItem.addActionListener( actionEvent -> chooseFileAndExport( CSV_EXTENSION, projectName + "_lineage_clustering",
					( file, resolution ) -> hierarchicalClusteringResult.exportCsv( file, selectedTagSet ) ) );
			add( csvItem );
		}

		private void chooseFileAndExport( final String extension, final String fileName, final ObjIntConsumer< File > exportFunction )
		{
			File chosenFile = FileChooser.chooseFile( this, fileName + '.' + extension,
					new ExtensionFileFilter( extension ), "Save dendrogram to " + extension, FileChooser.DialogType.SAVE );
			if ( chosenFile != null )
			{
				int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
				exportFunction.accept( chosenFile, screenResolution );
				openFile( chosenFile );
			}
		}

		private void openFile( final File chosenFile )
		{
			try
			{
				Desktop.getDesktop().open( chosenFile );
			}
			catch ( IOException e )
			{
				logger.error( "Could not open dendrogram image file: {}. Message: {}", chosenFile.getAbsolutePath(),
						e.getMessage() );
			}
		}
	}
}
