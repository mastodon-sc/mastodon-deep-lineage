package org.mastodon.mamut.feature.dimensionalityreduction.tsne;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.util.StandardScaler;

public class TSneDemoTgmmMini
{
	public static void main( String[] args ) throws IOException, CsvValidationException
	{
		File file = new File( "src/test/resources/org/mastodon/mamut/feature/dimensionalityreduction/tgmm-mini-spot.csv" );
		CSVReader reader = new CSVReader( new FileReader( file ) );
		List< double[] > data = new ArrayList<>();
		reader.readNext(); // skip header
		for ( String[] nextLine; ( nextLine = reader.readNext() ) != null; )
		{
			double[] values = new double[ nextLine.length ];
			for ( int i = 0; i < nextLine.length; i++ )
				values[ i ] = Double.parseDouble( nextLine[ i ] );
			data.add( values );
		}
		double[][] inputData = data.toArray( new double[ data.size() ][ data.get( 0 ).length ] );
		StandardScaler.standardizeColumns( inputData );
		TSneConfiguration config = setUpTSne( inputData );
		BarnesHutTSne tsne = new ParallelBHTsne(); // according to https://github.com/lejon/T-SNE-Java/ the parallel version is faster at same accuracy
		double[][] result = tsne.tsne( config );
		result = Arrays.stream( result ).map( row -> Arrays.stream( row ).map( value -> value * 5d ).toArray() ) // scale up
				.toArray( double[][]::new );
		PlotPoints.plot( null, result, null );
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
