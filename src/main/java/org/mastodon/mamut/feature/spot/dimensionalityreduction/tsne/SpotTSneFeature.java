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
package org.mastodon.mamut.feature.spot.dimensionalityreduction.tsne;

import java.util.List;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.dimensionalityreduction.tsne.feature.AbstractTSneFeature;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents a t-SNE feature for spots in the Mastodon project.
 * <br>
 * This feature is used to store the t-SNE outputs for spots.
 * <br>
 * The t-SNE outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the t-SNE output.
 */
public class SpotTSneFeature extends AbstractTSneFeature< Spot >
{
	public static final String KEY = "Spot t-SNE outputs";

	private final SpotTSneFeatureSpec adaptedSpec;

	public static final SpotTSneFeatureSpec GENERIC_SPEC = new SpotTSneFeatureSpec();

	public SpotTSneFeature( final List< DoublePropertyMap< Spot > > outputMaps )
	{
		super( outputMaps );
		FeatureProjectionSpec[] projectionSpecs =
				projectionMap.keySet().stream().map( FeatureProjectionKey::getSpec ).toArray( FeatureProjectionSpec[]::new );
		this.adaptedSpec = new SpotTSneFeatureSpec( projectionSpecs );
	}

	@Plugin( type = FeatureSpec.class )
	public static class SpotTSneFeatureSpec extends FeatureSpec< SpotTSneFeature, Spot >
	{
		public SpotTSneFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotTSneFeature.class, Spot.class, Multiplicity.SINGLE );
		}

		public SpotTSneFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, SpotTSneFeature.class, Spot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	@Override
	public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
	{
		return adaptedSpec;
	}
}
