package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.manifold.UMAP;

public class UmapSmileDemoTgmmMini
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static void main( String[] args ) throws IOException, CsvValidationException
	{
		File file = new File( "src/test/resources/org/mastodon/mamut/feature/dimensionalityreduction/tgmm-mini-spot.csv" );
		CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator( ',' );
		CSVReaderBuilder builder = new CSVReaderBuilder( new FileReader( file ) ).withCSVParser( parserBuilder.build() );
		try (CSVReader reader = builder.build())
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
			long t0 = System.currentTimeMillis();
			double[][] result = setUpUmap( inputData );
			logger.info( "UMAP took {} ms", System.currentTimeMillis() - t0 );
	
			double[][] resultScaled = Arrays.stream( result ).map( row -> Arrays.stream( row ).map( value -> value * 10d ).toArray() )
					.toArray( double[][]::new );
			PlotPoints.plot( null, resultScaled, null );
		}
	}
	
	static double[][] setUpUmap( double[][] data )
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

		return UMAP.fit( data, UMAP.Options.of( props ) );
	}
}
