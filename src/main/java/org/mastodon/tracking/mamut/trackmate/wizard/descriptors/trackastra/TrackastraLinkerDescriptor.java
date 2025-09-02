package org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra;

import static org.mastodon.mamut.detection.DeepLearningDetectorKeys.KEY_LEVEL;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MAX_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_MIN_TIMEPOINT;
import static org.mastodon.tracking.detection.DetectorKeys.KEY_SETUP_ID;
import static org.mastodon.tracking.linking.LinkerKeys.KEY_DO_LINK_SELECTION;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.linking.trackastra.TrackastraLinkerMamut;
import org.mastodon.mamut.linking.trackastra.TrackAstraMode;
import org.mastodon.tracking.detection.DetectorKeys;
import org.mastodon.tracking.mamut.linking.SpotLinkerOp;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.descriptors.SpotLinkerDescriptor;
import org.mastodon.tracking.mamut.trackmate.wizard.util.SelectOnFocusListener;
import org.mastodon.ui.util.SetupIDComboBox;
import org.scijava.log.LogLevel;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

@Plugin( type = SpotLinkerDescriptor.class, name = "Trackastra Linker Descriptor" )
public class TrackastraLinkerDescriptor extends SpotLinkerDescriptor
{

	public static final String IDENTIFIER = "Configure Trackastra linker";

	public static final String KEY_EDGE_THRESHOLD = "trackastraEdgeThreshold";

	public static final String KEY_TRACKASTRA_MODE = "trackastraMode";

	public static final String KEY_NUM_DIMENSIONS = "trackastraNumDimensions";

	@Parameter
	private PluginService pluginService;

	private Settings settings;

	public TrackastraLinkerDescriptor()
	{
		this.panelIdentifier = IDENTIFIER;
	}

	@Override
	public void aboutToDisplayPanel()
	{
		final ParameterPanel panel = ( ParameterPanel ) targetPanel;
		panel.loadSettings( settings.values.getLinkerSettings() );
	}

	@Override
	public void aboutToHidePanel()
	{
		final ParameterPanel panel = ( ParameterPanel ) targetPanel;
		final Map< String, Object > linkerSettings = panel.getSettings();
		settings.linkerSettings( linkerSettings );

		logger.log( LogLevel.INFO, "Configured Trackastra linker with the following parameters:\n" );
		logger.log( LogLevel.INFO, String.format( "  - source: %d%n", ( int ) linkerSettings.get( DetectorKeys.KEY_SETUP_ID ) ) );
		logger.log( LogLevel.INFO, String.format( "  - resolution level: %d%n", ( int ) linkerSettings.get( KEY_LEVEL ) ) );
		logger.log( LogLevel.INFO, String.format( "  - edge threshold: %s%n", linkerSettings.get( KEY_EDGE_THRESHOLD ) ) );
		logger.log( LogLevel.INFO, String.format( "  - mode: %s%n", linkerSettings.get( KEY_TRACKASTRA_MODE ) ) );
		logger.log( LogLevel.INFO, String.format( "  - num dimensions: %d%n", ( int ) linkerSettings.get( KEY_NUM_DIMENSIONS ) ) );
		logger.log( LogLevel.INFO, String.format( "  - min time-point: %d%n", ( int ) linkerSettings.get( KEY_MIN_TIMEPOINT ) ) );
		logger.log( LogLevel.INFO, String.format( "  - max time-point: %d%n", ( int ) linkerSettings.get( KEY_MAX_TIMEPOINT ) ) );
		logger.log( LogLevel.INFO, String.format( "  - target: %s%n",
				( boolean ) linkerSettings.get( KEY_DO_LINK_SELECTION ) ? "selection only." : "all detections." ) );
	}

	@Override
	public Collection< Class< ? extends SpotLinkerOp > > getTargetClasses()
	{
		return Collections.singleton( TrackastraLinkerMamut.class );
	}

	@Override
	public void setTrackMate( final TrackMate trackmate )
	{
		this.settings = trackmate.getSettings();
		this.targetPanel = new ParameterPanel();
	}

	@Override
	public void setWindowManager( final WindowManager windowManager )
	{
		// NB: not used
	}

	private class ParameterPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JSpinner edgeThreshold;

		private final JComboBox< TrackAstraMode > modeComboBox;

		private final SetupIDComboBox sourcesComboBox;

		private final JLabel levelLabel;

