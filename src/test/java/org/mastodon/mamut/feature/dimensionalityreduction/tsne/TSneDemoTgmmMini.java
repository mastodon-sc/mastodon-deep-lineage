package org.mastodon.mamut.feature.dimensionalityreduction.tsne;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.util.StandardScaler;

import smile.manifold.TSNE;

public class TSneDemoTgmmMini
{
	public static void main( String[] args ) throws IOException, CsvValidationException
	{
		File file = new File( "src/test/resources/org/mastodon/mamut/feature/dimensionalityreduction/tgmm-mini-spot.csv" );
		CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator( ',' );
		CSVReaderBuilder readerBuilder = new CSVReaderBuilder( new FileReader( file ) ).withCSVParser( parserBuilder.build() );
		try (CSVReader reader = readerBuilder.build())
		{
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
			double[][] result = setUpTSne( inputData );
			result = Arrays.stream( result ).map( row -> Arrays.stream( row ).map( value -> value * 2d ).toArray() ) // scale up
					.toArray( double[][]::new );
			PlotPoints.plot( null, result, null );
		}
	}

	static double[][] setUpTSne( double[][] inputData )
	{
		int d = 2; // target dimension
		double perplexity = 30d;
		double eta = 200; // learning rate
		int maxIter = 1000; // maximum number of iterations
		Properties p = new Properties();
		p.setProperty( "smile.t_sne.d", String.valueOf( d ) );
		p.setProperty( "smile.t_sne.perplexity", String.valueOf( perplexity ) );
		p.setProperty( "smile.t_sne.eta", String.valueOf( eta ) );
		p.setProperty( "smile.t_sne.iterations", String.valueOf( maxIter ) );
		TSNE tsne = TSNE.fit( inputData, TSNE.Options.of( p ) );
		return tsne.coordinates();
	}
}
