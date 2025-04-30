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
package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

import java.awt.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.imagej.ops.OpService;
import net.miginfocom.swing.MigLayout;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.detection.Cellpose;
import org.mastodon.mamut.detection.CellposeDetector;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.Wizard;
import org.mastodon.tracking.mamut.trackmate.wizard.util.WizardUtils;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotDetectorDescriptor.class, name = "Cellpose spot detector configuration descriptor" )
public class CellposeDetectorDescriptor extends SpotDetectorDescriptor
{

	private Settings settings;

	public final static String KEY_MODEL_TYPE = "cellposeModelType";

	public final static String KEY_CELL_PROBABILITY_THRESHOLD = "cellposeCellProbabilityThreshold";

	public final static String KEY_RESPECT_ANISOTROPY = "cellposeRespectAnisotropy";

	private static final Icon PREVIEW_ICON =
			new ImageIcon( Objects.requireNonNull( Wizard.class.getResource( "led-icon-eye-green.png" ) ) );

	private ProjectModel appModel;

	private ViewerFrameMamut viewFrame;

	private final Model previewModel;

	private static final String IDENTIFIER = "Configure Cellpose detector";

	@Parameter
	private OpService ops;

	public CellposeDetectorDescriptor()

	{
		this.panelIdentifier = IDENTIFIER;
		this.targetPanel = new ConfigPanel();
		this.previewModel = new Model();
	}

	/*
	 * The UI part, as a private class.
	 */
	private class ConfigPanel extends JPanel
	{

		private final JComboBox< Cellpose.MODEL_TYPE > modelType;

		private final JSpinner cellProbabilityThreshold;

		private final JCheckBox respectAnisotropy = new JCheckBox( "Respect anisotropy" );

		private final JButton preview = new JButton( "Preview", PREVIEW_ICON );

		private final JLabel info = new JLabel( "", JLabel.RIGHT );

		private ConfigPanel()
		{
			setLayout( new MigLayout( "wrap 1", "[grow]", "[]20[]" ) );

			JLabel headlineLabel = new JLabel( "Configure cellpose detector" );
			headlineLabel.setHorizontalAlignment( SwingConstants.LEFT );
			headlineLabel.setFont( getFont().deriveFont( Font.BOLD ) );
			add( headlineLabel, "growx" );

			JLabel modelTypeLabel = new JLabel( "Model type:" );
			add( modelTypeLabel, "align left, wrap" );
			modelType = new JComboBox<>( Cellpose.MODEL_TYPE.values() );
			add( modelType, "align left, grow" );

			SpinnerNumberModel model = new SpinnerNumberModel( 0.0, 0.0, 10.0, 0.1 );
			cellProbabilityThreshold = new JSpinner( model );

			String cellProbText = "<html>Cell probability threshold:<br>0 ... more detections<br>10 ... less detections</html>";
			JLabel cellProbLabel = new JLabel( cellProbText );
			add( cellProbLabel, "align left, wmin 200, wrap" );
			add( cellProbabilityThreshold, "align left, grow" );

			add( respectAnisotropy, "align left, wrap" );
			String respectAnisotropyText =
					"<html>Respecting anisotropy may take significantly more time,<br>but can lead to better detection results.</html>";
			add( new JLabel( respectAnisotropyText ), "align left, wmin 200, grow" );

			preview.addActionListener( ( e ) -> preview() );
			add( preview, "align right, wrap" );

			info.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( info, "align right, wrap" );
		}
	}

	@Override
	public void setTrackMate( final TrackMate trackmate )
	{
		/*
		 * This method is called when the panel is shown when the user 'enters'
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
		// Get the model type.
		final Cellpose.MODEL_TYPE modelType;
		final Object modelTypeObject = detectorSettings.get( KEY_MODEL_TYPE );
		if ( null == modelTypeObject )
			modelType = Cellpose.MODEL_TYPE.CYTO; // default
		else
			modelType = Cellpose.MODEL_TYPE.fromString( String.valueOf( modelTypeObject ) );
		// Get the cell probability threshold.
		final Object cellprobThresholdObject = detectorSettings.get( KEY_CELL_PROBABILITY_THRESHOLD );
		final double cellprobThreshold;
		if ( null == cellprobThresholdObject )
			cellprobThreshold = 3d; // default
		else
			cellprobThreshold = Double.parseDouble( String.valueOf( cellprobThresholdObject ) );
		// Get the anisotropy.
		final Object respectAnisotropyObject = detectorSettings.get( KEY_RESPECT_ANISOTROPY );
		final boolean respectAnisotropy;
		if ( null == respectAnisotropyObject )
			respectAnisotropy = true; // default
		else
			respectAnisotropy = Boolean.parseBoolean( String.valueOf( respectAnisotropyObject ) );

		// Show them in the config panel.
		final ConfigPanel panel = ( ConfigPanel ) targetPanel;
		panel.modelType.setSelectedItem( modelType );
		panel.cellProbabilityThreshold.setValue( cellprobThreshold );
		panel.respectAnisotropy.setSelected( respectAnisotropy );
	}

	@Override
	public void aboutToHidePanel()
	{
		if ( null != viewFrame )
			viewFrame.dispose();
		viewFrame = null;

		if ( null == settings )
			return;

		final ConfigPanel panel = ( ConfigPanel ) targetPanel;
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, panel.modelType.getSelectedItem() );
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, panel.cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_RESPECT_ANISOTROPY, panel.respectAnisotropy.isSelected() );

		logger.info( String.format( "  - model type: %s\n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
		logger.info( String.format( "  - cell probability threshold: %s\n",
				settings.values.getDetectorSettings().get( KEY_CELL_PROBABILITY_THRESHOLD ) ) );
		logger.info( String.format( "  - respect anisotropy: %s\n", settings.values.getDetectorSettings().get( KEY_RESPECT_ANISOTROPY ) ) );

		logger.info( "" );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( CellposeDetector.class );
	}

	@Override
	public void setAppModel( final ProjectModel appModel )
	{
		this.appModel = appModel;
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
		final JLabelLogger previewLogger = new JLabelLogger( panel.info );
		new Thread( () -> executePreview( currentTimepoint, previewLogger, panel ), "Cellpose detector preview thread" ).start();

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

	/**
	 * Update the settings field of this descriptor with the values set on the
	 * GUI.
	 */
	private void grabSettings()
	{
		if ( null == settings )
			return;

		final ConfigPanel panel = ( ConfigPanel ) targetPanel;
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, panel.modelType.getSelectedItem() );
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, panel.cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_RESPECT_ANISOTROPY, panel.respectAnisotropy.isSelected() );
	}
}