		private final JSpinner levelSpinner;

		public ParameterPanel()
		{
			edgeThreshold = new JSpinner( new SpinnerNumberModel( 0.05, 0.0, 1.0, 0.01 ) );
			modeComboBox = new JComboBox<>( TrackAstraMode.values() );
			sourcesComboBox = new SetupIDComboBox( settings.values.getSources() );
			levelLabel = new JLabel( getLevelText( sourcesComboBox.getSelectedSetupID() ) );
			levelSpinner = new JSpinner( new SpinnerNumberModel( 0, 0, 0, 1 ) );
			initBehaviour();
			initLayout();
		}

		private void initLayout()
		{
			MigLayout layout = new MigLayout( "", "[left]5[grow,fill]", "[]10[]10[]10[]" );
			setLayout( layout );

			JLabel header = new JLabel( "<html><h3>Configure Track Astra Linker Settings</h3></html>" );
			add( header, "span, wrap, gaptop 10, gapbottom 15" );

			JLabel edgeLabel = new JLabel( "Edge threshold:" );
			edgeLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			edgeThreshold.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			JLabel edgeDesc = new JLabel( "<html>Controls the edge detection threshold.<br>Value must be between 0 and 1.</html>" );

			add( edgeLabel, "wrap" );
			add( edgeThreshold, "wrap" );
			add( edgeDesc, "wrap, gapbottom 15" );

			JLabel modeLabel = new JLabel( "Mode:" );
			modeLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			modeComboBox.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			JLabel modeDesc = new JLabel( "<html>Select the algorithm mode.<br>Options: greedy or greedy_nodiv.</html>" );

			add( modeLabel, "wrap" );
			add( modeComboBox, "wrap" );
			add( modeDesc, "wrap" );

			JLabel sourceLabel = new JLabel( "Source:" );
			sourceLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			sourcesComboBox.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( sourceLabel, "wrap" );
			add( sourcesComboBox, "wrap" );

			levelLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			levelSpinner.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( levelLabel, "wrap" );
			add( levelSpinner, "wrap" );
		}

		private void initBehaviour()
		{
			final SelectOnFocusListener onFocusListener = new SelectOnFocusListener();

			edgeThreshold.addFocusListener( onFocusListener );
			modeComboBox.addFocusListener( onFocusListener );
			sourcesComboBox.addFocusListener( onFocusListener );

			sourcesComboBox.addActionListener( e -> {
				int levels = settings.values.getSources().get( sourcesComboBox.getSelectedIndex() ).getSpimSource().getNumMipmapLevels();
				( ( SpinnerNumberModel ) levelSpinner.getModel() ).setMaximum( levels - 1 );
				levelLabel.setText( getLevelText( levels ) );
			} );
		}

		private Map< String, Object > getSettings()
		{
			final Map< String, Object > linkerSettings = new HashMap<>( settings.values.getLinkerSettings() );

			linkerSettings.put( DetectorKeys.KEY_SETUP_ID, sourcesComboBox.getSelectedSetupID() );
			int dimensions = settings.values.getSources().get( sourcesComboBox.getSelectedSetupID() ).getSpimSource().getVoxelDimensions()
					.dimensionsAsDoubleArray().length;
			linkerSettings.put( KEY_NUM_DIMENSIONS, dimensions );
			linkerSettings.put( KEY_EDGE_THRESHOLD, edgeThreshold.getValue() );
			linkerSettings.put( KEY_TRACKASTRA_MODE, modeComboBox.getSelectedItem() );
			linkerSettings.put( KEY_LEVEL, levelSpinner.getValue() );

			return linkerSettings;
		}

		private void loadSettings( final Map< String, Object > linkerSettings )
		{
			sourcesComboBox.setSelectedSetupID( ( int ) linkerSettings.get( KEY_SETUP_ID ) );
			edgeThreshold.setValue( linkerSettings.get( KEY_EDGE_THRESHOLD ) );
			modeComboBox.setSelectedItem( linkerSettings.get( KEY_TRACKASTRA_MODE ) );
			levelSpinner.setValue( linkerSettings.get( KEY_LEVEL ) );
		}
	}

	private static String getLevelText( final int maxLevels )
	{
		return "<html>Resolution level:<br>0 ... highest (slower, more accurate)<br>" + maxLevels
				+ " ... (faster, less accurate)</html>";
	}
}
