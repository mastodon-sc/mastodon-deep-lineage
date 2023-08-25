package org.mastodon.mamut.feature.spot;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SpotBranchSpotIDFeatureTest extends AbstractFeatureTest< Spot >
{
	private Feature< Spot > feature;

	private final ExampleGraph2 graph = new ExampleGraph2();

	@Before
	public void setUp()
	{
		try (Context context = new Context())
		{
			feature = FeatureComputerTestUtils.getFeature( context, graph.getModel(), SpotBranchSpotIDFeature.SPEC );
		}
	}

	@Override
	@Test
	public void testFeatureComputation()
	{
		FeatureProjection< Spot > featureProjection = getProjection( feature, SpotBranchSpotIDFeature.PROJECTION_SPEC );
		assertEquals( graph.branchSpotA.getInternalPoolIndex(), ( int ) featureProjection.value( graph.spot0 ) );
		assertEquals( graph.branchSpotE.getInternalPoolIndex(), ( int ) featureProjection.value( graph.spot10 ) );
	}

	@Override
	@Test
	public void testFeatureSerialization() throws IOException
	{
		SpotBranchSpotIDFeature featureReloaded;
		try (Context context = new Context())
		{
			featureReloaded = ( SpotBranchSpotIDFeature ) FeatureSerializerTestUtils.saveAndReload( context, graph.getModel(), feature );
		}
		// check that the feature has correct values after saving and reloading
		assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( feature, featureReloaded,
				Collections.singleton( graph.spot0 )
		) );
	}

	@Override
	@Test
	public void testFeatureInvalidate()
	{
		Spot spot = graph.spot0;
		// test, if features have a non "-1" value before invalidation
		assertNotEquals( -1, ( int ) getProjection( feature, SpotBranchSpotIDFeature.PROJECTION_SPEC ).value( spot ) );

		// invalidate feature
		feature.invalidate( spot );

		// test, if features are "-1" after invalidation
		assertEquals( -1, ( int ) getProjection( feature, SpotBranchSpotIDFeature.PROJECTION_SPEC ).value( spot ) );
	}
}
