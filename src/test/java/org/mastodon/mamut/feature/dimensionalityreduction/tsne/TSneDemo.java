package org.mastodon.mamut.feature.dimensionalityreduction.tsne;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.RandomDataTools;

public class TSneDemo
{
	public static void main( final String[] args )
	{
		double[][] inputData = RandomDataTools.generateSampleData();
		TSneConfiguration config = setUpTSne( inputData );
		BarnesHutTSne tsne = new ParallelBHTsne(); // according to https://github.com/lejon/T-SNE-Java/ the parallel version is faster at same accuracy
		double[][] tsneResult = tsne.tsne( config );
		PlotPoints.plot( inputData, tsneResult, resultValues -> resultValues[ 0 ] > 10 );
	}

	static TSneConfiguration setUpTSne( double[][] inputData )
	{
		// Recommendations for t-SNE defaults: https://scikit-learn.org/stable/modules/generated/sklearn.manifold.TSNE.html
		int initialDimensions = 50; // used if PCA is true and dimensions of the input data are greater than this value
		double perplexity = 30d; // recommended value is between 5 and 50
		int maxIterations = 1000; // should be at least 250

		return TSneUtils.buildConfig( inputData, 2, initialDimensions, perplexity, maxIterations, true, 0.5d, false, true );
	}
}
