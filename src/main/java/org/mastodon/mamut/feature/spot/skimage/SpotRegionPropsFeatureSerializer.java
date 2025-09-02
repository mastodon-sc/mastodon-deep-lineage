package org.mastodon.mamut.feature.spot.skimage;

import static org.mastodon.mamut.feature.spot.skimage.SpotRegionPropsFeature.FEATURE_SPEC;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.spot.skimage.SpotRegionPropsFeature.RegionProp;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

@Plugin( type = FeatureSerializer.class )
public class SpotRegionPropsFeatureSerializer implements FeatureSerializer< SpotRegionPropsFeature, Spot >
{
	@Override
	public FeatureSpec< SpotRegionPropsFeature, Spot > getFeatureSpec()
	{
		return FEATURE_SPEC;
	}

	@Override
	public void serialize( SpotRegionPropsFeature feature, ObjectToFileIdMap< Spot > idMap,
			ObjectOutputStream out ) throws IOException
	{
		final int nSources = feature.valuesByProp.get( RegionProp.EQUIVALENT_DIAMETER_AREA ).size();
		out.writeInt( nSources );

		for ( int sourceId = 0; sourceId < nSources; sourceId++ )
		{
			for ( RegionProp regionProp : RegionProp.values() )
				writeMap( feature.valuesByProp.get( regionProp ).get( sourceId ), idMap, out );
		}
	}

	@Override
	public SpotRegionPropsFeature deserialize( FileIdToObjectMap< Spot > idMap, RefCollection< Spot > pool, ObjectInputStream in )
			throws IOException, ClassNotFoundException
	{
		final int nSources = in.readInt();
		final Map< RegionProp, List< DoublePropertyMap< Spot > > > map = new EnumMap<>( RegionProp.class );

		// allocate one list per field
		for ( RegionProp regionProp : RegionProp.values() )
			map.put( regionProp, new ArrayList<>( nSources ) );

		for ( int i = 0; i < nSources; i++ )
		{
			for ( RegionProp regionProp : RegionProp.values() )
				map.get( regionProp ).add( readMap( pool, idMap, in ) );
		}

		// constructor call stays explicit & readable
		return new SpotRegionPropsFeature( map );
	}

	// --- helpers ---

	private static void writeMap( DoublePropertyMap< Spot > map, ObjectToFileIdMap< Spot > idMap, ObjectOutputStream out )
			throws IOException
	{
		new DoublePropertyMapSerializer<>( map ).writePropertyMap( idMap, out );
	}

	private static DoublePropertyMap< Spot > readMap( RefCollection< Spot > pool, FileIdToObjectMap< Spot > idMap, ObjectInputStream in )
			throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > map = new DoublePropertyMap<>( pool, Double.NaN );
		new DoublePropertyMapSerializer<>( map ).readPropertyMap( idMap, in );
		return map;
	}
}
