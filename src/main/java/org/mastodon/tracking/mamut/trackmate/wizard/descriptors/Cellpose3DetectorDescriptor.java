package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.mastodon.mamut.detection.cellpose.Cellpose3;
import org.mastodon.mamut.detection.cellpose.Cellpose3Detector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotDetectorDescriptor.class, name = "Cellpose3 spot detector configuration descriptor" )
public class Cellpose3DetectorDescriptor extends CellposeDetectorDescriptor
{
	public final static String KEY_MODEL_TYPE = "cellpose3ModelType";

	public final static String KEY_CELL_PROBABILITY_THRESHOLD = "cellpose3CellProbabilityThreshold";

	public final static String KEY_FLOW_THRESHOLD = "cellpose3FlowThreshold";

	public final static String KEY_RESPECT_ANISOTROPY = "cellpose3RespectAnisotropy";

	private JComboBox< Cellpose3.ModelType > modelType;

	private JCheckBox respectAnisotropy;

	@Override
	protected void persistSettings()
	{
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, modelType.getSelectedItem() );
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_FLOW_THRESHOLD, flowThreshold.getValue() );
		detectorSettings.put( KEY_RESPECT_ANISOTROPY, respectAnisotropy.isSelected() );
	}

	@Override
	protected void logSettings()
	{
		logger.info( String.format( "  - model type: %s\n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
		logger.info( String.format( "  - cell probability threshold: %s\n",
				settings.values.getDetectorSettings().get( KEY_CELL_PROBABILITY_THRESHOLD ) ) );
		logger.info( String.format( "  - flow threshold: %s\n", settings.values.getDetectorSettings().get( KEY_FLOW_THRESHOLD ) ) );
		logger.info( String.format( "  - respect anisotropy: %s\n", settings.values.getDetectorSettings().get( KEY_RESPECT_ANISOTROPY ) ) );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
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
			cellprobThreshold = Cellpose3.DEFAULT_CELLPROB_THRESHOLD;
		else
			cellprobThreshold = Double.parseDouble( String.valueOf( cellprobThresholdObject ) );

		// Get the flow threshold.
		final Object flowThresholdObject = detectorSettings.get( KEY_FLOW_THRESHOLD );
		final double flowThreshold;
		if ( null == flowThresholdObject )
			flowThreshold = Cellpose3.DEFAULT_FLOW_THRESHOLD;
		else
			flowThreshold = Double.parseDouble( String.valueOf( flowThresholdObject ) );

		// Get the anisotropy.
		final Object respectAnisotropyObject = detectorSettings.get( KEY_RESPECT_ANISOTROPY );
		final boolean respectAnisotropy;
		if ( null == respectAnisotropyObject )
			respectAnisotropy = true; // default
		else
			respectAnisotropy = Boolean.parseBoolean( String.valueOf( respectAnisotropyObject ) );

		// Update them in the config panel.
		this.modelType.setSelectedItem( modelType );
		this.cellProbabilityThreshold.setValue( cellprobThreshold );
		this.flowThreshold.setValue( flowThreshold );
		this.respectAnisotropy.setSelected( respectAnisotropy );
	}

	@Override
	protected String getDetectorName()
	{
		return "Cellpose3";
	}

	@Override
	protected void addModelTypeSelection( final ConfigPanel panel )
	{
		modelType = new JComboBox<>( Cellpose3.ModelType.values() );
		JLabel modelTypeLabel = new JLabel( "Model type:" );
		panel.add( modelTypeLabel, "align left, wrap" );
		panel.add( modelType, "align left, grow" );
	}

	@Override
	protected void addRespectAnisotropyCheckbox( final ConfigPanel panel )
	{
		respectAnisotropy = new JCheckBox( "Respect anisotropy" );
		panel.add( respectAnisotropy, "align left, wrap" );
		String respectAnisotropyText =
				"<html>Respecting anisotropy may take significantly more time,<br>but can lead to better detection results.</html>";
		panel.add( new JLabel( respectAnisotropyText ), "align left, wmin 200, grow" );
	}

	@Override
	protected void configureDetectorSpecificFields( final ConfigPanel panel )
	{
		super.configureDetectorSpecificFields( panel );
	}

	@Override
	protected void grabSettings()
	{
		if ( null == settings )
			return;

		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, modelType.getSelectedItem() );
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_FLOW_THRESHOLD, flowThreshold.getValue() );
		detectorSettings.put( KEY_RESPECT_ANISOTROPY, respectAnisotropy.isSelected() );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( Cellpose3Detector.class );
	}
}
