/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 N/A
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
