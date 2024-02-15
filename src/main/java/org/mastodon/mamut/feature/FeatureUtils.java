package org.mastodon.mamut.feature;

import net.imglib2.util.Cast;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.model.Model;

import java.util.Objects;
import java.util.function.Predicate;

public class FeatureUtils
{
	private FeatureUtils()
	{
		// Prevent from instantiation.
	}

	private static < V extends Vertex< ? >, F extends Feature< V >, S extends FeatureSpec< F, V > > S
			getFeatureSpecGeneric( final Model model, final Predicate< F > predicate, final Class< S > featureSpecClass )
	{
		return Cast.unchecked( model.getFeatureModel().getFeatureSpecs().stream().filter( featureSpec -> {
			if ( featureSpecClass.isInstance( featureSpec ) )
			{
				final S spec = Cast.unchecked( featureSpec );
				return predicate.test( Cast.unchecked( model.getFeatureModel().getFeature( spec ) ) );
			}
			return false;
		} ).findFirst().orElse( null ) );
	}

	public static < V extends Vertex< ? >, F extends Feature< V >, S extends FeatureSpec< F, V > > F getFeatureGeneric( final Model model,
			final Predicate< F > predicate, final Class< S > featureSpecClass )
	{

		S spec = getFeatureSpecGeneric( model, predicate, featureSpecClass );
		if ( spec != null )
			return Cast.unchecked( model.getFeatureModel().getFeature( spec ) );
		return null;
	}

	public static < V extends Vertex< ? >, F extends Feature< V >, S extends FeatureSpec< F, V > > F getFeatureGeneric( final Model model,
			final Class< S > featureSpecClass )
	{
		Predicate< F > predicate = Objects::nonNull;
		S spec = getFeatureSpecGeneric( model, predicate, featureSpecClass );
		if ( spec != null )
			return Cast.unchecked( model.getFeatureModel().getFeature( spec ) );
		return null;
	}
}
