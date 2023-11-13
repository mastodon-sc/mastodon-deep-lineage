package org.mastodon.mamut.clustering;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.ui.DendrogramView;
import org.mastodon.mamut.clustering.util.Classification;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.util.Collections;
import java.util.Random;

/**
 * This class demonstrates the three different methods of linking for hierarchical clustering.<p>
 * The first three panels demonstrate the clustering with fixed values, the last three panels with random values.
 * The {@link org.mastodon.mamut.clustering.ui.DendrogramPanel} and the {@link DendrogramView} classes are used to visualize the dendrograms and thus also demonstrated by this class.
 *
 * @author Stefan Hahmann
 */
public class ClusterApporientedDemo
{
	private final static ClusteringAlgorithm algorithm = new DefaultClusteringAlgorithm();

	private final static Cluster averageFixed =
			algorithm.performClustering( ClusterData.fixedDistances, ClusterData.names, new AverageLinkageStrategy() );

	private final static Cluster singleFixed =
			algorithm.performClustering( ClusterData.fixedDistances, ClusterData.names, new SingleLinkageStrategy() );

	private final static Cluster completeFixed =
			algorithm.performClustering( ClusterData.fixedDistances, ClusterData.names, new CompleteLinkageStrategy() );

	public static void main( String[] args )
	{
		JFrame frame = new JFrame( "Three methods of linking for hierarchical clustering" );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setSize( 1600, 1000 );

		double[][] randomDistances = getRandomSymmetricDistanceMatrix();
		Cluster averageRandom = algorithm.performClustering( randomDistances, ClusterData.names, new AverageLinkageStrategy() );
		Cluster singleRandom = algorithm.performClustering( randomDistances, ClusterData.names, new SingleLinkageStrategy() );
		Cluster completeRandom = algorithm.performClustering( randomDistances, ClusterData.names, new CompleteLinkageStrategy() );

		JPanel averagedFixedPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), averageFixed, 20d ),
				"Average Linkage (Fixed Values)"
		).getPanel();
		JPanel singleFixedPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), singleFixed, 20d ),
				"Single Linkage (Fixed Values)"
		).getPanel();
		JPanel completeFixedPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), completeFixed, 20d ),
				"Complete Linkage (Fixed Values)"
		).getPanel();
		JPanel averagedRandomPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), averageRandom, 5d ),
				"Average Linkage (Random Values)"
		).getPanel();
		JPanel singleRandomPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), singleRandom, 5d ),
				"Single Linkage (Random Values)"
		).getPanel();
		JPanel completeRandomPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), completeRandom, 5d ),
				"Complete Linkage (Random Values)"
		).getPanel();

		frame.setLayout( new MigLayout( "insets 10, wrap 3, fill" ) );

		frame.add( averagedFixedPanel, "grow" );
		frame.add( singleFixedPanel, "grow" );
		frame.add( completeFixedPanel, "grow" );
		frame.add( averagedRandomPanel, "grow" );
		frame.add( singleRandomPanel, "grow" );
		frame.add( completeRandomPanel, "grow" );

		frame.setVisible( true );
	}

	private static double[][] getRandomSymmetricDistanceMatrix()
	{
		double[][] symmetricArray = new double[ 10 ][ 10 ];
		Random random = new Random();

		// Generate random int values for the upper triangular matrix and zero the diagonal
		for ( int i = 0; i < 10; i++ )
			for ( int j = i; j < 10; j++ )
				symmetricArray[ i ][ j ] = symmetricArray[ j ][ i ] = j == i ? 0 : ( int ) ( random.nextDouble() * 100 );

		return symmetricArray;
	}
}
