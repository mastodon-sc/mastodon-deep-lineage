package org.mastodon.tracking.mamut.trackmate.wizard.descriptors;

import java.awt.Font;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.imagej.ops.OpService;
import net.miginfocom.swing.MigLayout;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.mamut.trackmate.Settings;
import org.mastodon.tracking.mamut.trackmate.TrackMate;
import org.mastodon.tracking.mamut.trackmate.wizard.Wizard;
import org.mastodon.tracking.mamut.trackmate.wizard.util.WizardUtils;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.ViewerFrameMamut;
import org.scijava.plugin.Parameter;

/**
 * A class that contains the common logic for the spot detectors in this package.
 */
public abstract class AbstractSpotDetectorDescriptor extends SpotDetectorDescriptor
{

	protected Settings settings;

	private ProjectModel appModel;

	private ViewerFrameMamut viewFrame;

	private final Model previewModel;

	@Parameter
	private OpService ops;

	private static final Icon PREVIEW_ICON =
			new ImageIcon( Objects.requireNonNull( Wizard.class.getResource( "led-icon-eye-green.png" ) ) );

	protected AbstractSpotDetectorDescriptor()
	{
		this.panelIdentifier = "Configure " + getDetectorName() + " detector";
		this.targetPanel = new ConfigPanel();
		this.previewModel = new Model();
	}

	protected abstract void persistSettings();

	protected abstract void logSettings();

	protected abstract void getSettingsAndUpdateConfigPanel();

	protected abstract String getDetectorName();

	protected abstract void configureDetectorSpecificFields( final ConfigPanel panel );

	/**
	 * Update the settings field of this descriptor with the values set on the GUI by the user.
	 */
	protected abstract void grabSettings();

	@Override
	public void setAppModel( final ProjectModel appModel )
	{
		this.appModel = appModel;
	}

	@Override
	public void aboutToHidePanel()
	{
		if ( null != viewFrame )
			viewFrame.dispose();
		viewFrame = null;

		if ( null == settings )
			return;

		persistSettings();
		logSettings();
	}

	@Override
	public void setTrackMate( final TrackMate trackmate )
	{
		// This method is called when the user 'enters' the config panel.
		// Get the settings map to configure. It is updated with the values set by the user on the panel when they 'leave' the panel, cf. aboutToHidePanel().
		this.settings = trackmate.getSettings();

		if ( null == settings )
			return;

		// To display the detector settings values, either the default ones, or the one that were set previously, read these values from the TrackMate instance.
		getSettingsAndUpdateConfigPanel();
	}

	protected class ConfigPanel extends JPanel
	{
		private final JButton preview = new JButton( "Preview", PREVIEW_ICON );

		private final JLabel info = new JLabel( "", SwingConstants.RIGHT );

		protected ConfigPanel()
		{
			setLayout( new MigLayout( "wrap 1", "[grow]", "[]20[]" ) );

			JLabel headlineLabel = new JLabel( "Configure " + getDetectorName() + " detector" );
			headlineLabel.setHorizontalAlignment( SwingConstants.LEFT );
			headlineLabel.setFont( getFont().deriveFont( Font.BOLD ) );
			add( headlineLabel, "growx" );

			configureDetectorSpecificFields( this );

			preview.addActionListener( e -> preview() );
			add( preview, "align right, wrap" );

			info.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
			add( info, "align right, wrap" );
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
			new Thread( () -> executePreview( currentTimepoint, previewLogger, panel ), "Spot detector preview thread" ).start();
		}

		private void executePreview( final int currentTimepoint, final JLabelLogger previewLogger, final ConfigPanel panel )
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
	}
}
