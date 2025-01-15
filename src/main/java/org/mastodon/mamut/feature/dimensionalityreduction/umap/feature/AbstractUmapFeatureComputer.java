/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.feature.dimensionalityreduction.umap.feature;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.RefPool;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.dimensionalityreduction.AbstractOutputFeatureComputer;
import org.mastodon.mamut.feature.dimensionalityreduction.CommonSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.UmapSettings;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.model.Model;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tagbio.umap.Umap;

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
public abstract class AbstractUmapFeatureComputer< V extends Vertex< E >, E extends Edge< V >, G extends ReadOnlyGraph< V, E > >
		extends AbstractOutputFeatureComputer< V, E, G >
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private UmapSettings umapSettings;

	private double[][] umapResult;

	protected AbstractUmapFeatureComputer( final Model model, final Context context )
	{
		super( model, context );
	}

	public void computeFeature( final CommonSettings commonSettings, final UmapSettings umapSettings,
			final List< InputDimension< V > > inputDimensions, final G graph )
	{
		this.umapSettings = umapSettings;
		super.computeFeature( commonSettings, inputDimensions, graph );
	}

	@Override
	protected void computeAlgorithm( double[][] dataMatrix )
	{
		Umap umap = new Umap();
		umap.setNumberComponents( settings.getNumberOfOutputDimensions() );
		umap.setNumberNearestNeighbours( umapSettings.getNumberOfNeighbors() );
		umap.setMinDist( ( float ) umapSettings.getMinimumDistance() );
		umap.setThreads( 1 );
		umap.setSeed( 42 );
		logger.info( "Fitting umap. Data matrix has {} rows x {} columns.", dataMatrix.length, dataMatrix[ 0 ].length );
		umapResult = umap.fitTransform( dataMatrix );
		logger.info( "Finished fitting umap. Results has {} rows x {} columns.", umapResult.length,
				umapResult.length > 0 ? umapResult[ 0 ].length : 0 );
	}

	@Override
	protected double[][] getResult()
	{
		return umapResult;
	}

	protected abstract AbstractUmapFeature< V > createFeatureInstance( final List< DoublePropertyMap< V > > umapOutputMaps );

	protected abstract RefPool< V > getRefPool();

	protected abstract ReentrantReadWriteLock getLock( final G graph );
}
