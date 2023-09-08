package org.mastodon.mamut.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

public class ClusterWekaDemo
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static void main( String[] args ) throws Exception
	{
		Instances data = createDataset( ClusterData.fixedDistances, ClusterData.names );

		HierarchicalClusterer clusterer = new HierarchicalClusterer();
		int classCount = 3;
		String[] options = { "-P", "-N", String.valueOf( classCount ), "-L", "Average", "-B" };
		clusterer.setOptions( options );
		clusterer.buildClusterer( data );

		String newick = clusterer.graph();

		logger.info( "Number of classes: {}", clusterer.getNumClusters() );
		logger.info( "Newick: {}", newick );

		for ( int i = 0; i < ClusterData.fixedDistances.length; i++ )
		{
			Instance instance = data.get( i );
			int cluster = clusterer.clusterInstance( instance );
			logger.info( "Instance {}: {} is in cluster {}", ClusterData.names[ i ], instance, cluster );
		}

		// Visualize the dendrogram
		HierarchyVisualizer visualizer = new HierarchyVisualizer( newick );

		// Create a JFrame to display the dendrogram
		JFrame frame = new JFrame( "Dendrogram" );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setSize( 800, 600 );
		frame.getContentPane().add( visualizer );
		frame.setVisible( true );
	}

	private static Instances createDataset( double[][] distances, String[] names )
	{
		Instances data = new Instances( "MyDataset", createAttributes( names ), 10 );

		for ( double[] distance : distances )
		{
			Instance instance = new DenseInstance( 1.0, distance );
			data.add( instance );
		}

		return data;
	}

	private static ArrayList< Attribute > createAttributes( String[] names )
	{
		ArrayList< Attribute > attributes = new ArrayList<>();

		for ( String name : names )
			attributes.add( new Attribute( name ) );

		return attributes;
	}

	private static Instance createInstance( double[][] distances, int index )
	{
		double[] values = distances[ index ];

		return new DenseInstance( 1.0, values );
	}
}
