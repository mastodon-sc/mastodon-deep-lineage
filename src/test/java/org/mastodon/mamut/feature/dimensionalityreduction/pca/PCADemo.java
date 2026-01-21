package org.mastodon.mamut.feature.dimensionalityreduction.pca;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

import smile.data.DataFrame;
import smile.feature.extraction.PCA;

public class PCADemo
{
	public static void main( String[] args )
	{
		double[][] inputData = RandomDataTools.generateSampleData();
		double[][] result = setUpPCA( inputData );
		PlotPoints.plot( inputData, result, resultValues -> resultValues[ 0 ] > 0 );
	}

	static double[][] setUpPCA( final double[][] inputData )
	{
		DataFrame dataFrame = DataFrame.of( inputData );
		PCA pca = PCA.fit( dataFrame ).getProjection( 2 );
		return pca.apply( inputData );
	}
}
