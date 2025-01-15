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
package org.mastodon.mamut.feature.spot.dimensionalityreduction.umap;

import org.mastodon.RefPool;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeatureComputer;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SpotUmapFeatureComputer extends AbstractUmapFeatureComputer< Spot, Link, ModelGraph >
{

	public SpotUmapFeatureComputer( final Model model, final Context context )
	{
		super( model, context );
	}

	@Override
	protected AbstractUmapFeature< Spot > createFeatureInstance( final List< DoublePropertyMap< Spot > > umapOutputMaps )
	{
		return new SpotUmapFeature( umapOutputMaps );
	}

	@Override
	protected RefPool< Spot > getRefPool()
	{
		return model.getGraph().vertices().getRefPool();
	}

	@Override
	protected ReentrantReadWriteLock getLock( final ModelGraph graph )
	{
		return graph.getLock();
	}

	@Override
	protected Collection< Spot > getVertices()
	{
		return model.getGraph().vertices();
	}
}
