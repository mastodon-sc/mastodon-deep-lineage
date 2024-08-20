package org.mastodon.mamut.feature.dimensionalityreduction.umap.feature;

import org.mastodon.RefPool;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.AbstractSerialFeatureComputer;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapFeatureSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.util.StandardScaler;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.util.UmapInputDimension;
import org.mastodon.mamut.model.Model;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tagbio.umap.Umap;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract class for computing UMAP features in the Mastodon project.
 * <br>
 * This provides the base implementation for computing UMAP features on vertices in a read-only graph.
 * It handles the setup, execution, and caching of UMAP computations.
 * <br>
 * This class connects the UMAP library to the Mastodon project by providing the necessary data and settings.
 * It ensures that only valid data rows (i.e. rows where the selected feature projections do not have values, such as {@link Double#NaN} or {@link Double#POSITIVE_INFINITY}) are used for UMAP computations.
 *
 * @param <V> the type of vertex
 * @param <G> the type of read-only graph
 */
public abstract class AbstractUmapFeatureComputer< V extends Vertex< ? >, G extends ReadOnlyGraph< V, ? > >
		extends AbstractSerialFeatureComputer< V >
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final StatusService statusService;

	private final Umap umap;

	private List< UmapInputDimension< V > > inputDimensions;

	private UmapFeatureSettings settings;

	private AbstractUmapFeature< V > feature;

	private double[][] umapResult;

	private final IntPropertyMap< V > validFeaturesCache;

	private int index;

	protected AbstractUmapFeatureComputer( final Model model, final Context context )
	{
		this.model = model;
		this.statusService = context.getService( StatusService.class );
		this.umap = new Umap();
		this.validFeaturesCache = createFeatureCacheInstance();
	}

	/**
	 * Computes the UMAP feature with the given settings and input dimensions and declares it in the feature model.
	 * <br>
	 * During computation, the given graph is locked for reading.
	 * The UMAP feature is computed for each vertex in the graph, excluding vertices with invalid data rows
	 * (i.e. rows where the selected feature projections do not have values, such as {@link Double#NaN} or {@link Double#POSITIVE_INFINITY}).
	 *
	 * @param settings        the UMAP settings
	 * @param inputDimensions the input dimensions
	 * @param graph           the read-only graph
	 */
	public void computeFeature( final UmapFeatureSettings settings, final List< UmapInputDimension< V > > inputDimensions,
			final G graph )
	{
		logger.info( "Computing UmapFeature with settings: {}", settings );
		this.settings = settings;
		logger.info( "Computing UmapFeatureComputer with {} input dimensions.", inputDimensions.size() );
		for ( UmapInputDimension< V > inputDimension : inputDimensions )
			logger.info( "Input dimension: {}", inputDimension );
		this.inputDimensions = inputDimensions;
		this.forceComputeAll = new AtomicBoolean( true );
		long start = System.currentTimeMillis();
		ReentrantReadWriteLock.ReadLock lock = getLock( graph ).readLock();
		lock.lock();
		try
		{
			run();
		}
		finally
		{
			lock.unlock();
		}
		logger.info( "Finished computing UmapFeature in {} ms", System.currentTimeMillis() - start );
		model.getFeatureModel().declareFeature( feature );
	}

	@Override
	protected void compute( final V vertex )
	{
		if ( isVertexValid( vertex ) )
		{
			for ( int i = 0; i < settings.getNumberOfOutputDimensions(); i++ )
			{
				DoublePropertyMap< V > umapOutput = feature.getUmapOutputMaps().get( i );
				umapOutput.set( vertex, umapResult[ index ][ i ] );
			}
			index++;
		}
	}

	@Override
	public void createOutput()
	{
		if ( feature == null )
			feature = initFeature( settings.getNumberOfOutputDimensions() );
		computeUmap();
	}

	@Override
	protected void notifyProgress( final int finished, final int total )
	{
		statusService.showStatus( finished, total, "Computing UmapFeature" );
	}

	@Override
	protected ValueIsSetEvaluator< V > getEvaluator()
	{
		return feature;
	}

	@Override
	protected void reset()
	{
		if ( feature == null )
			return;
		feature.getUmapOutputMaps().forEach( DoublePropertyMap::beforeClearPool );
	}

	private void computeUmap()
	{
		index = 0;
		List< double[] > data = extractAndCacheValidDataRows();
		double[][] dataMatrix = data.toArray( new double[ 0 ][ 0 ] );
		if ( dataMatrix.length == 0 )
			throw new IllegalArgumentException(
					"No valid data rows found, i.e. in each existing data row there is at least one non-finite value, such Not a Number or Infinity." );
		if ( settings.isStandardizeFeatures() )
		{
			logger.debug( "Standardizing features with {} rows", dataMatrix.length );
			for ( int i = 0; i < dataMatrix.length; i++ )
				dataMatrix[ i ] = StandardScaler.standardizeVector( dataMatrix[ i ] );
			logger.debug( "Finished standardizing features" );
		}
		umap.setNumberComponents( settings.getNumberOfOutputDimensions() );
		umap.setNumberNearestNeighbours( settings.getNumberOfNeighbors() );
		umap.setMinDist( ( float ) settings.getMinimumDistance() );
		umap.setThreads( 1 );
		umap.setSeed( 42 );
		logger.info( "Fitting umap. Data matrix has {} rows x {} columns.", dataMatrix.length, inputDimensions.size() );
		umapResult = umap.fitTransform( dataMatrix );
		logger.info( "Finished fitting umap. Results has {} rows x {} columns.", umapResult.length,
				umapResult.length > 0 ? umapResult[ 0 ].length : 0 );
	}

	private List< double[] > extractAndCacheValidDataRows()
	{
		List< double[] > data = new ArrayList<>();
		for ( V vertex : getVertices() )
		{
			double[] row = new double[ inputDimensions.size() ];
			boolean finiteRow = true;
			for ( int i = 0; i < inputDimensions.size(); i++ )
			{
				UmapInputDimension< V > inputDimension = inputDimensions.get( i );
				FeatureProjection< V > projection = inputDimension.getFeatureProjection();
				double value = projection.value( vertex );
				if ( Double.isNaN( value ) )
				{
					finiteRow = false;
					break;
				}
				row[ i ] = projection.value( vertex );
			}
			if ( !finiteRow )
			{
				cacheVertexAsInvalid( vertex );
				continue;
			}
			data.add( row );
			cacheVertexAsValid( vertex );
		}
		return data;
	}

	private boolean isVertexValid( final V vertex )
	{
		return validFeaturesCache.get( vertex ) != 0;
	}

	private void cacheVertexAsValid( final V vertex )
	{
		validFeaturesCache.set( vertex, 1 );
	}

	private void cacheVertexAsInvalid( final V vertex )
	{
		validFeaturesCache.set( vertex, 0 );
	}

	private IntPropertyMap< V > createFeatureCacheInstance()
	{
		return new IntPropertyMap<>( getRefPool(), 0 );
	}

	private AbstractUmapFeature< V > initFeature( int numOutputDimensions )
	{
		List< DoublePropertyMap< V > > umapOutputMaps;
		umapOutputMaps = new ArrayList<>( numOutputDimensions );
		for ( int i = 0; i < numOutputDimensions; i++ )
		{
			umapOutputMaps.add( new DoublePropertyMap<>( getRefPool(), Double.NaN ) );
		}
		return createFeatureInstance( umapOutputMaps );
	}

	protected abstract AbstractUmapFeature< V > createFeatureInstance( final List< DoublePropertyMap< V > > umapOutputMaps );

	protected abstract RefPool< V > getRefPool();

	protected abstract ReentrantReadWriteLock getLock( final G graph );
}
