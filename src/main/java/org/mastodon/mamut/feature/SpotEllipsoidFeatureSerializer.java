package org.mastodon.mamut.feature;

import static org.mastodon.mamut.feature.SpotEllipsoidFeature.SPOT_ELLIPSOID_FEATURE_SPEC;

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
 * De-/serializes {@link SpotEllipsoidFeature}
 */
@Plugin( type = FeatureSerializer.class )
public class SpotEllipsoidFeatureSerializer implements FeatureSerializer< SpotEllipsoidFeature, Spot >
{
	@Override
	public FeatureSpec< SpotEllipsoidFeature, Spot > getFeatureSpec()
	{
		return SPOT_ELLIPSOID_FEATURE_SPEC;
	}

	@Override
	public void serialize( SpotEllipsoidFeature feature, ObjectToFileIdMap< Spot > idMap,
			ObjectOutputStream objectOutputStream )
			throws IOException
	{
		final DoublePropertyMapSerializer< Spot > semiAxisAPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.semiAxisA );
		final DoublePropertyMapSerializer< Spot > semiAxisBPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.semiAxisB );
		final DoublePropertyMapSerializer< Spot > semiAxisCPropertySerializer =
				new DoublePropertyMapSerializer<>( feature.semiAxisC );
		final DoublePropertyMapSerializer< Spot > volumePropertySerializer =
				new DoublePropertyMapSerializer<>( feature.volume );

		semiAxisAPropertySerializer.writePropertyMap( idMap, objectOutputStream );
		semiAxisBPropertySerializer.writePropertyMap( idMap, objectOutputStream );
		semiAxisCPropertySerializer.writePropertyMap( idMap, objectOutputStream );
		volumePropertySerializer.writePropertyMap( idMap, objectOutputStream );
	}

	@Override
	public SpotEllipsoidFeature deserialize( FileIdToObjectMap< Spot > idMap, RefCollection< Spot > pool,
			ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException
	{
		final DoublePropertyMap< Spot > semiAxisAMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > semiAxisBMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > semiAxisCMap = new DoublePropertyMap<>( pool, Double.NaN );
		final DoublePropertyMap< Spot > volumeMap = new DoublePropertyMap<>( pool, Double.NaN );

		new DoublePropertyMapSerializer<>( semiAxisAMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( semiAxisBMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( semiAxisCMap ).readPropertyMap( idMap, objectInputStream );
		new DoublePropertyMapSerializer<>( volumeMap ).readPropertyMap( idMap, objectInputStream );

		return new SpotEllipsoidFeature( semiAxisAMap, semiAxisBMap, semiAxisCMap, volumeMap );
	}
}
