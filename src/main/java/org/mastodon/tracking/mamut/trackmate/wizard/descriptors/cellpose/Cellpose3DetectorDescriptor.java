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
package org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_GPU_ID;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_GPU_MEMORY_FRACTION;
import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_CELLPROB_THRESHOLD;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_DIAMETER;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_FLOW_THRESHOLD;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.mastodon.mamut.detection.cellpose.Cellpose3;
import org.mastodon.mamut.detection.cellpose.Cellpose3Detector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.AbstractSpotDetectorDescriptor;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.SpotDetectorDescriptor;
import org.scijava.plugin.Plugin;

/**
 * The {@code Cellpose3DetectorDescriptor} class is a configuration descriptor
 * for the {@link Cellpose3Detector}.
 */
@Plugin( type = SpotDetectorDescriptor.class, name = "Cellpose3 spot detector configuration descriptor" )
public class Cellpose3DetectorDescriptor extends CellposeDetectorDescriptor
{
	public static final String KEY_MODEL_TYPE = "cellpose3ModelType";

	public static final String KEY_CELL_PROBABILITY_THRESHOLD = "cellpose3CellProbabilityThreshold";

	public static final String KEY_FLOW_THRESHOLD = "cellpose3FlowThreshold";

	public static final String KEY_DIAMETER = "cellpose3Diameter";

	public static final String KEY_RESPECT_ANISOTROPY = "cellpose3RespectAnisotropy";

	private JComboBox< Cellpose3.ModelType > modelTypeSelection;

	private JCheckBox respectAnisotropyCheckbox;

	@Override
	protected void persistSettings()
	{
		super.persistSettings();
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, modelTypeSelection.getSelectedItem() );
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_FLOW_THRESHOLD, flowThreshold.getValue() );
		detectorSettings.put( KEY_DIAMETER, diameter.getValue() );
		detectorSettings.put( KEY_RESPECT_ANISOTROPY, respectAnisotropyCheckbox.isSelected() );
	}

	@Override
	protected void logSettings()
	{
		logger.info( String.format( "  - model type: %s%n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
		logger.info( String.format( "  - cell probability threshold: %s%n",
				settings.values.getDetectorSettings().get( KEY_CELL_PROBABILITY_THRESHOLD ) ) );
		logger.info( String.format( "  - flow threshold: %s%n", settings.values.getDetectorSettings().get( KEY_FLOW_THRESHOLD ) ) );
		logger.info( String.format( "  - estimated diameter: %s%n", settings.values.getDetectorSettings().get( KEY_DIAMETER ) ) );
		logger.info( String.format( "  - resolution level: %s%n", settings.values.getDetectorSettings().get( KEY_LEVEL ) ) );
		logger.info( String.format( "  - respect anisotropy: %s%n", settings.values.getDetectorSettings().get( KEY_RESPECT_ANISOTROPY ) ) );
		logger.info( String.format( "  - GPU ID: %s%n", settings.values.getDetectorSettings().get( KEY_GPU_ID ) ) );
		logger.info(
				String.format( "  - GPU memory fraction: %s%n", settings.values.getDetectorSettings().get( KEY_GPU_MEMORY_FRACTION ) ) );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
		super.getSettingsAndUpdateConfigPanel();
		// Get the values.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		// Get the model type.
		final Cellpose3.ModelType modelType;
		final Object modelTypeObject = detectorSettings.get( KEY_MODEL_TYPE );
		if ( null == modelTypeObject )
			modelType = Cellpose3.ModelType.CYTO3; // default
		else
			modelType = Cellpose3.ModelType.fromString( String.valueOf( modelTypeObject ) );
		// Get the cell probability threshold.
		final Object cellprobThresholdObject = detectorSettings.get( KEY_CELL_PROBABILITY_THRESHOLD );
		final double cellprobThreshold;
		if ( null == cellprobThresholdObject )
			cellprobThreshold = DEFAULT_CELLPROB_THRESHOLD;
		else
			cellprobThreshold = Double.parseDouble( String.valueOf( cellprobThresholdObject ) );

		// Get the flow threshold.
		final Object flowThresholdObject = detectorSettings.get( KEY_FLOW_THRESHOLD );
		final double flowThreshold;
		if ( null == flowThresholdObject )
			flowThreshold = DEFAULT_FLOW_THRESHOLD;
		else
			flowThreshold = Double.parseDouble( String.valueOf( flowThresholdObject ) );

		// Get the diameter.
		final Object diameterObject = detectorSettings.get( KEY_DIAMETER );
		final double diameter;
		if ( null == diameterObject )
			diameter = DEFAULT_DIAMETER; // default
		else
			diameter = Double.parseDouble( String.valueOf( diameterObject ) );

		// Get the anisotropy.
		final Object respectAnisotropyObject = detectorSettings.get( KEY_RESPECT_ANISOTROPY );
		final boolean respectAnisotropy;
		if ( null == respectAnisotropyObject )
			respectAnisotropy = true; // default
		else
			respectAnisotropy = Boolean.parseBoolean( String.valueOf( respectAnisotropyObject ) );

		// Update them in the config panel.
		this.modelTypeSelection.setSelectedItem( modelType );
		this.cellProbabilityThreshold.setValue( cellprobThreshold );
		this.flowThreshold.setValue( flowThreshold );
		this.diameter.setValue( diameter );
		this.respectAnisotropyCheckbox.setSelected( respectAnisotropy );
	}

	@Override
	protected String getDetectorName()
	{
		return "Cellpose3";
	}

	@Override
	protected void addModelTypeSelection( final AbstractSpotDetectorDescriptor.ConfigPanel panel )
	{
		modelTypeSelection = new JComboBox<>( Cellpose3.ModelType.values() );
		JLabel modelTypeLabel = new JLabel( "Model type:" );
		panel.add( modelTypeLabel, "align left, wrap" );
		panel.add( modelTypeSelection, "align left, grow" );
	}

	@Override
	protected void addRespectAnisotropyCheckbox( final AbstractSpotDetectorDescriptor.ConfigPanel panel )
	{
		respectAnisotropyCheckbox = new JCheckBox( "Respect anisotropy" );
		panel.add( respectAnisotropyCheckbox, "align left, wrap" );
		String respectAnisotropyText =
				"<html>Respecting anisotropy may take significantly more time, but can lead to better detection results.</html>";
		panel.add( new JLabel( respectAnisotropyText ), "align left, wmin 200, grow" );
	}

	@Override
	protected void grabSettings()
	{
		super.grabSettings();
		if ( null == settings )
			return;

		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, modelTypeSelection.getSelectedItem() );
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_FLOW_THRESHOLD, flowThreshold.getValue() );
		detectorSettings.put( KEY_DIAMETER, diameter.getValue() );
		detectorSettings.put( KEY_RESPECT_ANISOTROPY, respectAnisotropyCheckbox.isSelected() );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( Cellpose3Detector.class );
	}
}
