package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.mastodon.mamut.detection.stardist.StarDist;
import org.mastodon.mamut.detection.stardist.StarDistDetector;
import org.mastodon.tracking.mamut.detection.SpotDetectorOp;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotDetectorDescriptor.class, name = "StarDist spot detector configuration descriptor" )
public class StarDistDetectorDescriptor extends AbstractSpotDetectorDescriptor
{
	public static final String KEY_MODEL_TYPE = "starDistModelType";

	private JComboBox< StarDist.ModelType > modelTypeSelection;

	@Override
	protected void persistSettings()
	{
		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, this.modelTypeSelection.getSelectedItem() );
	}

	@Override
	protected void logSettings()
	{
		logger.info( String.format( "  - model type: %s%n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
	}

	@Override
	protected void getSettingsAndUpdateConfigPanel()
	{
		// Get the value.
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
	}

	@Override
	protected void grabSettings()
	{
		if ( null == settings )
			return;

		final Map< String, Object > detectorSettings = settings.values.getDetectorSettings();
		detectorSettings.put( KEY_MODEL_TYPE, modelTypeSelection.getSelectedItem() );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( StarDistDetector.class );
	}
}
