package org.mastodon.mamut.feature.spot.dimensionalityreduction.umap;

import org.mastodon.RefPool;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SpotUmapFeatureComputer extends AbstractUmapFeatureComputer< Spot, ModelGraph >
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
