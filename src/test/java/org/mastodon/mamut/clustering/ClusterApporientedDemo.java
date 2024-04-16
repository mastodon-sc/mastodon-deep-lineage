/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import net.imglib2.util.Util;
import net.miginfocom.swing.MigLayout;
import org.mastodon.mamut.clustering.ui.DendrogramView;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.util.Collections;
import java.util.Random;

/**
 * This class demonstrates the three different methods of linking for hierarchical clustering.<br>
 * The first three panels demonstrate the clustering with fixed values, the last three panels with random values.
 * The {@link org.mastodon.mamut.clustering.ui.DendrogramPanel} and the {@link DendrogramView} classes are used to visualize the dendrograms and thus also demonstrated by this class.
 *
 * @author Stefan Hahmann
 */
public class ClusterApporientedDemo
{
	private final static ClusteringAlgorithm algorithm = new DefaultClusteringAlgorithm();

	private final static Cluster averageFixed =
			algorithm.performClustering( ClusterData.example1.getValue(), ClusterData.example1.getKey(), new AverageLinkageStrategy() );

	private final static Cluster singleFixed =
			algorithm.performClustering( ClusterData.example1.getValue(), ClusterData.example1.getKey(), new SingleLinkageStrategy() );

	private final static Cluster completeFixed =
			algorithm.performClustering( ClusterData.example1.getValue(), ClusterData.example1.getKey(), new CompleteLinkageStrategy() );

	public static void main( String[] args )
	{
		JFrame frame = new JFrame( "Three methods of linking for hierarchical clustering" );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setSize( 1600, 1000 );

		double[][] randomDistances = getRandomSymmetricDistanceMatrix();
		Cluster averageRandom = algorithm.performClustering( randomDistances, ClusterData.example1.getKey(), new AverageLinkageStrategy() );
		Cluster singleRandom = algorithm.performClustering( randomDistances, ClusterData.example1.getKey(), new SingleLinkageStrategy() );
		Cluster completeRandom =
				algorithm.performClustering( randomDistances, ClusterData.example1.getKey(), new CompleteLinkageStrategy() );
		double median = Util.median( ClusterUtils.getUpperTriangle( randomDistances ) );

		JPanel averagedFixedPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), averageFixed, 20d, median ),
				"Average Linkage (Fixed Values)"
		).getCanvas();
		JPanel singleFixedPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), singleFixed, 20d, median ),
				"Single Linkage (Fixed Values)"
		).getCanvas();
		JPanel completeFixedPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), completeFixed, 20d, median ),
				"Complete Linkage (Fixed Values)"
		).getCanvas();
		JPanel averagedRandomPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), averageRandom, 5d, median ),
				"Average Linkage (Random Values)"
		).getCanvas();
		JPanel singleRandomPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), singleRandom, 5d, median ),
				"Single Linkage (Random Values)"
		).getCanvas();
		JPanel completeRandomPanel = new DendrogramView<>(
				new Classification<>( Collections.emptyList(), completeRandom, 5d, median ),
				"Complete Linkage (Random Values)"
		).getCanvas();

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
