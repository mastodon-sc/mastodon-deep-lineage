package org.mastodon.mamut.feature.spot.dimensionalityreduction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.mastodon.collection.RefCollection;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.feature.dimensionalityreduction.AbstractOutputFeature;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;

public class SpotOutputFeatureSerializerTools
{
	private SpotOutputFeatureSerializerTools()
	{
		// prevent instantiation
	}

	public static void serialize( final AbstractOutputFeature< Spot > feature, final ObjectToFileIdMap< Spot > idMap,
			final ObjectOutputStream oos ) throws IOException
	{
		oos.writeInt( feature.getOutputMaps().size() );
		for ( DoublePropertyMap< Spot > output : feature.getOutputMaps() )
		{
			final DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( output );
			serializer.writePropertyMap( idMap, oos );
		}
	}

	public static < T extends AbstractOutputFeature< Spot > > T deserialize( final FileIdToObjectMap< Spot > idMap,
			final RefCollection< Spot > pool, final ObjectInputStream ois,
			final Function< List< DoublePropertyMap< Spot > >, T > featureCreator ) throws IOException, ClassNotFoundException
	{
		int numDimensions = ois.readInt();
		List< DoublePropertyMap< Spot > > outputMaps = new ArrayList<>( numDimensions );
		for ( int i = 0; i < numDimensions; i++ )
		{
			DoublePropertyMap< Spot > umapOutput = new DoublePropertyMap<>( pool, Double.NaN );
			DoublePropertyMapSerializer< Spot > serializer = new DoublePropertyMapSerializer<>( umapOutput );
			serializer.readPropertyMap( idMap, ois );
			outputMaps.add( umapOutput );
		}
		return featureCreator.apply( outputMaps );
	}
}
