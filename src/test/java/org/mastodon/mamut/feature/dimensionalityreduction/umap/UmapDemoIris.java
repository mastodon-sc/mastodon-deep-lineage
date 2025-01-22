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

import tagbio.umap.Umap;
import tagbio.umap.metric.CategoricalMetric;
import tagbio.umap.metric.EuclideanMetric;

public class UmapDemoIris
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static void main( String[] args ) throws IOException, CsvValidationException
	{
		File file = new File( "src/test/resources/org/mastodon/mamut/feature/dimensionalityreduction/iris.tsv" );
		CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator( '\t' );
		CSVReaderBuilder builder = new CSVReaderBuilder( new FileReader( file ) ).withCSVParser( parserBuilder.build() );
		try (CSVReader reader = builder.build())
		{
			List< double[] > data = new ArrayList<>();
			reader.readNext(); // skip header
			for ( String[] nextLine; ( nextLine = reader.readNext() ) != null; )
			{
				double[] values = new double[ nextLine.length - 1 ];
				for ( int i = 1; i < nextLine.length; i++ )
					values[ i - 1 ] = Double.parseDouble( nextLine[ i ] );
				data.add( values );
			}
			double[][] inputData = data.toArray( new double[ data.size() ][ data.get( 0 ).length ] );
			StandardScaler.standardizeColumns( inputData );
			Umap umap = setUpUmap();
			long t0 = System.currentTimeMillis();
			double[][] result = umap.fitTransform( inputData );
			logger.info( "UMAP took {} ms", System.currentTimeMillis() - t0 );
			result = Arrays.stream( result ).map( row -> Arrays.stream( row ).map( value -> value * 5d ).toArray() ) // scale up
					.toArray( double[][]::new );
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
		umap.setLearningRate( 1.0f );
		umap.setLocalConnectivity( 1 );
		umap.setNegativeSampleRate( 5 );
		umap.setRepulsionStrength( 1.0f );
		umap.setThreads( 1 );
		umap.setSetOpMixRatio( 1.0f );
		umap.setSpread( 1.0f );
		umap.setTargetNNeighbors( -1 );
		umap.setTargetWeight( 0.5f );
		umap.setTransformQueueSize( 4 );
		umap.setAngularRpForest( false );
		umap.setNumberEpochs( 500 );
		umap.setTargetMetric( CategoricalMetric.SINGLETON );

		return umap;
	}
}
