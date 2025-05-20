package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

import java.awt.Component;
import java.awt.Font;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.imagej.ops.OpService;
import net.miginfocom.swing.MigLayout;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.detection.StarDist;
import org.mastodon.mamut.detection.StarDistDetector;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin( type = SpotDetectorDescriptor.class, name = "StarDist spot detector configuration descriptor" )
public class StarDistDetectorDescriptor extends SpotDetectorDescriptor
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public final static String KEY_MODEL_TYPE = "starDistModelType";

	private Settings settings;

	private static final Icon PREVIEW_ICON =
			new ImageIcon( Objects.requireNonNull( Wizard.class.getResource( "led-icon-eye-green.png" ) ) );

	private ProjectModel appModel;

	private ViewerFrameMamut viewFrame;

	private final Model previewModel;

	private static final String IDENTIFIER = "Configure StarDist detector";

	@Parameter
	private OpService ops;

	public StarDistDetectorDescriptor()

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

		private final JComboBox< StarDist.ModelType > modelTypeSelection;

		private final JButton preview = new JButton( "Preview", PREVIEW_ICON );

		private final JLabel info;

		private ConfigPanel()
		{
			setLayout( new MigLayout( "wrap 1", "[grow]", "[]20[]" ) );

			JLabel headlineLabel = new JLabel( "Configure StarDist detector" );
			headlineLabel.setHorizontalAlignment( SwingConstants.LEFT );
			headlineLabel.setFont( getFont().deriveFont( Font.BOLD ) );
			add( headlineLabel, "growx" );

			JLabel modelTypeLabel = new JLabel( "Model type:" );
			add( modelTypeLabel, "align left, wrap" );
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
			add( modelTypeSelection, "align left, grow" );

			preview.addActionListener( ( e ) -> preview() );
			add( preview, "align right, wrap" );

			info = new JLabel( "", JLabel.RIGHT );
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
		final StarDist.ModelType modelType;
		final Object modelTypeObject = detectorSettings.get( KEY_MODEL_TYPE );
		if ( null == modelTypeObject )
			modelType = StarDist.ModelType.DEMO; // default
		else
			modelType = StarDist.ModelType.fromString( String.valueOf( modelTypeObject ) );

		// Show them in the config panel.
		final ConfigPanel panel = ( ConfigPanel ) targetPanel;
		panel.modelTypeSelection.setSelectedItem( modelType );
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
		detectorSettings.put( KEY_MODEL_TYPE, panel.modelTypeSelection.getSelectedItem() );

		logger.info( String.format( "  - model type: %s\n", settings.values.getDetectorSettings().get( KEY_MODEL_TYPE ) ) );
	}

	@Override
	public Collection< Class< ? extends SpotDetectorOp > > getTargetClasses()
	{
		return Collections.singleton( StarDistDetector.class );
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
		final SpotDetectorDescriptor.JLabelLogger previewLogger = new SpotDetectorDescriptor.JLabelLogger( panel.info );
		new Thread( () -> executePreview( currentTimepoint, previewLogger, panel ), "StarDist detector preview thread" ).start();

	}

	private void executePreview( final int currentTimepoint, final SpotDetectorDescriptor.JLabelLogger previewLogger,
			final ConfigPanel panel )
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
		detectorSettings.put( KEY_MODEL_TYPE, panel.modelTypeSelection.getSelectedItem() );
	}
}
