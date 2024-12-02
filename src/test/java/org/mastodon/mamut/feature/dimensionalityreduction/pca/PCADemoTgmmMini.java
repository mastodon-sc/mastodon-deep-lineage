package org.mastodon.mamut.feature.dimensionalityreduction.pca;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.util.StandardScaler;

import smile.data.DataFrame;
import smile.feature.extraction.PCA;

public class PCADemoTgmmMini
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
		double[][] result = setUpPCA( inputData );
		result = Arrays.stream( result ).map( row -> Arrays.stream( row ).map( value -> value * 20d ).toArray() ) // scale up
				.toArray( double[][]::new );
		PlotPoints.plot( null, result, null );
	}

	static double[][] setUpPCA( final double[][] inputData )
	{
		DataFrame dataFrame = DataFrame.of( inputData );
		PCA pca = PCA.fit( dataFrame ).getProjection( 2 );
		return pca.apply( inputData );
	}
}
