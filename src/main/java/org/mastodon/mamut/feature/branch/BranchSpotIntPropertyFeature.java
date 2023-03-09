package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.properties.IntPropertyMap;

import java.util.Collections;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

public abstract class BranchSpotIntPropertyFeature< T > implements Feature< T >
{

	public final IntPropertyMap< T > map;

	protected final IntFeatureProjection< T > projection;

	public BranchSpotIntPropertyFeature( IntPropertyMap< T > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( getFeatureProjectionSpec() ), map, Dimension.NONE_UNITS );
	}

	protected abstract FeatureProjectionSpec getFeatureProjectionSpec();

	public int get( final T branchSpot )
	{
		return map.getInt( branchSpot );
	}

	@Override
	public FeatureProjection< T > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< T > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public void invalidate( final T branchSpot )
	{
		map.remove( branchSpot );
	}
}
