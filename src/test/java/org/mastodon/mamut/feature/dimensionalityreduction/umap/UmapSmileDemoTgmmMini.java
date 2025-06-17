package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.util.StandardScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.manifold.UMAP;
import smile.math.MathEx;

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
		int iterations = data.length < 10_000 ? 500 : 200; // https://github.com/lmcinnes/umap/blob/a012b9d8751d98b94935ca21f278a54b3c3e1b7f/umap/umap_.py#L1073
		double minDist = 0.1;
		int nNeighbors = 15;
		return UMAP.of( data, MathEx::distance, nNeighbors, 2, iterations, 1, minDist, 1.0, 5, 1 );
	}
}
