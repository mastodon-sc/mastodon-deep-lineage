package org.mastodon.mamut.feature;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;

import java.io.IOException;

public abstract class AbstractFeatureTest< T >
{

	protected FeatureProjection< T > getProjection( Feature< T > ellipsoidFeature, FeatureProjectionSpec featureProjectionSpec )
	{
		return ellipsoidFeature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	public abstract void testFeatureComputation();

	public abstract void testFeatureSerialization() throws IOException;

	public abstract void testFeatureInvalidate();
}
