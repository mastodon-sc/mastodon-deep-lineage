package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;
import org.mastodon.mamut.clustering.ClusterRootNodesListener;
import org.mastodon.mamut.clustering.config.ClusteringMethod;
import org.mastodon.mamut.clustering.config.CropCriteria;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.ClusterRootNodesController;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

public class ClusterRootNodesView extends JFrame implements ClusterRootNodesListener< BranchSpotTree >
{
	private final ClusterRootNodesController controller;

	private final JRadioButton timepoint = new JRadioButton( "Timepoint" );

	private final JRadioButton numberOfCells = new JRadioButton( "Number of cells" );

	private final JFormattedTextField start = createNumberTextField();

	private final JFormattedTextField end = createNumberTextField();

	private final JFormattedTextField numberOfClasses = createNumberTextField();

	private final JFormattedTextField minCellDivisions = createNumberTextField();

	private final JRadioButton normalizedDifference = new JRadioButton( SimilarityMeasure.NORMALIZED_DIFFERENCE.getName() );

	private final JRadioButton averageDifference = new JRadioButton( SimilarityMeasure.AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE.getName() );

	private final JRadioButton absoluteDifference = new JRadioButton( SimilarityMeasure.ABSOLUTE_DIFFERENCE.getName() );

	private final JRadioButton averageLinkage = new JRadioButton( ClusteringMethod.AVERAGE_LINKAGE.getName() );

	private final JRadioButton singleLinkage = new JRadioButton( ClusteringMethod.SINGLE_LINKAGE.getName() );

	private final JRadioButton completeLinkage = new JRadioButton( ClusteringMethod.COMPLETE_LINKAGE.getName() );

	private final JButton createTagSet = new JButton( "Create tag set" );

	public ClusterRootNodesView( @Nullable ClusterRootNodesController controller )
	{
		super( "Cluster Root Nodes based on lineage tree similarity" );
		this.controller = controller;
		initLayout();
		initActions();
		updateState();
		if ( controller != null )
			controller.addListener( this );
	}

	private void initLayout()
	{
		setLayout( new MigLayout( "insets dialog, fill" ) );

		addCropping();

		add( new JLabel( "Number of classes:" ) );
		add( numberOfClasses, "wrap" );

		add( new JLabel( "Minimum number of cell divisions:" ) );
		add( minCellDivisions, "wrap" );

		addSimilarityMethod();
		addClusteringMethod();
		addSimilarityFeature();

		add( createTagSet, "span, split 2, align center" );
	}

	private void addCropping()
	{
		add( new JLabel( "Crop tree criterion:" ), "gaptop unrelated" );
		add( timepoint, "split 4" );
		add( numberOfCells, "gapbefore unrelated, wrap" );
		ButtonGroup cropCriteria = new ButtonGroup();
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
		add( normalizedDifference, "split 3" );
		add( averageDifference, "gapbefore unrelated" );
		add( absoluteDifference, "gapbefore unrelated, wrap" );
		ButtonGroup similarityMeasures = new ButtonGroup();
		similarityMeasures.add( normalizedDifference );
		similarityMeasures.add( averageDifference );
		similarityMeasures.add( absoluteDifference );
	}

	private void addClusteringMethod()
	{
		add( new JLabel( "Clustering method:" ), "gaptop unrelated" );
		add( averageLinkage, "split 3" );
		add( singleLinkage, "gapbefore unrelated" );
		add( completeLinkage, "gapbefore unrelated, wrap" );
		ButtonGroup clusteringMethods = new ButtonGroup();
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

		if ( controller == null )
			return;

		timepoint.addActionListener( event -> controller.setCropCriterion( CropCriteria.TIMEPOINT ) );
		numberOfCells.addActionListener( event -> controller.setCropCriterion( CropCriteria.NUMBER_OF_CELLS ) );
		addChangeListener( start, event -> controller.setCropStart( getValue( start ) ) );
		addChangeListener( end, event -> controller.setCropEnd( getValue( end ) ) );
		addChangeListener( numberOfClasses, event -> controller.setNumberOfClasses( getValue( numberOfClasses ) ) );
		addChangeListener( minCellDivisions, event -> controller.setMinCellDivisions( getValue( minCellDivisions ) ) );
		normalizedDifference.addActionListener( event -> controller.setSimilarityMeasure( SimilarityMeasure.NORMALIZED_DIFFERENCE ) );
		averageDifference
				.addActionListener( event -> controller.setSimilarityMeasure( SimilarityMeasure.AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE ) );
		absoluteDifference.addActionListener( event -> controller.setSimilarityMeasure( SimilarityMeasure.ABSOLUTE_DIFFERENCE ) );
		averageLinkage.addActionListener( event -> controller.setClusteringMethod( ClusteringMethod.AVERAGE_LINKAGE ) );
		singleLinkage.addActionListener( event -> controller.setClusteringMethod( ClusteringMethod.SINGLE_LINKAGE ) );
		completeLinkage.addActionListener( event -> controller.setClusteringMethod( ClusteringMethod.COMPLETE_LINKAGE ) );
		createTagSet.addActionListener( event -> SwingUtilities.invokeLater( controller::createTagSet ) );
	}

