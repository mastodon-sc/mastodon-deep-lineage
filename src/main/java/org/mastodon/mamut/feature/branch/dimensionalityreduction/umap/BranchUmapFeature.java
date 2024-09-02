package org.mastodon.mamut.feature.branch.dimensionalityreduction.umap;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.dimensionalityreduction.umap.feature.AbstractUmapFeature;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.List;

/**
 * Represents a UMAP feature for BranchSpots in the Mastodon project.
 * <br>
 * This feature is used to store the UMAP outputs for BranchSpots.
 * <br>
 * The UMAP outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the UMAP output.
 */
public class BranchUmapFeature extends AbstractUmapFeature< BranchSpot >
{
	public static final String KEY = "Branch Umap outputs";

	private final BranchSpotUmapFeatureSpec adaptedSpec;

	public static final BranchSpotUmapFeatureSpec GENERIC_SPEC = new BranchSpotUmapFeatureSpec();

	public BranchUmapFeature( final List< DoublePropertyMap< BranchSpot > > umapOutputMaps )
	{
		super( umapOutputMaps );
		FeatureProjectionSpec[] projectionSpecs =
				projectionMap.keySet().stream().map( FeatureProjectionKey::getSpec ).toArray( FeatureProjectionSpec[]::new );
		this.adaptedSpec = new BranchSpotUmapFeatureSpec( projectionSpecs );
	}

	@Plugin( type = FeatureSpec.class )
	public static class BranchSpotUmapFeatureSpec extends FeatureSpec< BranchUmapFeature, BranchSpot >
	{
		public BranchSpotUmapFeatureSpec()
		{
			super( KEY, HELP_STRING, BranchUmapFeature.class, BranchSpot.class, Multiplicity.SINGLE );
		}

		public BranchSpotUmapFeatureSpec( final FeatureProjectionSpec... projectionSpecs )
		{
			super( KEY, HELP_STRING, BranchUmapFeature.class, BranchSpot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}

	@Override
	public FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > getSpec()
	{
		return adaptedSpec;
	}
}
