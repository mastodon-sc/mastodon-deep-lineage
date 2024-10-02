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
package org.mastodon.mamut.feature.dimensionalityreduction.umap.feature;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.properties.DoublePropertyMap;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * This generic feature is used to store the UMAP outputs.
 * <br>
 * The UMAP outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the UMAP output.
 */
public abstract class AbstractUmapFeature< V extends Vertex< ? > > implements Feature< V >, ValueIsSetEvaluator< V >
{
	private static final String PROJECTION_NAME_TEMPLATE = "UMAP%d";

	protected static final String HELP_STRING =
			"Computes the umap according to the selected dimensions, the selected number of target dimensions and the minimum distance value.";

	private final List< DoublePropertyMap< V > > umapOutputMaps;

	protected final Map< FeatureProjectionKey, FeatureProjection< V > > projectionMap;

	protected AbstractUmapFeature( final List< DoublePropertyMap< V > > umapOutputMaps )
	{
		this.umapOutputMaps = umapOutputMaps;
		this.projectionMap = new LinkedHashMap<>( 2 );
		for ( int i = 0; i < umapOutputMaps.size(); i++ )
		{
			FeatureProjectionSpec projectionSpec = new FeatureProjectionSpec( getProjectionName( i ), Dimension.NONE );
			final FeatureProjectionKey key = key( projectionSpec );
			projectionMap.put( key, FeatureProjections.project( key, getUmapOutputMaps().get( i ), Dimension.NONE_UNITS ) );
		}
	}

	public String getProjectionName( final int outputDimension )
	{
		return String.format( PROJECTION_NAME_TEMPLATE, outputDimension + 1 );
	}

	public List< DoublePropertyMap< V > > getUmapOutputMaps()
	{
		return umapOutputMaps;
	}

	@Override
	public void invalidate( V vertex )
	{
		getUmapOutputMaps().forEach( map -> map.remove( vertex ) );
	}

	@Override
	public boolean valueIsSet( final V vertex )
	{
		for ( final DoublePropertyMap< V > map : getUmapOutputMaps() )
			if ( !map.isSet( vertex ) )
				return false;
		return true;
	}

	@Override
	public FeatureProjection< V > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< V > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

}
