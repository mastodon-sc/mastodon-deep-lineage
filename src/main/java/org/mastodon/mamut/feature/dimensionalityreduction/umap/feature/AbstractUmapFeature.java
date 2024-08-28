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
