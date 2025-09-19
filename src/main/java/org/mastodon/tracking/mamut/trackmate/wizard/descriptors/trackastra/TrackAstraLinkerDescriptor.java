package org.mastodon.tracking.mamut.trackmate.wizard.descriptors.trackastra;

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
import org.mastodon.mamut.linking.trackastra.TrackAstraLinkerMamut;
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

@Plugin( type = SpotLinkerDescriptor.class, name = "Track Astra Linker Descriptor" )
public class TrackAstraLinkerDescriptor extends SpotLinkerDescriptor
{

	public static final String IDENTIFIER = "Configure Track Astra linker";

	public static final String KEY_EDGE_THRESHOLD = "trackAstraEdgeThreshold";

	public static final String KEY_TRACKASTRA_MODE = "trackAstraMode";

	public static final String KEY_NUM_DIMENSIONS = "trackAstraNumDimensions";

	@Parameter
	private PluginService pluginService;

	private Settings settings;

	public TrackAstraLinkerDescriptor()
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
		return Collections.singleton( TrackAstraLinkerMamut.class );
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

		private final JSpinner edgeSpinner;

		private final JComboBox< TrackAstraMode > modeCombo;

		private final SetupIDComboBox sourcesCombo;

		private final JLabel levelLabel;

		private final JSpinner level;

		public ParameterPanel()
		{
			edgeSpinner = new JSpinner( new SpinnerNumberModel( 0.05, 0.0, 1.0, 0.01 ) );
			modeCombo = new JComboBox<>( TrackAstraMode.values() );
			sourcesCombo = new SetupIDComboBox( settings.values.getSources() );
			levelLabel = new JLabel( getLevelText( sourcesCombo.getSelectedSetupID() ) );
			level = new JSpinner( new SpinnerNumberModel( 0, 0, 0, 1 ) );
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
			edgeSpinner.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			JLabel edgeDesc = new JLabel( "<html>Controls the edge detection threshold.<br>Value must be between 0 and 1.</html>" );

			add( edgeLabel, "wrap" );
			add( edgeSpinner, "wrap" );
			add( edgeDesc, "wrap, gapbottom 15" );

			JLabel modeLabel = new JLabel( "Mode:" );
			modeLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			modeCombo.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			JLabel modeDesc = new JLabel( "<html>Select the algorithm mode.<br>Options: greedy or greedy_nodiv.</html>" );

			add( modeLabel, "wrap" );
			add( modeCombo, "wrap" );
			add( modeDesc, "wrap" );

			JLabel sourceLabel = new JLabel( "Source:" );
			sourceLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			sourcesCombo.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( sourceLabel, "wrap" );
			add( sourcesCombo, "wrap" );

			levelLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			level.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( levelLabel, "wrap" );
			add( level, "wrap" );
		}

		private void initBehaviour()
		{
			final SelectOnFocusListener onFocusListener = new SelectOnFocusListener();

			edgeSpinner.addFocusListener( onFocusListener );
			modeCombo.addFocusListener( onFocusListener );
			sourcesCombo.addFocusListener( onFocusListener );

			sourcesCombo.addActionListener( e -> {
				int levels = settings.values.getSources().get( sourcesCombo.getSelectedIndex() ).getSpimSource().getNumMipmapLevels();
				( ( SpinnerNumberModel ) level.getModel() ).setMaximum( levels - 1 );
				levelLabel.setText( getLevelText( levels ) );
			} );
		}

		private Map< String, Object > getSettings()
		{
			final Map< String, Object > linkerSettings = new HashMap<>( settings.values.getLinkerSettings() );

			linkerSettings.put( DetectorKeys.KEY_SETUP_ID, sourcesCombo.getSelectedSetupID() );
			int dimensions = settings.values.getSources().get( sourcesCombo.getSelectedSetupID() ).getSpimSource().getVoxelDimensions()
					.dimensionsAsDoubleArray().length;
			linkerSettings.put( KEY_NUM_DIMENSIONS, dimensions );
			linkerSettings.put( KEY_EDGE_THRESHOLD, edgeSpinner.getValue() );
			linkerSettings.put( KEY_TRACKASTRA_MODE, modeCombo.getSelectedItem() );

			return linkerSettings;
		}

		private void loadSettings( final Map< String, Object > linkerSettings )
		{
			sourcesCombo.setSelectedSetupID( ( int ) linkerSettings.get( KEY_SETUP_ID ) );
			edgeSpinner.setValue( linkerSettings.get( KEY_EDGE_THRESHOLD ) );
			modeCombo.setSelectedItem( linkerSettings.get( KEY_TRACKASTRA_MODE ) );
		}
	}

	private static String getLevelText( final int maxLevels )
	{
		return "<html>Resolution level:<br>0 ... highest (slower, more accurate)<br>" + maxLevels
				+ " ... (faster, less accurate)</html>";
	}
}
