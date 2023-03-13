package org.mastodon.mamut.feature.branch;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.properties.DoublePropertyMap;

import java.util.Collections;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

public abstract class DoublePropertyFeature< T > implements Feature< T >
{

	public final DoublePropertyMap< T > map;

	protected final FeatureProjection< T > projection;

	public DoublePropertyFeature( DoublePropertyMap< T > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( getFeatureProjectionSpec() ), map, Dimension.NONE_UNITS );
	}

	protected abstract FeatureProjectionSpec getFeatureProjectionSpec();

	public double get( final T branchSpot )
	{
		return map.getDouble( branchSpot );
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
