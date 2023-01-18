package org.mastodon.mamut.feature;

import static org.mastodon.mamut.feature.SpotEllipsoidAspectRatiosFeature.SPOT_ELLIPSOID_ASPECT_RATIOS_FEATURE_SPEC;

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
		final DoublePropertyMapSerializer< Spot > aspectRatioAToBPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioAToB );
		final DoublePropertyMapSerializer< Spot > aspectRatioAToCPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioAToC );
		final DoublePropertyMapSerializer< Spot > aspectRatioBToCPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.aspectRatioBToC );

		aspectRatioAToBPropertySerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
		aspectRatioAToCPropertySerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
		aspectRatioBToCPropertySerializer.writePropertyMap( objectToFileIdMap, objectOutputStream );
	}

	@Override
	public SpotEllipsoidAspectRatiosFeature deserialize( FileIdToObjectMap< Spot > fileIdToObjectMap,
			RefCollection< Spot > pool, ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > aspectRatioAToBMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > aspectRatioAToCMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > aspectRatioBToCMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( aspectRatioAToBMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );
		new DoublePropertyMapSerializer<>( aspectRatioAToCMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );
		new DoublePropertyMapSerializer<>( aspectRatioBToCMap ).readPropertyMap( fileIdToObjectMap, objectInputStream );

		return new SpotEllipsoidAspectRatiosFeature( aspectRatioAToBMap, aspectRatioAToCMap, aspectRatioBToCMap );
	}
}
