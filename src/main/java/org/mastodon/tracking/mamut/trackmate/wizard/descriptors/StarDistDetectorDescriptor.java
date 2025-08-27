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

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.mamut.detection.stardist.StarDist.DEFAULT_NMS_THRESHOLD;
import static org.mastodon.mamut.detection.stardist.StarDist.DEFAULT_PROB_THRESHOLD;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mastodon.mamut.detection.stardist.StarDist;
import org.mastodon.mamut.detection.stardist.StarDistDetector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.plugin.Plugin;

/**
 * The {@code StarDistDetectorDescriptor} class is a configuration descriptor
 * for the {@link StarDistDetector}.
 */
@Plugin( type = SpotDetectorDescriptor.class, name = "StarDist spot detector configuration descriptor" )
public class StarDistDetectorDescriptor extends AbstractSpotDetectorDescriptor
{
	public static final String KEY_MODEL_TYPE = "starDistModelType";

	public static final String KEY_PROB_THRESHOLD = "starDistProbThreshold";

	public static final String KEY_NMS_THRESHOLD = "starDistNmsThreshold";

	private JComboBox< StarDist.ModelType > modelTypeSelection;

	private JSpinner probThreshold;

	private JSpinner nmsThreshold;

	@Override
	protected void persistSettings()
	{
		super.persistSettings();
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, this.modelTypeSelection.getSelectedItem() );
		detectorSettings.put( KEY_PROB_THRESHOLD, this.probThreshold.getValue() );
		detectorSettings.put( KEY_NMS_THRESHOLD, this.nmsThreshold.getValue() );
	}

	@Override
	protected void logSettings()
	{
		logger.info( String.format( "  - model type: %s%n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
		logger.info( String.format( "  - probability threshold: %s%n", settings.values.getDetectorSettings().get( KEY_PROB_THRESHOLD ) ) );
		logger.info( String.format( "  - overlap threshold: %s%n ", settings.values.getDetectorSettings().get( KEY_NMS_THRESHOLD ) ) );
		logger.info( String.format( "  - resolution level: %s%n ", settings.values.getDetectorSettings().get( KEY_LEVEL ) ) );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
		super.getSettingsAndUpdateConfigPanel();
		// Get the values.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();

		// Get the model type.
		final StarDist.ModelType modelType;
		final Object modelTypeObject = detectorSettings.get( KEY_MODEL_TYPE );
		if ( null == modelTypeObject )
			modelType = StarDist.ModelType.DEMO; // default
		else
			modelType = StarDist.ModelType.fromString( String.valueOf( modelTypeObject ) );

		// Update in the config panel.
		this.modelTypeSelection.setSelectedItem( modelType );

		// Get the probability threshold.
		final Object probThresholdObject = detectorSettings.get( KEY_PROB_THRESHOLD );
		final double probThresholdValue;
		if ( null == probThresholdObject )
			probThresholdValue = DEFAULT_PROB_THRESHOLD; // default
		else
			probThresholdValue = Double.parseDouble( String.valueOf( probThresholdObject ) );

		// Get the flow threshold.
		final Object nmsThresholdObject = detectorSettings.get( KEY_NMS_THRESHOLD );
		final double nmsThresholdValue;
		if ( null == nmsThresholdObject )
			nmsThresholdValue = DEFAULT_NMS_THRESHOLD; // default
		else
			nmsThresholdValue = Double.parseDouble( String.valueOf( nmsThresholdObject ) );

		// Update them in the config panel.
		this.probThreshold.setValue( probThresholdValue );
		this.nmsThreshold.setValue( nmsThresholdValue );

	}

	@Override
	protected String getDetectorName()
	{
		return "StarDist";
	}

	@Override
	protected void configureDetectorSpecificFields( final JPanel contentPanel )
	{
		modelTypeSelection = new JComboBox<>( StarDist.ModelType.values() );
		modelTypeSelection.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		modelTypeSelection.setRenderer( new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(
					JList< ? > list, Object value, int index,
					boolean isSelected, boolean cellHasFocus )
			{
				super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				if ( value instanceof StarDist.ModelType )
				{
					setText( ( ( StarDist.ModelType ) value ).getDisplayName() );
				}
				return this;
			}
		} );

		JLabel modelTypeLabel = new JLabel( "Model type:" );
		modelTypeLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( modelTypeLabel, "align left, growx" );
		contentPanel.add( modelTypeSelection, LAYOUT_CONSTRAINT );

		probThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 0.1 ) );
		probThreshold.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		String probText =
				"<html>"
						+ "Probability/Score Threshold: Determine the number of object candidates<br>"
						+ "Higher values lead to fewer segmented objects, but will likely avoid false positives."
						+ "</html>";
		JLabel probLabel = new JLabel( probText );
		probLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( probLabel, LAYOUT_CONSTRAINT );
		contentPanel.add( probThreshold, "align left, grow" );

		nmsThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 0.1 ) );
		nmsThreshold.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		String nmsText =
				"<html>"
						+ "Overlap Threshold: Determine when two objects are considered the same during non-maximum suppression.<br>"
						+ "Higher values allow objects to overlap substantially."
						+ "</html>";
		JLabel nmsLabel = new JLabel( nmsText );
		nmsLabel.setFont( contentPanel.getFont().deriveFont( contentPanel.getFont().getSize2D() - 2f ) );
		contentPanel.add( nmsLabel, LAYOUT_CONSTRAINT );
		contentPanel.add( nmsThreshold, "align left, grow" );
	}

	@Override
	protected void grabSettings()
	{
		super.grabSettings();
		if ( null == settings )
			return;

		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, modelTypeSelection.getSelectedItem() );
		detectorSettings.put( KEY_PROB_THRESHOLD, probThreshold.getValue() );
		detectorSettings.put( KEY_NMS_THRESHOLD, nmsThreshold.getValue() );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( StarDistDetector.class );
	}
}
