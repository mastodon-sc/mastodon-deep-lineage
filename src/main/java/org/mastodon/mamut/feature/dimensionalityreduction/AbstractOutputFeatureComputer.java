/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature.dimensionalityreduction;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.RefPool;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.ref.RefIntHashMap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.AbstractSerialFeatureComputer;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.feature.dimensionalityreduction.util.StandardScaler;
import org.mastodon.mamut.model.Model;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for computing features that result from dimensionality reduction in the Mastodon project.
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
public abstract class AbstractOutputFeatureComputer< V extends Vertex< E >, E extends Edge< V >, G extends ReadOnlyGraph< V, E > >
		extends AbstractSerialFeatureComputer< V >
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final StatusService statusService;

	private List< InputDimension< V > > inputDimensions;

	protected CommonSettings settings;

	private AbstractOutputFeature< V > feature;

	private final RefIntMap< V > vertexToRowIndexMap;

	private static final int NO_ENTRY = -1;

	protected AbstractOutputFeatureComputer( final Model model, final Context context )
	{
		this.model = model;
		this.statusService = context.getService( StatusService.class );
		this.vertexToRowIndexMap = new RefIntHashMap<>( getRefPool(), NO_ENTRY );
	}

	/**
	 * Computes the feature with the given settings and input dimensions and declares it in the feature model.
	 * <br>
	 * During computation, the given graph is locked for reading.
	 * The feature values are computed for each vertex in the graph, excluding vertices with invalid data rows
	 * (i.e. rows where the selected feature projections do not have values, such as {@link Double#NaN} or {@link Double#POSITIVE_INFINITY}).
	 *
	 * @param settings        the UMAP settings
	 * @param inputDimensions the input dimensions
	 * @param graph           the read-only graph
	 */
	protected void computeFeature( final CommonSettings settings, final List< InputDimension< V > > inputDimensions, final G graph )
	{
		logger.info( "Computing Feature with these common settings: {}", settings );
		this.settings = settings;
		logger.info( "Computing with {} input dimensions.", inputDimensions.size() );
		for ( InputDimension< V > inputDimension : inputDimensions )
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
		logger.info( "Finished computing output in {} ms", System.currentTimeMillis() - start );
		model.getFeatureModel().declareFeature( feature );
	}

	@Override
	protected void compute( final V vertex )
	{
		int rowIndex = vertexToRowIndexMap.get( vertex );
		if ( rowIndex == NO_ENTRY )
			return;
		for ( int i = 0; i < settings.getNumberOfOutputDimensions(); i++ )
		{
			DoublePropertyMap< V > outputMap = feature.getOutputMaps().get( i );
			double[][] result = getResult();
			outputMap.set( vertex, result[ rowIndex ][ i ] );
		}
	}

	@Override
	public void createOutput()
	{
		if ( feature == null )
			feature = initFeature( settings.getNumberOfOutputDimensions() );
		compute();
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
		feature.getOutputMaps().forEach( DoublePropertyMap::beforeClearPool );
	}

	private void compute()
	{
		vertexToRowIndexMap.clear();
		List< double[] > data = extractValidDataRowsAndCacheIndexes();
		double[][] dataMatrix = data.toArray( new double[ 0 ][ 0 ] );
		if ( dataMatrix.length == 0 )
			throw new IllegalArgumentException(
					"No valid data rows found, i.e. in each existing data row there is at least one non-finite value, such as Not a Number or Infinity." );
		if ( settings.isStandardizeFeatures() )
		{
			logger.debug( "Standardizing features with {} rows and {} columns.", dataMatrix.length, inputDimensions.size() );
			StandardScaler.standardizeColumns( dataMatrix );
			logger.debug( "Finished standardizing features" );
		}
		computeAlgorithm( dataMatrix );
	}

	private List< double[] > extractValidDataRowsAndCacheIndexes()
	{
		List< double[] > data = new ArrayList<>();
		int index = 0;
		for ( V vertex : getVertices() )
		{
			double[] row = new double[ inputDimensions.size() ];
			boolean finiteRow = true;
			for ( int i = 0; i < inputDimensions.size(); i++ )
			{
				InputDimension< V > inputDimension = inputDimensions.get( i );
				double value = inputDimension.getValue( vertex );
				if ( Double.isNaN( value ) )
				{
					finiteRow = false;
					break;
				}
				row[ i ] = value;
			}
			if ( !finiteRow )
				continue;
			data.add( row );
			vertexToRowIndexMap.put( vertex, index );
			index++;
		}
		return data;
	}

	private AbstractOutputFeature< V > initFeature( int numOutputDimensions )
	{
		List< DoublePropertyMap< V > > umapOutputMaps;
		umapOutputMaps = new ArrayList<>( numOutputDimensions );
		for ( int i = 0; i < numOutputDimensions; i++ )
		{
			umapOutputMaps.add( new DoublePropertyMap<>( getRefPool(), Double.NaN ) );
		}
		return createFeatureInstance( umapOutputMaps );
	}

	protected abstract double[][] getResult();

	protected abstract void computeAlgorithm( double[][] dataMatrix );

	protected abstract AbstractOutputFeature< V > createFeatureInstance( final List< DoublePropertyMap< V > > umapOutputMaps );

	protected abstract RefPool< V > getRefPool();

	protected abstract ReentrantReadWriteLock getLock( final G graph );
}