	private void updateState()
	{
		if ( controller == null )
			return;

		timepoint.setSelected( controller.getCropCriterion().equals( CropCriteria.TIMEPOINT ) );
		numberOfCells.setSelected( controller.getCropCriterion().equals( CropCriteria.NUMBER_OF_CELLS ) );

		start.setValue( controller.getCropStart() );
		end.setValue( controller.getCropEnd() );
		numberOfClasses.setValue( controller.getNumberOfClasses() );
		minCellDivisions.setValue( controller.getMinCellDivisions() );

		normalizedDifference.setSelected( controller.getSimilarityMeasure().equals( SimilarityMeasure.NORMALIZED_DIFFERENCE ) );
		averageDifference
				.setSelected( controller.getSimilarityMeasure().equals( SimilarityMeasure.AVERAGE_DIFFERENCE_PER_CELL_LIFE_CYCLE ) );
		absoluteDifference.setSelected( controller.getSimilarityMeasure().equals( SimilarityMeasure.ABSOLUTE_DIFFERENCE ) );

		averageLinkage.setSelected( controller.getClusteringMethod().equals( ClusteringMethod.AVERAGE_LINKAGE ) );
		singleLinkage.setSelected( controller.getClusteringMethod().equals( ClusteringMethod.SINGLE_LINKAGE ) );
		completeLinkage.setSelected( controller.getClusteringMethod().equals( ClusteringMethod.COMPLETE_LINKAGE ) );
	}

	@Override
	public void clusterRootNodesComputed( @Nullable Cluster cluster, @Nullable Map< String, BranchSpotTree > objectMapping, double cutoff )
	{
		DendrogramView< BranchSpotTree > dendrogramView =
				new DendrogramView<>( cluster, objectMapping, cutoff, "Dendrogram of hierarchical clustering of lineages" );
		dendrogramView.show();
	}

	@Override
	public void cropCriterionChanged( double start, double end )
	{
		this.start.setValue( start );
		this.end.setValue( end );
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

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener( JTextComponent text, ChangeListener changeListener )
	{
		Objects.requireNonNull( text );
		Objects.requireNonNull( changeListener );
		DocumentListener dl = new DocumentListener()
		{
			private int lastChange = 0;

			private int lastNotifiedChange = 0;

			@Override
			public void insertUpdate( DocumentEvent e )
			{
				changedUpdate( e );
			}

			@Override
			public void removeUpdate( DocumentEvent e )
			{
				changedUpdate( e );
			}

			@Override
			public void changedUpdate( DocumentEvent e )
			{
				lastChange++;
				SwingUtilities.invokeLater( () -> {
					if ( lastNotifiedChange != lastChange )
					{
						lastNotifiedChange = lastChange;
						changeListener.stateChanged( new ChangeEvent( text ) );
					}
				} );
			}
		};
		text.addPropertyChangeListener( "document", ( PropertyChangeEvent e ) -> {
			Document d1 = ( Document ) e.getOldValue();
			Document d2 = ( Document ) e.getNewValue();
			if ( d1 != null )
				d1.removeDocumentListener( dl );
			if ( d2 != null )
				d2.addDocumentListener( dl );
			dl.changedUpdate( null );
		} );
		Document d = text.getDocument();
		if ( d != null )
			d.addDocumentListener( dl );
	}
}
