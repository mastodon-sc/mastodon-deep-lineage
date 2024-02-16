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

	/**
	 * Get a feature from the model that matches the given predicate and feature spec class.
	 * <p>
	 * If no feature matches the predicate, returns null.
	 * <p>
	 * If multiple features match the predicate, returns the first one.
	 *
	 * @param model the model to get the feature from.
	 * @param predicate the predicate to match the feature.
	 * @param featureSpecClass the feature spec class.
	 * @return the feature that matches the predicate and feature spec class, or null if none matches.
	 *
	 * @param <V> the vertex type.
	 * @param <F> the feature type.
	 * @param <S> the feature spec type.
	 */
	public static < V extends Vertex< ? >, F extends Feature< V >, S extends FeatureSpec< F, V > > F getFeature( final Model model,
			final Predicate< F > predicate, final Class< S > featureSpecClass )
	{

		S spec = getFeatureSpecGeneric( model, predicate, featureSpecClass );
		if ( spec != null )
			return Cast.unchecked( model.getFeatureModel().getFeature( spec ) );
		return null;
	}

	/**
	 * Gets a feature from the model that matches the given feature spec class.
	 * <p>
	 * If no feature matches, returns null.
	 * <p>
	 * If multiple features match the predicate, returns the first one.
	 *
	 * @param model the model to get the feature from.
	 * @param featureSpecClass the feature spec class.
	 * @return the feature that matches the predicate and feature spec class, or null if none matches.
	 *
	 * @param <V> the vertex type.
	 * @param <F> the feature type.
	 * @param <S> the feature spec type.
	 */
	public static < V extends Vertex< ? >, F extends Feature< V >, S extends FeatureSpec< F, V > > F getFeature( final Model model,
			final Class< S > featureSpecClass )
	{
		Predicate< F > predicate = Objects::nonNull;
		S spec = getFeatureSpecGeneric( model, predicate, featureSpecClass );
		if ( spec != null )
			return Cast.unchecked( model.getFeatureModel().getFeature( spec ) );
		return null;
	}
}
