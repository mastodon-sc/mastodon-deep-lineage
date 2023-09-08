package org.mastodon.mamut.clustering;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.ui.DendrogramView;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.util.Random;

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

		JPanel averagedFixedPanel = new DendrogramView<>( averageFixed, null, 0d, "Average Linkage (Fixed Values)" ).getPanel();
		JPanel singleFixedPanel = new DendrogramView<>( singleFixed, null, 0d, "Single Linkage (Fixed Values)" ).getPanel();
		JPanel completeFixedPanel = new DendrogramView<>( completeFixed, null, 0d, "Complete Linkage (Fixed Values)" ).getPanel();
		JPanel averagedRandomPanel = new DendrogramView<>( averageRandom, null, 0d, "Average Linkage (Random Values)" ).getPanel();
		JPanel singleRandomPanel = new DendrogramView<>( singleRandom, null, 0d, "Single Linkage (Random Values)" ).getPanel();
		JPanel completeRandomPanel =
				new DendrogramView<>( completeRandom, null, 0d, "Complete Linkage (Random Values)" ).getPanel();

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
