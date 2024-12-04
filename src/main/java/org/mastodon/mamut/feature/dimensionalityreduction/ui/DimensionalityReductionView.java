/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature.dimensionalityreduction.ui;

import net.imglib2.util.Cast;
import net.miginfocom.swing.MigLayout;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionAlgorithm;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionController;
import org.mastodon.mamut.feature.dimensionalityreduction.CommonSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.tsne.TSneSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapSettings;
import org.mastodon.mamut.model.Model;
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
 * This class represents the user interface for performing dimensionality reduction using different algorithms.
 * It provides various input fields and buttons to configure and execute the selected algorithm.
 * The selected algorithm is executed asynchronously to avoid blocking the user interface.
 * <br>
 */
public class DimensionalityReductionView extends JFrame
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static final String SPLIT_2 = "split 2";

	public static final String WMIN_35_WRAP = "wmin 35, wrap";

	private final JPanel canvas;

	private final JRadioButton modelGraphRadioButton;

	private final JRadioButton branchGraphRadioButton;

	private final JRadioButton umapRadioButton;

	private final JRadioButton tsneRadioButton;

	private final JRadioButton pcaRadioButton;

	private final JCheckBox standardizeFeaturesCheckBox;

	private final JSpinner numberOfDimensionsInput;

	private final JSpinner numberOfNeighborsInput;

	private final JSpinner minimumDistanceInput;

	private final JSpinner perplexityInput;

	private final JSpinner maxIterationsInput;

	private final JPanel algorithmSpecificSettingsPanel;

	private InputDimensionsPanel< ?, ? > inputDimensionsPanel;

	private final JLabel feedbackLabel;

	private final JButton computeButton;

	private final ImageIcon loadingIcon;

	private final FeatureModel featureModel;

	private final DimensionalityReductionController controller;

	private static final String PANEL_CONSTRAINTS = "span, growx, pushx, growy, pushy, wrap";

	private static final int MINIMUM_NUMBER_OF_DIMENSIONS = 1;

	public DimensionalityReductionView( final Model model, final Context context )
	{
		this.featureModel = model.getFeatureModel();
		this.controller = new DimensionalityReductionController( model, context );

		canvas = new JPanel( new MigLayout( "insets 0 10 0 10, fill", "", "" ) );
		setTitle( "Dimensionality Reduction" );
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		setSize( 600, 600 );
		setLocationRelativeTo( null );

		modelGraphRadioButton = new JRadioButton( "Model Graph Features" );
		branchGraphRadioButton = new JRadioButton( "Branch Graph Features" );

		umapRadioButton = new JRadioButton( "UMAP" );
		tsneRadioButton = new JRadioButton( "t-SNE" );
		pcaRadioButton = new JRadioButton( "PCA" );

		// Common settings
		standardizeFeaturesCheckBox = new JCheckBox( "Standardize features" );
		numberOfDimensionsInput = new JSpinner();
		// UMAP settings
		numberOfNeighborsInput = new JSpinner();
		minimumDistanceInput = new JSpinner();
		// t-SNE settings
		perplexityInput = new JSpinner();
		maxIterationsInput = new JSpinner();

		algorithmSpecificSettingsPanel = new JPanel( new MigLayout( "insets 0 0 0 0, fill", "", "" ) );
		addAlgorithmSpecificSettings( controller.getAlgorithm() );
		inputDimensionsPanel = createInputDimensionsPanel();
		feedbackLabel = new JLabel();
		computeButton = new JButton( "Compute" );
		loadingIcon = new ImageIcon( Objects.requireNonNull( getClass().getResource( "loading.gif" ) ) );

		initSettings();
		initBehavior();
		initLayout();
		initToolTips();
	}

	private void initSettings()
	{
		logger.debug( "Initializing dimensionality reduction settings." );
		boolean isModelGraph = controller.isModelGraphPreferences();
		modelGraphRadioButton.setSelected( isModelGraph );
		branchGraphRadioButton.setSelected( !isModelGraph );
		DimensionalityReductionAlgorithm algorithm = controller.getAlgorithm();
		umapRadioButton.setSelected( algorithm == DimensionalityReductionAlgorithm.UMAP );
		tsneRadioButton.setSelected( algorithm == DimensionalityReductionAlgorithm.TSNE );
		pcaRadioButton.setSelected( algorithm == DimensionalityReductionAlgorithm.PCA );
		CommonSettings settings = controller.getCommonSettings();
		UmapSettings umapSettings = controller.getUmapSettings();
		TSneSettings tSneSettings = controller.getTSneSettings();
		standardizeFeaturesCheckBox.setSelected( settings.isStandardizeFeatures() );
		numberOfDimensionsInput.setModel( getNumberOfDimensionsSpinnerModel() );
		numberOfNeighborsInput.setModel( new SpinnerNumberModel( controller.getUmapSettings().getNumberOfNeighbors(),
				UmapSettings.MIN_VALUE_NUMBER_OF_NEIGHBORS, UmapSettings.MAX_VALUE_NUMBER_OF_NEIGHBORS, 1 ) );
		minimumDistanceInput.setModel( new SpinnerNumberModel( umapSettings.getMinimumDistance(), UmapSettings.MIN_VALUE_MINIMUM_DISTANCE,
				UmapSettings.MAX_VALUE_MINIMUM_DISTANCE, 0.1d ) );
		perplexityInput.setModel( new SpinnerNumberModel( tSneSettings.getPerplexity(), TSneSettings.MIN_VALUE_PERPLEXITY,
				TSneSettings.MAX_VALUE_PERPLEXITY, 1 ) );
		maxIterationsInput.setModel( new SpinnerNumberModel( tSneSettings.getMaxIterations(), TSneSettings.MIN_VALUE_MAX_ITERATIONS,
				TSneSettings.MAX_VALUE_MAX_ITERATIONS, 1 ) );
	}

	private void initBehavior()
	{
		modelGraphRadioButton.addActionListener( e -> updateInputDimensionsPanel() );
		branchGraphRadioButton.addActionListener( e -> updateInputDimensionsPanel() );

		ButtonGroup graphGroup = new ButtonGroup();
		graphGroup.add( modelGraphRadioButton );
		graphGroup.add( branchGraphRadioButton );

		umapRadioButton.addActionListener( e -> updateAlgorithmSettings() );
		tsneRadioButton.addActionListener( e -> updateAlgorithmSettings() );
		pcaRadioButton.addActionListener( e -> updateAlgorithmSettings() );

		ButtonGroup algorithmGroup = new ButtonGroup();
		algorithmGroup.add( umapRadioButton );
		algorithmGroup.add( tsneRadioButton );
		algorithmGroup.add( pcaRadioButton );

		CommonSettings commonSettings = controller.getCommonSettings();
		UmapSettings umapSettings = controller.getUmapSettings();
		TSneSettings tsneSettings = controller.getTSneSettings();
		standardizeFeaturesCheckBox
				.addActionListener( e -> commonSettings.setStandardizeFeatures( standardizeFeaturesCheckBox.isSelected() ) );
		numberOfDimensionsInput
				.addChangeListener( e -> commonSettings.setNumberOfOutputDimensions( ( int ) numberOfDimensionsInput.getValue() ) );
		numberOfNeighborsInput.addChangeListener( e -> umapSettings.setNumberOfNeighbors( ( int ) numberOfNeighborsInput.getValue() ) );
		minimumDistanceInput.addChangeListener( e -> umapSettings.setMinimumDistance( ( double ) minimumDistanceInput.getValue() ) );
		perplexityInput.addChangeListener( e -> tsneSettings.setPerplexity( ( int ) perplexityInput.getValue() ) );
		maxIterationsInput.addChangeListener( e -> tsneSettings.setMaxIterations( ( int ) maxIterationsInput.getValue() ) );
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

		canvas.add( new JLabel( "Graph type:" ), "split 4" );
		canvas.add( modelGraphRadioButton );
		canvas.add( branchGraphRadioButton, "wrap" );
		canvas.add( new JLabel( "Algorithm:" ), "split 4" );
		canvas.add( umapRadioButton );
		canvas.add( tsneRadioButton );
		canvas.add( pcaRadioButton, "wrap" );
		canvas.add( standardizeFeaturesCheckBox, "wrap" );
		canvas.add( new JLabel( "Number of dimensions:" ), SPLIT_2 );
		canvas.add( numberOfDimensionsInput, WMIN_35_WRAP );
		canvas.add( algorithmSpecificSettingsPanel, "wrap" );
		canvas.add( inputDimensionsPanel, PANEL_CONSTRAINTS );
		canvas.add( computeButton, "dock south, gapleft 10, gapbottom 10, wmax 150, wrap" );
		canvas.add( feedbackLabel, "dock south, gapleft 10, gapbottom 10, wrap" );
	}

	private void initToolTips()
	{
		standardizeFeaturesCheckBox.setToolTipText(
				"<html>Whether to standardize the data before reducing the dimensionality."
						+ "<br>Standardization is recommended when the data has different scales / units.</html>" );
		numberOfDimensionsInput
				.setToolTipText( "<html>The number of reduced dimensions to use.<br>The default is 2, but 3 is also common.</html>" );
		numberOfNeighborsInput.setToolTipText(
				"<html>The size of the local neighborhood (in terms of number of neighboring sample points) used for manifold approximation."
						+ "<br>Larger values result in more global views of the manifold, while smaller values result in more local data being preserved."
						+ "<br>In general, it should be in the range 2 to 100.</html>" );
		minimumDistanceInput.setToolTipText(
				"<html>The minimum distance that points are allowed to be apart from each other in the low dimensional representation."
						+ "<br>This parameter controls how tightly UMAP is allowed to pack points together.</html>" );
		perplexityInput.setToolTipText(
				"<html>The perplexity is related to the number of nearest neighbors that is used in other manifold learning algorithms. Larger datasets usually require a larger perplexity.<br>"
						+ "Consider selecting a value between 5 and 50. Different values can result in significantly different results.<br>"
						+ "The perplexity must be less than the number of samples.</html>" );
		maxIterationsInput.setToolTipText( "<html>The maximum number of iterations for the optimization.<br>"
				+ "The optimization algorithm will stop when the maximum number of iterations is reached.<br>"
				+ "Should be at least 250. More iterations will give more accurate results, but will also take longer to compute</html>" );
	}

	private void updateInputDimensionsPanel()
	{
		logger.debug( "Updating input dimensions panel." );
		canvas.remove( inputDimensionsPanel );
		controller.setModelGraph( modelGraphRadioButton.isSelected() );
		inputDimensionsPanel = createInputDimensionsPanel();
		canvas.add( inputDimensionsPanel, PANEL_CONSTRAINTS );
		revalidate();
		repaint();
		numberOfDimensionsInput.setModel( getNumberOfDimensionsSpinnerModel() );
	}

	private void updateAlgorithmSettings()
	{
		algorithmSpecificSettingsPanel.removeAll();
		DimensionalityReductionAlgorithm algorithm;
		if ( umapRadioButton.isSelected() )
			algorithm = DimensionalityReductionAlgorithm.UMAP;
		else if ( tsneRadioButton.isSelected() )
			algorithm = DimensionalityReductionAlgorithm.TSNE;
		else
			algorithm = DimensionalityReductionAlgorithm.PCA;
		controller.setAlgorithm( algorithm );
		addAlgorithmSpecificSettings( algorithm );
		revalidate();
		repaint();
	}

	private void addAlgorithmSpecificSettings( final DimensionalityReductionAlgorithm algorithm )
	{
		logger.debug( "Adding algorithm specific settings for {}.", algorithm );
		switch ( algorithm )
		{
		case UMAP:
			algorithmSpecificSettingsPanel.add( new JLabel( "Number of neighbors:" ), SPLIT_2 );
			algorithmSpecificSettingsPanel.add( numberOfNeighborsInput, WMIN_35_WRAP );
			algorithmSpecificSettingsPanel.add( new JLabel( "Minimum distance:" ), SPLIT_2 );
			algorithmSpecificSettingsPanel.add( minimumDistanceInput, "wmin 40, wrap" );
			break;
		case TSNE:
			algorithmSpecificSettingsPanel.add( new JLabel( "Perplexity:" ), SPLIT_2 );
			algorithmSpecificSettingsPanel.add( perplexityInput, WMIN_35_WRAP );
			algorithmSpecificSettingsPanel.add( new JLabel( "Maximum number of iterations:" ), SPLIT_2 );
			algorithmSpecificSettingsPanel.add( maxIterationsInput, WMIN_35_WRAP );
			break;
		case PCA:
			algorithmSpecificSettingsPanel.add( new JLabel( "" ), "wrap" );
			algorithmSpecificSettingsPanel.add( new JLabel( "" ), "wrap" );
			break;
		default:
			break;
		}
	}

	private < V extends Vertex< E >, E extends Edge< V > > InputDimensionsPanel< V, E > createInputDimensionsPanel()
	{
		return new InputDimensionsPanel<>( Cast.unchecked( controller.getVertexType() ),
				Cast.unchecked( controller.getEdgeType() ), featureModel );
	}

	private SpinnerModel getNumberOfDimensionsSpinnerModel()
	{
		int maximumNumberOfDimensions = Math.max( 2, inputDimensionsPanel.getNumberOfFeatures() );
		int numberOfDimensions = Math.min( controller.getCommonSettings().getNumberOfOutputDimensions(), maximumNumberOfDimensions );
		return new SpinnerNumberModel( numberOfDimensions, MINIMUM_NUMBER_OF_DIMENSIONS,
				maximumNumberOfDimensions, 1 );
	}

	// Method called when the JFrame is closed
	private void onWindowClosing()
	{
		// Perform any cleanup or actions needed before closing the window
		logger.debug( "View is closing." );
		controller.saveSettingsToPreferences();
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
		feedbackLabel.setText( "Computing ..." );
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
				controller.computeFeature( inputDimensionsPanel );
				return null;
			}

			@Override
			protected void done()
			{
				try
				{
					get();
					executionCompleted( "Successfully computed dimensionality reduction.", new Color( 0, 100, 0 ) );
				}
				catch ( Exception e )
				{
					String message = e.getCause().getMessage();
					logger.error( "Running dimensionality reduction failed. {}", message, e.getCause() );
					executionCompleted( message, Color.RED );
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

	private void executionCompleted( final String message, final Color color )
	{
		feedbackLabel.setForeground( color );
		feedbackLabel.setText( message );
		computeButton.setIcon( null );
		repaint();
	}
}
