package org.mastodon.mamut.feature.spot.dimensionalityreduction.umap;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.List;

/**
 * Represents a UMAP feature for spots in the Mastodon project.
 * <br>
 * This feature is used to store the UMAP outputs for spots.
 * <br>
 * The UMAP outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the UMAP output.
 */
public class SpotUmapFeature extends AbstractUmapFeature< Spot >
{
	public static final String KEY = "Spot Umap outputs";

	private final SpotUmapFeatureSpec adaptedSpec;

	public static final SpotUmapFeatureSpec GENERIC_SPEC = new SpotUmapFeatureSpec();

	public SpotUmapFeature( final List< DoublePropertyMap< Spot > > umapOutputMaps )
	{
		super( umapOutputMaps );
		FeatureProjectionSpec[] projectionSpecs =
				projectionMap.keySet().stream().map( FeatureProjectionKey::getSpec ).toArray( FeatureProjectionSpec[]::new );
		this.adaptedSpec = new SpotUmapFeatureSpec( projectionSpecs );
	}

	@Plugin( type = FeatureSpec.class )
	public static class SpotUmapFeatureSpec extends FeatureSpec< SpotUmapFeature, Spot >
	{
		public SpotUmapFeatureSpec()
		{
			super( KEY, HELP_STRING, SpotUmapFeature.class, Spot.class, Multiplicity.SINGLE );
		}

		public SpotUmapFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, SpotUmapFeature.class, Spot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	@Override
	public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
	{
		return adaptedSpec;
	}
}
