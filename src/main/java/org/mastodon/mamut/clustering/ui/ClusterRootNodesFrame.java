package org.mastodon.mamut.clustering.ui;

import net.miginfocom.swing.MigLayout;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.Settings;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.util.ClusterUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterRootNodesFrame extends JFrame
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	private final Settings settings = new Settings();

	private final ButtonGroup cropCriteria = new ButtonGroup();

	private final ButtonGroup similarityMeasures = new ButtonGroup();

	private final ButtonGroup clusteringMethods = new ButtonGroup();

	private final JFormattedTextField start = createNumberTextField();

	private final JFormattedTextField end = createNumberTextField();

	private final JFormattedTextField numberOfClasses = createNumberTextField();

	private final JButton createTagset = new JButton( "Create Tagset" );

	public ClusterRootNodesFrame( Model model )
	{
		super( "Cluster Root Nodes based on lineage tree similarity" );
		this.model = model;
		initLayout();
		initActions();
	}

	private void initLayout()
	{
		setLayout( new MigLayout( "insets dialog, fill" ) );

		addCropping();

		add( new JLabel( "Number of classes:" ) );
		add( numberOfClasses, "wrap" );

		addSimilarityMethod();
		addClusteringMethod();
		addSimilarityFeature();

		add( createTagset, "span, split 2, align center" );
	}

	private void addCropping()
	{
		add( new JLabel( "Crop tree criterion:" ), "gaptop unrelated" );
		JRadioButton timepoint = new JRadioButton( "Timepoint" );
		JRadioButton numberOfCells = new JRadioButton( "Number of cells" );
		timepoint.setSelected( true );
		add( timepoint, "split 4" );
		add( numberOfCells, "gapbefore unrelated, wrap" );
		cropCriteria.add( timepoint );
		cropCriteria.add( numberOfCells );

		add( new JLabel( "Crop at:" ), "gaptop unrelated" );
		add( new JLabel( "start: " ), "split 4" );
		add( start );
		add( new JLabel( "end: " ), "gapbefore unrelated" );
		add( end, "wrap" );
	}

	private void addSimilarityMethod()
	{
		add( new JLabel( "Similarity method:" ), "gaptop unrelated" );
		JRadioButton normalizedDifference = new JRadioButton( SimilarityMeasure.NORMALIZED_DIFFERENCE.getName() );
		JRadioButton averageDifference = new JRadioButton( SimilarityMeasure.AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE.getName() );
		JRadioButton absoluteDifference = new JRadioButton( SimilarityMeasure.ABSOLUTE_DIFFERENCE.getName() );
		normalizedDifference.setSelected( true );
		add( absoluteDifference, "split 3" );
		add( averageDifference, "gapbefore unrelated" );
		add( normalizedDifference, "gapbefore unrelated, wrap" );
		similarityMeasures.add( absoluteDifference );
		similarityMeasures.add( averageDifference );
		similarityMeasures.add( normalizedDifference );
	}

	private void addClusteringMethod()
	{
		add( new JLabel( "Clustering method:" ), "gaptop unrelated" );
		JRadioButton averageLinkage = new JRadioButton( ClusteringMethod.AVERAGE_LINKAGE.getName() );
		JRadioButton singleLinkage = new JRadioButton( ClusteringMethod.SINGLE_LINKAGE.getName() );
		JRadioButton completeLinkage = new JRadioButton( ClusteringMethod.COMPLETE_LINKAGE.getName() );
		averageLinkage.setSelected( true );
		add( averageLinkage, "split 3" );
		add( singleLinkage, "gapbefore unrelated" );
		add( completeLinkage, "gapbefore unrelated, wrap" );
		clusteringMethods.add( averageLinkage );
		clusteringMethods.add( singleLinkage );
		clusteringMethods.add( completeLinkage );
	}

	private void addSimilarityFeature()
	{
		add( new JLabel( "Similarity feature:" ), "gaptop unrelated" );
		JRadioButton cellLifetime = new JRadioButton( "Cell lifetime" );
		cellLifetime.setSelected( true );
		add( cellLifetime, "wrap" );
	}

	private void initActions()
	{
		this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		this.setLocationByPlatform( true );

		createTagset.addActionListener( event -> createTagset() );
	}

	@Nullable
	private AbstractButton getSelectedButton( ButtonGroup buttonGroup )
	{
		for ( Enumeration< AbstractButton > buttons = buttonGroup.getElements(); buttons.hasMoreElements(); )
		{
			AbstractButton button = buttons.nextElement();
			if ( button.isSelected() )
				return button;
		}
		return null;
	}

	private void createTagset()
	{
		updateSettings();

		Set< Spot > roots = RootFinder.getRoots( model.getGraph() );
		List< BranchSpotTree > trees = new ArrayList<>();
		this.model.getBranchGraph().graphRebuilt();
		for ( Spot root : roots )
		{
			BranchSpot rootBranchSpot = this.model.getBranchGraph().getBranchVertex( root, this.model.getBranchGraph().vertexRef() );
			trees.add( new BranchSpotTree( rootBranchSpot, settings.getCropEnd() ) );
		}
		double[][] distances = ClusterUtils.getDistanceMatrix( new ArrayList<>( trees ), settings.getSimilarityMeasure() );
		BranchSpotTree[] rootBranchSpots = trees.toArray( new BranchSpotTree[ 0 ] );
		Map< Integer, List< BranchSpotTree > > classification = ClusterUtils.getClustersByClassCount( rootBranchSpots, distances,
				settings.getClusteringMethod().getLinkageStrategy(), settings.getNumberOfClasses() );

		for ( Map.Entry< Integer, List< BranchSpotTree > > entry : classification.entrySet() )
		{
			logger.info( "Class {} has {} trees", entry.getKey(), entry.getValue().size() );
		}

	}

	private void updateSettings()
	{
		settings.setCropCriterion( CropCriteria.getByName( getSelectedButton( cropCriteria ).getText() ) );
		settings.setCropStart( getValue( start ) );
		settings.setCropEnd( getValue( end ) );
		settings.setNumberOfClasses( getValue( numberOfClasses ) );
		settings.setMinCellDivisions( getValue( minCellDivisions ) );
		settings.setSimilarityMeasure( SimilarityMeasure.getByName( getSelectedButton( similarityMeasures ).getText() ) );
		settings.setClusteringMethod( ClusteringMethod.getByName( getSelectedButton( clusteringMethods ).getText() ) );
	}

	private int getValue( JFormattedTextField textField )
	{
		Object value = textField.getValue();
		return value == null ? 0 : ( int ) value;
	}

	private static JFormattedTextField createNumberTextField()
	{
		NumberFormatter numberFormatter = new NumberFormatter( NumberFormat.getIntegerInstance() );
		numberFormatter.setValueClass( Integer.class );
		numberFormatter.setAllowsInvalid( false );
		numberFormatter.setMinimum( 0 );
		JFormattedTextField textField = new JFormattedTextField( numberFormatter );
		textField.setColumns( 5 );
		textField.setText( "0" );
		textField.setHorizontalAlignment( SwingConstants.RIGHT );
		return textField;
	}

	public static void main( String... args )
	{
		// NOTE: Small demo function that only shows the ClusterRootNodesFrame. For easy debugging.
		ClusterRootNodesFrame dialog = new ClusterRootNodesFrame( null );
		dialog.pack();
		dialog.setVisible( true );
	}
}
