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
package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.DEFAULT_LEVEL;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;

import java.awt.Font;
import java.util.Map;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.imagej.ops.OpService;
import net.miginfocom.swing.MigLayout;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.Wizard;
import org.mastodon.tracking.mamut.trackmate.wizard.util.WizardUtils;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.scijava.plugin.Parameter;

/**
 * A class that contains the common logic for the spot detectors in this package.
 */
public abstract class AbstractSpotDetectorDescriptor extends SpotDetectorDescriptor
{

	protected Settings settings;

	protected ProjectModel appModel;

	private ViewerFrameMamut viewFrame;

	private final Model previewModel;

	private JSpinner level;

	private JLabel levelLabel;

	protected static final String LAYOUT_CONSTRAINT = "align left, wmax 250, growx, wrap";

	@Parameter
	private OpService ops;

	private static final Icon PREVIEW_ICON =
			new ImageIcon( Objects.requireNonNull( Wizard.class.getResource( "led-icon-eye-green.png" ) ) );

	protected AbstractSpotDetectorDescriptor()
	{
		this.panelIdentifier = "Configure " + getDetectorName() + " detector";
		this.targetPanel = new ConfigPanel();
		this.previewModel = new Model();
	}

	protected void persistSettings()
	{
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_LEVEL, this.level.getValue() );
	}

	protected abstract void logSettings();

	protected void getSettingsAndUpdateConfigPanel()
	{
		// Get the values.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();

		final Object levelObject = detectorSettings.get( KEY_LEVEL );
		final int levelValue;
		if ( null == levelObject )
			levelValue = DEFAULT_LEVEL; // default
		else
			levelValue = Integer.parseInt( String.valueOf( levelObject ) );
		this.level.setValue( levelValue );
		SpinnerModel model = this.level.getModel();
		int setupId = ( int ) settings.values.getDetectorSettings().get( DetectorKeys.KEY_SETUP_ID );
		int maxLevels = appModel.getSharedBdvData().getSources().get( setupId ).getSpimSource().getNumMipmapLevels() - 1;
		if ( model instanceof SpinnerNumberModel )
		{
			final SpinnerNumberModel spinnerModel = ( SpinnerNumberModel ) model;
			spinnerModel.setMaximum( maxLevels );
		}
		levelLabel.setText( getLevelText( maxLevels ) );
	}

	protected abstract String getDetectorName();

	protected abstract void configureDetectorSpecificFields( final JPanel contentPanel );

	/**
	 * Update the settings field of this descriptor with the values set on the GUI by the user.
	 */
	protected void grabSettings()
	{
		if ( null == settings )
			return;

		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_LEVEL, level.getValue() );
	}

	@Override
	public void setAppModel( final ProjectModel appModel )
	{
		this.appModel = appModel;

		if ( null == settings )
			return;

		// To display the detector settings values, either the default ones or the one that were set previously, read these values from the TrackMate instance.
		getSettingsAndUpdateConfigPanel();
	}

	@Override
	public void aboutToHidePanel()
	{
		if ( null != viewFrame )
			viewFrame.dispose();
		viewFrame = null;

		if ( null == settings )
			return;

		persistSettings();
		logSettings();
	}

	@Override
	public void setTrackMate( final TrackMate trackmate )
	{
		// This method is called when the user 'enters' the config panel.
		// Get the settings map to configure. It is updated with the values set by the user on the panel when they 'leave' the panel, cf. aboutToHidePanel().
		this.settings = trackmate.getSettings();
	}

	protected class ConfigPanel extends JPanel
	{
		private final JButton preview = new JButton( "Preview", PREVIEW_ICON );

		private final JLabel info = new JLabel( "", SwingConstants.RIGHT );

		protected final JPanel contentPanel;

		protected ConfigPanel()
		{
			setLayout( new MigLayout( "fill", "[grow]", "[grow]" ) );

			contentPanel = new JPanel();
			contentPanel.setLayout( new MigLayout( "fillx, wrap 1", "[grow]", "[]5[]" ) );

			final JScrollPane scrollPane = new JScrollPane( contentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );

			add( scrollPane, "grow" );

			JLabel headlineLabel = new JLabel( "Configure " + getDetectorName() + " detector" );
			headlineLabel.setHorizontalAlignment( SwingConstants.LEFT );
			headlineLabel.setFont( getFont().deriveFont( Font.BOLD ) );
			contentPanel.add( headlineLabel, "growx" );

			configureDetectorSpecificFields( contentPanel );

			level = new JSpinner( new SpinnerNumberModel( 0, 0, 0, 1 ) );
			level.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			levelLabel = new JLabel( getLevelText( 0 ) );
			levelLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			contentPanel.add( levelLabel, LAYOUT_CONSTRAINT );
			contentPanel.add( level, "align left, grow" );

			preview.addActionListener( e -> preview() );
			contentPanel.add( preview, "align right, wrap" );

			info.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			contentPanel.add( info, "align right, wrap" );
		}

		private void preview()
		{
			if ( null == appModel )
				return;

			final SharedBigDataViewerData shared = appModel.getSharedBdvData();
			viewFrame = WizardUtils.previewFrame( viewFrame, shared, previewModel );
			final int currentTimepoint = viewFrame.getViewerPanel().state().getCurrentTimepoint();

			final ConfigPanel panel = ( ConfigPanel ) targetPanel;
			panel.preview.setEnabled( false );
			final SpotDetectorDescriptor.JLabelLogger previewLogger = new SpotDetectorDescriptor.JLabelLogger( panel.info );
			new Thread( () -> executePreview( currentTimepoint, previewLogger, panel ), "Spot detector preview thread" ).start();
		}

		private void executePreview( final int currentTimepoint, final JLabelLogger previewLogger, final ConfigPanel panel )
		{
			try
			{
				grabSettings();
				final boolean ok =
						WizardUtils.executeDetectionPreview( previewModel, settings, ops, currentTimepoint, previewLogger, statusService );
				if ( !ok )
					return;

				final int nSpots = WizardUtils.countSpotsIn( previewModel, currentTimepoint );
				panel.info.setText( "Found " + nSpots + " spots in time-point " + currentTimepoint );
			}
			finally
			{
				panel.preview.setEnabled( true );
			}
		}
	}

	private static String getLevelText( final int maxLevels )
	{
		return "<html>Resolution level:<br>0 ... highest (slower, more accurate)<br>" + maxLevels
				+ " ... (faster, less accurate)</html>";
	}
}
