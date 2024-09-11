package org.mastodon.mamut.feature.dimensionalityreduction.umap.ui;

import net.miginfocom.swing.MigLayout;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapController;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapFeatureSettings;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * This class represents the user interface for performing dimensionality reduction using UMAP.
 * It provides various input fields and buttons to configure and execute the UMAP algorithm.
 * The UMAP algorithm is executed asynchronously to avoid blocking the user interface.
 * <br>
 * The input fields include:
 * <ul>
 *     <li>Radio buttons to select the type of graph (Spot or Branch)</li>
 *     <li>Check box to standardize features</li>
 *     <li>Spinners to set the number of dimensions, number of neighbors, and minimum distance</li>
 *     <li>Panel to select input dimensions</li>
 * </ul>
 */
public class UmapView extends JFrame
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final JPanel canvas;

	private final JRadioButton spotRadioButton;

	private final JRadioButton branchSpotRadioButton;

	private final JCheckBox standardizeFeaturesCheckBox;

	private final JSpinner numberOfDimensionsInput;

	private final JSpinner numberOfNeighborsInput;

	private final JSpinner minimumDistanceInput;

	private UmapInputDimensionsPanel< ?, ? > umapInputDimensionsPanel;

	private final JLabel feedbackLabel;

	private final JButton computeButton;

	private final ImageIcon loadingIcon;

	private final FeatureModel featureModel;

	private final UmapController umapController;

	private static final String UMAP_PANEL_CONSTRANTS = "span, growx, pushx, growy, pushy, wrap";

	/**
	 * Constructs a new UmapView with the specified model and context.
	 *
	 * @param model The model containing the data to be processed.
	 * @param context The context in which the UmapView is created.
	 */
	public UmapView( final Model model, final Context context )
	{
		this.featureModel = model.getFeatureModel();
		this.umapController = new UmapController( model, context );

		canvas = new JPanel( new MigLayout( "insets 0 10 0 10, fill", "", "" ) );
		setTitle( "Dimensionality Reduction using UMAP" );
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		setSize( 600, 600 );
		setLocationRelativeTo( null );

		spotRadioButton = new JRadioButton( Spot.class.getSimpleName() );
		branchSpotRadioButton = new JRadioButton( BranchSpot.class.getSimpleName() );

		standardizeFeaturesCheckBox = new JCheckBox( "Standardize features" );
		numberOfDimensionsInput = new JSpinner();
		numberOfNeighborsInput = new JSpinner();
		minimumDistanceInput = new JSpinner();
		umapInputDimensionsPanel = createUmapInputDimensionsPanel();
		feedbackLabel = new JLabel();
		computeButton = new JButton( "Compute UMAP" );
		loadingIcon = new ImageIcon( Objects.requireNonNull( getClass().getResource( "loading.gif" ) ) );

		initSettings();
		initBehavior();
		initLayout();
		initToolTips();
	}

	private void initSettings()
	{
		logger.debug( "Initializing UMAP settings." );
		boolean isSpotGraph = umapController.isSpotGraphPreferences();
		spotRadioButton.setSelected( isSpotGraph );
		branchSpotRadioButton.setSelected( !isSpotGraph );
		UmapFeatureSettings settings = umapController.getFeatureSettings();
		standardizeFeaturesCheckBox.setSelected( settings.isStandardizeFeatures() );
		numberOfDimensionsInput.setModel( getNumberOfDimensionsSpinnerModel() );
		numberOfNeighborsInput.setModel( getNumberOfNeighborsSpinnerModel() );
		minimumDistanceInput.setModel( new SpinnerNumberModel( settings.getMinimumDistance(), 0d, 1d, 0.1d ) );
	}

	private void initBehavior()
	{
		spotRadioButton.addActionListener( e -> updateUmapInputDimensionsPanel() );
		branchSpotRadioButton.addActionListener( e -> updateUmapInputDimensionsPanel() );

		ButtonGroup group = new ButtonGroup();
		group.add( spotRadioButton );
		group.add( branchSpotRadioButton );

		UmapFeatureSettings settings = umapController.getFeatureSettings();
		standardizeFeaturesCheckBox.addActionListener( e -> settings.setStandardizeFeatures( standardizeFeaturesCheckBox.isSelected() ) );
		numberOfDimensionsInput
				.addChangeListener( e -> settings.setNumberOfOutputDimensions( ( int ) numberOfDimensionsInput.getValue() ) );
		numberOfNeighborsInput.addChangeListener( e -> settings.setNumberOfNeighbors( ( int ) numberOfNeighborsInput.getValue() ) );
		minimumDistanceInput.addChangeListener( e -> settings.setMinimumDistance( ( double ) minimumDistanceInput.getValue() ) );
		computeButton.addActionListener( e -> SwingUtilities.invokeLater( this::run ) );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent e )
			{
				onWindowClosing();
			}
		} );
	}

	private void initLayout()
	{
		add( canvas, BorderLayout.CENTER );

		canvas.add( new JLabel( "Table:" ), "split 3" );
		canvas.add( spotRadioButton );
		canvas.add( branchSpotRadioButton, "wrap" );
		canvas.add( standardizeFeaturesCheckBox, "wrap" );
		String split2 = "split 2";
		canvas.add( new JLabel( "Number of dimensions:" ), split2 );
		canvas.add( numberOfDimensionsInput, "wmin 35, wrap" );
		canvas.add( new JLabel( "Number of neighbors:" ), split2 );
		canvas.add( numberOfNeighborsInput, "wmin 35, wrap" );
		canvas.add( new JLabel( "Minimum distance:" ), split2 );
		canvas.add( minimumDistanceInput, "wmin 40, wrap" );
		canvas.add( umapInputDimensionsPanel, UMAP_PANEL_CONSTRANTS );
		canvas.add( computeButton, "dock south, gapleft 10, gapbottom 10, wmax 150, wrap" );
		canvas.add( feedbackLabel, "dock south, gapleft 10, gapbottom 10, wrap" );
	}

	private void initToolTips()
	{
		numberOfDimensionsInput
				.setToolTipText( "<html>The number of reduced dimensions to use.<br>The default is 2, but 3 is also common.</html>" );
		numberOfNeighborsInput.setToolTipText(
				"<html>The size of the local neighborhood (in terms of number of neighboring sample points) used for manifold approximation."
						+ "<br>Larger values result in more global views of the manifold, while smaller values result in more local data being preserved."
						+ "<br>In general, it should be in the range 2 to 100.</html>" );
		minimumDistanceInput.setToolTipText(
				"<html>The minimum distance that points are allowed to be apart from each other in the low dimensional representation."
						+ "<br>This parameter controls how tightly UMAP is allowed to pack points together.</html>" );
		standardizeFeaturesCheckBox.setToolTipText(
				"<html>Whether to standardize the data before reducing the dimensionality."
						+ "<br>Standardization is recommended when the data has different scales / units.</html>" );
	}

	private void updateUmapInputDimensionsPanel()
	{
		canvas.remove( umapInputDimensionsPanel );
		boolean isSpotGraph = spotRadioButton.isSelected();
		umapController.setSpotGraph( isSpotGraph );
		umapInputDimensionsPanel = createUmapInputDimensionsPanel();
		canvas.add( umapInputDimensionsPanel, UMAP_PANEL_CONSTRANTS );
		revalidate();
		repaint();
		numberOfDimensionsInput.setModel( getNumberOfDimensionsSpinnerModel() );
	}

	private < V extends Vertex< E >, E extends Edge< V > > UmapInputDimensionsPanel< V, E > createUmapInputDimensionsPanel()
	{
		final Class< V > vertexType = umapController.getVertexType();
		final Class< E > edgeType = umapController.getEdgeType();
		return new UmapInputDimensionsPanel<>( vertexType, edgeType, featureModel );
	}

	private SpinnerModel getNumberOfDimensionsSpinnerModel()
	{
		int maximumNumberOfDimensions = Math.max( 2, umapInputDimensionsPanel.getNumberOfFeatures() );
		int numberOfDimensions = Math.min( umapController.getFeatureSettings().getNumberOfOutputDimensions(), maximumNumberOfDimensions );
		return new SpinnerNumberModel( numberOfDimensions, UmapFeatureSettings.DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS,
				maximumNumberOfDimensions, 1 );
	}

	private SpinnerModel getNumberOfNeighborsSpinnerModel()
	{
		return new SpinnerNumberModel( umapController.getFeatureSettings().getNumberOfNeighbors(), 1, 100, 1 );
	}

	// Method called when the JFrame is closed
	private void onWindowClosing()
	{
		// Perform any cleanup or actions needed before closing the window
		logger.debug( "UmapView is closing." );
		umapController.saveSettingsToPreferences();
		// Dispose the window
		dispose();
	}

	private void run()
	{
		beforeRun();
		executeAsynchronously();
	}

	private void beforeRun()
	{
		computeButton.setEnabled( false );
		computeButton.setIcon( loadingIcon );
		feedbackLabel.setText( "Computing UMAP..." );
		feedbackLabel.setForeground( Color.BLACK );
		repaint();
	}

	private void executeAsynchronously()
	{
		SwingWorker< Void, Void > worker = new SwingWorker< Void, Void >()
		{
			@Override
			protected Void doInBackground()
			{
				umapController.computeFeature( umapInputDimensionsPanel );
				return null;
			}

			@Override
			protected void done()
			{
				try
				{
					get();
					afterSuccessfulRun();
				}
				catch ( Exception e )
				{
					logger.error( "Running umap failed. {}", e.getCause().getMessage(), e );
					afterFailedRun( e.getCause().getMessage() );
					Thread.currentThread().interrupt();
				}
				finally
				{
					computeButton.setEnabled( true );
				}
			}
		};
		worker.execute();
	}

	private void afterFailedRun( final String message )
	{
		feedbackLabel.setForeground( Color.RED );
		feedbackLabel.setText( message );
		repaint();
	}

	private void afterSuccessfulRun()
	{
		computeButton.setIcon( null );
		feedbackLabel.setForeground( new Color( 0, 100, 0 ) );
		feedbackLabel.setText( "UMAP sucessfully computed." );
		repaint();
	}
}
