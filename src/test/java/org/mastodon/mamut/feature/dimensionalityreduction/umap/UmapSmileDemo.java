package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import java.util.Arrays;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

import smile.manifold.UMAP;
import smile.math.MathEx;

public class UmapSmileDemo
{
	/*
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
		int iterations = sampleData.length < 10_000 ? 500 : 200; // https://github.com/lmcinnes/umap/blob/a012b9d8751d98b94935ca21f278a54b3c3e1b7f/umap/umap_.py#L1073
		double minDist = 0.1;
		int nNeighbors = 15;
		return UMAP.of( sampleData, MathEx::distance, nNeighbors, 2, iterations, 1, minDist, 1.0, 5, 1 );
	}
	
	 */
}
