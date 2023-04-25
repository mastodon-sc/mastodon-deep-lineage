package org.mastodon.mamut.feature.spot.ellipsoid;

import static org.mastodon.mamut.feature.spot.ellipsoid.SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * De-/serializes {@link SpotEllipsoidAspectRatiosFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class SpotEllipsoidAspectRatiosFeatureSerializer
		implements FeatureSerializer< SpotEllipsoidAspectRatiosFeature, Spot >
{
	@Override
	public FeatureSpec< SpotEllipsoidAspectRatiosFeature, Spot > getFeatureSpec()
	{
		return SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC;
	}

	@Override
	public void serialize( SpotEllipsoidAspectRatiosFeature feature, ObjectToFileIdMap< Spot > objectToFileIdMap,
			ObjectOutputStream objectOutputStream ) throws IOException
	{
		final DoublePropertyMapSerializer< Spot > shortToMiddleSerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioShortToMiddle );
		final DoublePropertyMapSerializer< Spot > shortToLongSerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioShortToLong );
		final DoublePropertyMapSerializer< Spot > middleToLongSerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioMiddleToLong );

		shortToMiddleSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
		shortToLongSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
		middleToLongSerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
	}

	@Override
	public SpotEllipsoidAspectRatiosFeature deserialize( FileIdToObjectMap< Spot > fileIdToObjectMap,
			RefCollection< Spot > pool, ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > shortToMiddleMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > shortToLongMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > middleToLongMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( shortToMiddleMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );
		new DoublePropertyMapSerializer<>( shortToLongMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );
		new DoublePropertyMapSerializer<>( middleToLongMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );

		return new SpotEllipsoidAspectRatiosFeature( shortToMiddleMap, shortToLongMap, middleToLongMap );
	}
}
