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
import org.mastodon.mamut.linking.trackastra.TrackastraMode;
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

import bdv.viewer.Source;

@Plugin( type = SpotLinkerDescriptor.class, name = "Trackastra Linker Descriptor" )
public class TrackastraLinkerDescriptor extends SpotLinkerDescriptor
{

	public static final String IDENTIFIER = "Configure Trackastra linker";

	public static final String KEY_EDGE_THRESHOLD = "trackastraEdgeThreshold";

	public static final String KEY_TRACKASTRA_MODE = "trackastraMode";

	public static final String KEY_NUM_DIMENSIONS = "trackastraNumDimensions";

	public static final String KEY_NUM_SOURCES = "trackastraNumSources";

	public static final String KEY_SOURCE = "trackastraSource";

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

		private final JComboBox< TrackastraMode > modeComboBox;

		private final SetupIDComboBox sourcesComboBox;

		private final JLabel levelDesc;

		private final JSpinner levelSpinner;

		public ParameterPanel()
		{
			edgeThreshold = new JSpinner( new SpinnerNumberModel( 0.05, 0.0, 1.0, 0.01 ) );
			modeComboBox = new JComboBox<>( TrackastraMode.values() );
			sourcesComboBox = new SetupIDComboBox( settings.values.getSources() );
			levelDesc = new JLabel( getLevelText( sourcesComboBox.getSelectedSetupID() ) );
			levelSpinner = new JSpinner( new SpinnerNumberModel( 0, 0, 0, 1 ) );
			initBehaviour();
			initLayout();
		}

		private void initLayout()
		{
			MigLayout layout = new MigLayout( "insets 5, gapx 5, gapy 2", "[left][pref!]", "[]" );
			setLayout( layout );

			JLabel header = new JLabel( "<html><b>Configure<br>Trackastra Linker Settings</b></html>" );
			add( header, "span 2, wrap, gaptop 0, gapbottom 10" );

			JLabel edgeLabel = new JLabel( "Link threshold:" );
			edgeLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			edgeThreshold.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			JLabel edgeDesc = new JLabel( "<html>Higher values result in less created links.</html>" );
			edgeDesc.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );

			add( edgeLabel, "wrap" );
			add( edgeThreshold, "wrap" );
			add( edgeDesc, "span 2, wrap, gapbottom 10" );

			JLabel modeLabel = new JLabel( "Mode:" );
			modeLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			modeComboBox.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			JLabel modeDesc = new JLabel( "<html>Select the algorithm mode.<br>Options: greedy with or without divisions.</html>" );
			modeDesc.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );

			add( modeLabel, "wrap" );
			add( modeComboBox, "wrap" );
			add( modeDesc, "span 2, wrap, gapbottom 10" );

			JLabel sourceLabel = new JLabel( "Source:" );
			sourceLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			sourcesComboBox.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( sourceLabel, "wrap" );
			add( sourcesComboBox, "wrap, gapbottom 10" );

			JLabel levelLabel = new JLabel( "Resolution level:" );
			levelLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			levelDesc.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			levelSpinner.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( levelLabel, "wrap" );
			add( levelSpinner, "wrap" );
			add( levelDesc, "span 2" );
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
				levelDesc.setText( getLevelText( levels ) );
			} );
		}

		private Map< String, Object > getSettings()
		{
			final Map< String, Object > linkerSettings = new HashMap<>( settings.values.getLinkerSettings() );

			linkerSettings.put( DetectorKeys.KEY_SETUP_ID, sourcesComboBox.getSelectedSetupID() );
			Source< ? > source = settings.values.getSources().get( sourcesComboBox.getSelectedSetupID() ).getSpimSource();
			linkerSettings.put( KEY_SOURCE, source );
			int dimensions = source.getVoxelDimensions().dimensionsAsDoubleArray().length;
			linkerSettings.put( KEY_NUM_DIMENSIONS, dimensions );
			linkerSettings.put( KEY_EDGE_THRESHOLD, edgeThreshold.getValue() );
			linkerSettings.put( KEY_TRACKASTRA_MODE, modeComboBox.getSelectedItem() );
			linkerSettings.put( KEY_LEVEL, levelSpinner.getValue() );
			linkerSettings.put( KEY_NUM_SOURCES, settings.values.getSources().size() );

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
		return "<html>0 ... highest (slower, more accurate)<br>" + maxLevels
				+ " ... (faster, less accurate)</html>";
	}
}
