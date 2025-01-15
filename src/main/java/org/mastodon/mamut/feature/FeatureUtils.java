/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
