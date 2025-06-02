package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mastodon.mamut.detection.stardist.StarDist;
import org.mastodon.mamut.detection.stardist.StarDistDetector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.plugin.Plugin;

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
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, this.modelTypeSelection.getSelectedItem() );
		detectorSettings.put( KEY_PROB_THRESHOLD, this.probThreshold.getValue() );
		detectorSettings.put( KEY_NMS_THRESHOLD, this.nmsThreshold.getValue() );
	}

	@Override
	protected void logSettings()
	{
		logger.info( String.format( "  - model type: %s%n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
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
	protected void configureDetectorSpecificFields( final ConfigPanel panel )
	{
		modelTypeSelection = new JComboBox<>( StarDist.ModelType.values() );
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
		panel.add( modelTypeLabel, "align left, wrap" );
		panel.add( modelTypeSelection, "align left, grow" );

		probThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 0.1 ) );
		String probText =
				"<html>"
						+ "Probability/Score Threshold: Determine the number of object candidates<br>"
						+ "Higher values lead to fewer segmented objects, but will likely avoid false positives."
						+ "</html>";
		JLabel probLabel = new JLabel( probText );
		panel.add( probLabel, "align left, wmin 200, wrap" );
		panel.add( probThreshold, "align left, grow" );

		nmsThreshold = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 0.1 ) );
		String nmsText =
				"<html>"
						+ "Overlap Threshold: Determine when two objects are considered the same during non-maximum suppression.<br>"
						+ "Higher values allow objects to overlap substantially."
						+ "</html>";
		JLabel nmsLabel = new JLabel( nmsText );
		panel.add( nmsLabel, "align left, wmin 200, wrap" );
		panel.add( nmsThreshold, "align left, grow" );
	}

	@Override
	protected void grabSettings()
	{
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
