package org.mastodon.mamut.feature.branch;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Computes the sinuosity (cf. <a href="https://en.wikipedia.org/wiki/Sinuosity">Sinuosity</a>) of a Spot during an individual cell life cycle.
 * <p>
 *     <ul>
 *          <li>A sinuosity of 1 means that the cell moved in a straight line</li>
 *          <li> sinuosity of {@link Double#NaN} means that the cell did not move at all.</li>
 *          <li>A sinuosity > 1 means that the cell moved in a curved line. The higher, this value is, the "curvier" the cell has moved</li>
 *     </ul>
 */
public class BranchSinuosityFeature
		implements Feature< BranchSpot >
{
	public static final String KEY = "Branch Sinuosity";

	private static final String HELP_STRING =
			"Computes the directness of movement a spot during a single cell life cycle.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final BranchSinuosityFeature.Spec BRANCH_SINUOSITY_FEATURE_SPEC = new BranchSinuosityFeature.Spec();

	final DoublePropertyMap< BranchSpot > map;

	private final FeatureProjection< BranchSpot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchSinuosityFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchSinuosityFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	public BranchSinuosityFeature( final DoublePropertyMap< BranchSpot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	public double get( final BranchSpot branchSpot )
	{
		return map.getDouble( branchSpot );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public BranchSinuosityFeature.Spec getSpec()
	{
		return BRANCH_SINUOSITY_FEATURE_SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		map.remove( branchSpot );
	}
}
