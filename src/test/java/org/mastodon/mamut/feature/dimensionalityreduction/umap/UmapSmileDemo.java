package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import java.util.Arrays;
import java.util.Properties;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

import smile.manifold.UMAP;

public class UmapSmileDemo
{

	public static void main( final String[] args )
	{
		int numCluster1 = 50;
		int numCluster2 = 100;
		double[][] sampleData = RandomDataTools.generateSampleData( numCluster1, numCluster2 );
		double[][] umapResult = setUpUmap( sampleData );
		double[][] result = Arrays.stream( umapResult ).map( row -> Arrays.stream( row ).toArray() ).toArray( double[][]::new );
		PlotPoints.plot( sampleData, result, resultValues -> resultValues[ 0 ] > 1 );
	}
	
	static double[][] setUpUmap( final double[][] sampleData )
	{
		int nNeighbors = 15;
		int d = 2;
		double learningRate = 1.0;
		double minDist = 0.1;
		double spread = 1.0;
		int negativeSampleRate = 5;
		double repulsionStrength = 1.0;

		Properties props = new Properties();
		props.setProperty( "smile.umap.k", String.valueOf( nNeighbors ) );
		props.setProperty( "smile.umap.d", String.valueOf( d ) );
		props.setProperty( "smile.umap.learning_rate", String.valueOf( learningRate ) );
		props.setProperty( "smile.umap.min_dist", String.valueOf( minDist ) );
		props.setProperty( "smile.umap.spread", String.valueOf( spread ) );
		props.setProperty( "smile.umap.negative_samples", String.valueOf( negativeSampleRate ) );
		props.setProperty( "smile.umap.repulsion_strength", String.valueOf( repulsionStrength ) );

		return UMAP.fit( sampleData, UMAP.Options.of( props ) );
	}
}
