package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.Distance;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;

public class DendrogramPanelDemo
{
	public static void main( String[] args )
	{
		JFrame frame = new JFrame();
		frame.setSize( 400, 300 );
		frame.setLocation( 400, 300 );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		JPanel content = new JPanel();
		Cluster cluster = createSampleCluster();
		DendrogramPanel< String > dp = new DendrogramPanel<>( cluster, null, 6d, null );

		frame.setContentPane( content );
		content.setBackground( Color.red );
		content.setLayout( new BorderLayout() );
		content.add( dp, BorderLayout.CENTER );
		dp.setBackground( Color.WHITE );

		frame.setVisible( true );
	}

	private static Cluster createSampleCluster()
	{
		double[][] distances = new double[][] { { 0, 1, 9, 7, 11, 14 }, { 1, 0, 4, 3, 8, 10 }, { 9, 4, 0, 9, 2, 8 },
				{ 7, 3, 9, 0, 6, 13 }, { 11, 8, 2, 6, 0, 10 }, { 14, 10, 8, 13, 10, 0 } };
		String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster cluster = alg.performClustering( distances, names, new AverageLinkageStrategy() );
		cluster.toConsole( 0 );
		return cluster;
	}
}
