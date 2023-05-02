package org.mastodon.mamut.feature.spot.ellipsoid;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

import java.io.IOException;

public abstract class AbstractEllipsoidFeatureTest
{
	protected final Model model = new Model();

	protected final ModelGraph graph = model.getGraph();

	protected final Spot spot = initEllipsoidSpot( graph );

	// eigenvalues for given covariance matrix
	// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
	protected final double[] eigenValues = new double[] { 3.2695842d, 4.4422001d, 13.2882158d };

	protected final double expectedLongAxis = Math.sqrt( eigenValues[ 2 ] );

	protected final double expectedMiddleAxis = Math.sqrt( eigenValues[ 1 ] );

	// compute semi-axes from eigenvalues
	protected final double expectedShortAxis = Math.sqrt( eigenValues[ 0 ] );

	private static Spot initEllipsoidSpot( ModelGraph graph )
	{
		Spot spot = graph.addVertex();
		spot.init( 1, new double[] { 1, 2, 3 }, 0 );
		spot.setCovariance( new double[][] { { 6, 2, 3 }, { 2, 7, 4 }, { 3, 4, 8 } } );
		return spot;
	}

	protected static FeatureProjection< Spot > getProjection( Feature< Spot > ellipsoidFeature,
			FeatureProjectionSpec featureProjectionSpec )
	{
		return ellipsoidFeature.project( FeatureProjectionKey.key( featureProjectionSpec ) );
	}

	abstract void testFeatureComputation();

	abstract void testFeatureSerialization() throws IOException;

	abstract void testFeatureInvalidate();
}
