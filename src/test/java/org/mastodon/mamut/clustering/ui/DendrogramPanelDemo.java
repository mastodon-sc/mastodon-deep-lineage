/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import org.mastodon.mamut.clustering.util.Classification;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collections;

/**
 * A demo class for {@link DendrogramPanel}.
 * <br>
 * It creates a dendrogram from a sample cluster with fixed values and displays it in a frame.
 *
 * @author Stefan Hahmann
 */
public class DendrogramPanelDemo
{
	private static final double[][] distances = new double[][] { { 0, 1, 9, 7, 11, 14 }, { 1, 0, 4, 3, 8, 10 }, { 9, 4, 0, 9, 2, 8 },
			{ 7, 3, 9, 0, 6, 13 }, { 11, 8, 2, 6, 0, 10 }, { 14, 10, 8, 13, 10, 0 } };

	public static void main( String[] args )
	{
		JFrame frame = new JFrame();
		frame.setSize( 400, 300 );
		frame.setLocation( 400, 300 );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		JPanel content = new JPanel();
		Cluster cluster = createSampleCluster();
		Classification< String > classification = new Classification<>( Collections.emptyList(), cluster, 6d, distances );
		DendrogramPanel< String > dp = new DendrogramPanel<>( classification );

		frame.setContentPane( content );
		content.setBackground( Color.red );
		content.setLayout( new BorderLayout() );
		content.add( dp, BorderLayout.CENTER );
		dp.setBackground( Color.WHITE );

		frame.setVisible( true );
	}

	private static Cluster createSampleCluster()
	{
		String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster cluster = alg.performClustering( distances, names, new AverageLinkageStrategy() );
		cluster.toConsole( 0 );
		return cluster;
	}
}
