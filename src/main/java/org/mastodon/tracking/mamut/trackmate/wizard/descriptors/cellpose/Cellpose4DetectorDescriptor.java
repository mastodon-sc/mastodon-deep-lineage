package org.mastodon.tracking.mamut.trackmate.wizard.descriptors.cellpose;

import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_CELLPROB_THRESHOLD;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_DIAMETER;
import static org.mastodon.mamut.detection.cellpose.Cellpose.DEFAULT_FLOW_THRESHOLD;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mastodon.mamut.detection.cellpose.Cellpose4Detector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.AbstractSpotDetectorDescriptor;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.SpotDetectorDescriptor;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotDetectorDescriptor.class, name = "Cellpose4 spot detector configuration descriptor" )
public class Cellpose4DetectorDescriptor extends CellposeDetectorDescriptor
{

	public static final String KEY_CELL_PROBABILITY_THRESHOLD = "cellpose4CellProbabilityThreshold";

	public static final String KEY_FLOW_THRESHOLD = "cellpose4flowThreshold";

	public static final String KEY_DIAMETER = "cellpose4Diameter";

	@Override
	protected void persistSettings()
	{
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, this.cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_FLOW_THRESHOLD, this.flowThreshold.getValue() );
		detectorSettings.put( KEY_DIAMETER, this.diameter.getValue() );
	}

	@Override
	protected void logSettings()
	{
		logger.info( String.format( "  - cell probability threshold: %s%n",
				settings.values.getDetectorSettings().get( KEY_CELL_PROBABILITY_THRESHOLD ) ) );
		logger.info( String.format( "  - flow threshold: %s%n",
				settings.values.getDetectorSettings().get( KEY_FLOW_THRESHOLD ) ) );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
		// Get the values.
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		// Get the cell probability threshold.
		final Object cellprobThresholdObject = detectorSettings.get( KEY_CELL_PROBABILITY_THRESHOLD );
		final double cellprobThreshold;
		if ( null == cellprobThresholdObject )
			cellprobThreshold = DEFAULT_CELLPROB_THRESHOLD; // default
		else
			cellprobThreshold = Double.parseDouble( String.valueOf( cellprobThresholdObject ) );

		// Get the flow threshold.
		final Object flowThresholdObject = detectorSettings.get( KEY_FLOW_THRESHOLD );
		final double flowThreshold;
		if ( null == flowThresholdObject )
			flowThreshold = DEFAULT_FLOW_THRESHOLD; // default
		else
			flowThreshold = Double.parseDouble( String.valueOf( flowThresholdObject ) );

		// Get the diameter.
		final Object diameterObject = detectorSettings.get( KEY_DIAMETER );
		final double diameter;
		if ( null == diameterObject )
			diameter = DEFAULT_DIAMETER; // default
		else
			diameter = Double.parseDouble( String.valueOf( diameterObject ) );

		// Update them in the config panel.
		this.cellProbabilityThreshold.setValue( cellprobThreshold );
		this.flowThreshold.setValue( flowThreshold );
		this.diameter.setValue( diameter );
	}

	@Override
	protected String getDetectorName()
	{
		return "Cellpose4";
	}

	@Override
	protected void addModelTypeSelection( final AbstractSpotDetectorDescriptor.ConfigPanel panel )
	{
		// No model type selection for Cellpose4.
	}

	@Override
	protected void addRespectAnisotropyCheckbox( final AbstractSpotDetectorDescriptor.ConfigPanel panel )
	{
		// No respect anisotropy checkbox for Cellpose4.
	}

	@Override
	protected void grabSettings()
	{
		if ( null == settings )
			return;

		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_CELL_PROBABILITY_THRESHOLD, cellProbabilityThreshold.getValue() );
		detectorSettings.put( KEY_FLOW_THRESHOLD, flowThreshold.getValue() );
		detectorSettings.put( KEY_DIAMETER, diameter.getValue() );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( Cellpose4Detector.class );
	}
}
