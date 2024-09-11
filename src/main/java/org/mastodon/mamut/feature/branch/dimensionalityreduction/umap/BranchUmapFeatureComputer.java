package org.mastodon.mamut.feature.branch.dimensionalityreduction.umap;

import org.mastodon.RefPool;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.Context;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BranchUmapFeatureComputer extends AbstractUmapFeatureComputer< BranchSpot, BranchLink, ModelBranchGraph >
{

	public BranchUmapFeatureComputer( final Model model, final Context context )
	{
		super( model, context );
	}

	@Override
	protected AbstractUmapFeature< BranchSpot > createFeatureInstance( final List< DoublePropertyMap< BranchSpot > > umapOutputMaps )
	{
		return new BranchUmapFeature( umapOutputMaps );
	}

	@Override
	protected RefPool< BranchSpot > getRefPool()
	{
		return model.getBranchGraph().vertices().getRefPool();
	}

	@Override
	protected ReentrantReadWriteLock getLock( final ModelBranchGraph branchGraph )
	{
		return branchGraph.getLock();
	}

	@Override
	protected Collection< BranchSpot > getVertices()
	{
		return model.getBranchGraph().vertices();
	}
}
