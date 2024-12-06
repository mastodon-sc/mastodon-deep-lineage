package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.mastodon.mamut.feature.dimensionalityreduction.PlotPoints;
import org.mastodon.mamut.feature.dimensionalityreduction.util.StandardScaler;

import tagbio.umap.Umap;
import tagbio.umap.metric.EuclideanMetric;

public class UmapDemoTgmmMini
{
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
			Umap umap = setUpUmap();
			double[][] result = umap.fitTransform( inputData );
			PlotPoints.plot( null, result, null );
		}
	}

	static Umap setUpUmap()
	{
		Umap umap = new Umap();
		umap.setVerbose( true );
		umap.setMetric( EuclideanMetric.SINGLETON );
		umap.setSeed( 42 );
		umap.setNumberComponents( 2 );
		umap.setMinDist( 0.1f );
		umap.setNumberNearestNeighbours( 15 );
		return umap;
	}
}
