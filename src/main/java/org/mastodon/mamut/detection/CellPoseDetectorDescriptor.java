/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2023 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.detection;

import static org.mastodon.tracking.detection.DetectorKeys.KEY_RADIUS;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.tracking.detection.DetectionUtil;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.SpotDetectorDescriptor;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotDetectorDescriptor.class, name = "CellPose spot detector configuration descriptor" )
public class CellPoseDetectorDescriptor extends SpotDetectorDescriptor
{

	private Settings settings;

	public CellPoseDetectorDescriptor()
	{
		/*
		 * A descriptor represents a 'card' in the wizard we have in Mastodon.
		 * For the wizard to run properly, you need to give it a unique
		 * identifier and a UI panel when you create it.
		 */

		this.panelIdentifier = "Configure random spot detector";
		this.targetPanel = new ConfigPanel(); // described just below.
	}

	/*
	 * The UI part, stored as a private static class. We need to show only two
	 * controls that can configure the number of spots we want to generate and
	 * their diameter.
	 */
	private static class ConfigPanel extends JPanel
	{

		private static final NumberFormat FORMAT = new DecimalFormat( "0.0" );

		private static final long serialVersionUID = 1L;

		private final JFormattedTextField diameter;

		private final JLabel labelDiameterUnit;

		private final JFormattedTextField nSpots;

		public ConfigPanel()
		{
			final GridBagLayout layout = new GridBagLayout();
			layout.columnWidths = new int[] { 80, 80, 40 };
			layout.columnWeights = new double[] { 0.2, 0.7, 0.1 };
			layout.rowHeights = new int[] { 26, 0, 0, 0, 26, 26 };
			layout.rowWeights = new double[] { 1., 0., 0., 0., 0., 1. };
			setLayout( layout );

			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.gridwidth = 3;
			gbc.anchor = GridBagConstraints.BASELINE_LEADING;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets( 5, 5, 5, 5 );

			final JLabel labelTitle = new JLabel( "Configure random spot generator" );
			labelTitle.setFont( getFont().deriveFont( Font.BOLD ) );
			add( labelTitle, gbc );

			// Diameter.

			final JLabel lblDiameter = new JLabel( "Spot diameter:", JLabel.RIGHT );
			gbc.gridy++;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
			add( lblDiameter, gbc );

			this.diameter = new JFormattedTextField( FORMAT );
			diameter.setHorizontalAlignment( JLabel.RIGHT );
			gbc.gridx++;
			gbc.anchor = GridBagConstraints.CENTER;
			add( diameter, gbc );

			labelDiameterUnit = new JLabel();
			gbc.gridx++;
			gbc.anchor = GridBagConstraints.LINE_END;
			add( labelDiameterUnit, gbc );

			// Number of spots

			final JLabel lblNSpots = new JLabel( "Number of spots:", JLabel.RIGHT );
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
			add( lblNSpots, gbc );

			this.nSpots = new JFormattedTextField( 3 );
			nSpots.setHorizontalAlignment( JLabel.RIGHT );
			gbc.gridx++;
			gbc.anchor = GridBagConstraints.CENTER;
			add( nSpots, gbc );
		}
	}

	@Override
	public void setTrackMate( final TrackMate trackmate )
	{
		/*
		 * This method is called when the panel is shown, when the user 'enters'
		 * the config panel. It receives the instance of TrackMate that will run
		 * the detection, and that this panel needs to configure. We use the
		 * method for two things:
		 * 
		 * 1/ Get the settings map to configure. The TrackMate instance stores
		 * the detector settings in a map. We will need to update it with the
		 * values set by the user on the panel when they 'leave' the panel, so
		 * we store a reference to it in the descriptor.
		 */

		this.settings = trackmate.getSettings();

		/*
		 * 2/ We want to display the detector settings values, either the
		 * default ones, or the one that were set previously. For this, we read
		 * these values from the TrackMate instance we receive.
		 */

		if ( null == settings )
			return;

		// Get the values.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		final double diameter;
		final Object objRadius = detectorSettings.get( KEY_RADIUS );
		if ( null == objRadius )
			diameter = 2. * DetectorKeys.DEFAULT_RADIUS;
		else
			diameter = 2. * ( double ) objRadius;

		final int nSpots;
		final Object nSpotsObj = detectorSettings.get( "" );
		if ( null == nSpotsObj )
			nSpots = 30; // default
		else
			nSpots = ( int ) nSpotsObj;

		// Show them in the config panel.
		final ConfigPanel panel = ( ConfigPanel ) targetPanel;
		panel.diameter.setValue( diameter );
		panel.nSpots.setValue( nSpots );
		// Also the spatial units because we are nice.
		final String unit = DetectionUtil.getSpatialUnits( settings.values.getSources() );
		panel.labelDiameterUnit.setText( unit );
	}

	@Override
	public void aboutToHidePanel()
	{
		/*
		 * This method is run when the user 'leaves' the panel going 'forward'.
		 * This is the step just before running the full detection. The only
		 * thing we have to do is to grab the setting values from the panel, and
		 * store them in the settings instance we got when we entered the panel.
		 */

		if ( null == settings )
			return;

		/*
		 * In the wizard, the settings map should already contain the parameter
		 * values for the target channel and min and max time-point to run the
		 * detector on (KEY_SETUP_ID, KEY_MIN_TIMEPOINT and KEY_MAX_TIMEPOINT).
		 * We just have to add the parameters specific to this detector.
		 */

		// Cast the panel field to the right class.
		final ConfigPanel panel = ( ConfigPanel ) targetPanel;
		// Update settings map for the detector.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_RADIUS, ( ( Number ) panel.diameter.getValue() ).doubleValue() / 2. );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		/*
		 * This method is used to tell Mastodon what detectors this wizard
		 * descriptor can configure. This one is only suitable for our dummy
		 * detector, which yields:
		 */
		return Collections.singleton( CellPoseDetector.class );
	}

	@Override
	public void setAppModel( final ProjectModel appModel )
	{
		/*
		 * This method is used to receive the project model. Other detector
		 * descriptors use it to run a preview of the detection, or to display
		 * information about it.
		 * 
		 * In our case we don't do preview, so we don't need to keep a reference
		 * to the project model. This method does then nothing.
		 */
	}
}
