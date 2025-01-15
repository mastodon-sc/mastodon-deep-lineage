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
package org.mastodon.mamut.feature.branch.timepoints;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

/**
 * Represents the first and the last time point  of a branch in the lineage tree.
 * <br>
 * <br>
 * Cf. following example:
 * <br>
 * <br>
 * <strong>Model-Graph (i.e. Graph of Spots)</strong>
 *
 * <pre>
 *                                                Spot( 0, X=1,00, Y=2,00, Z=3,00, tp=0 )
 *                                                                   │
 *                                                                   │
 *                                                Spot( 1, X=2,00, Y=4,00, Z=6,00, tp=1 )
 *                                                                   │
 *                                                                   │
 *                                                Spot( 2, X=3,00, Y=6,00, Z=9,00, tp=2 )
 *                       ┌───────────────────────────────────────────┴──────────────────────┐
 *                       │                                                                  │
 *  Spot( 11, X=12,00, Y=24,00, Z=36,00, tp=3 )                         Spot( 3, X=4,00, Y=8,00, Z=12,00, tp=3 )
 *                       │                                                                  │
 *                       │                                                                  │
 *  Spot( 12, X=13,00, Y=26,00, Z=39,00, tp=4 )                         Spot( 4, X=5,00, Y=10,00, Z=15,00, tp=4 )
 *                       │                                            ┌─────────────────────┴─────────────────────┐
 *                       │                                            │                                           │
 *  Spot( 13, X=14,00, Y=28,00, Z=42,00, tp=5 )   Spot( 8, X=9,00, Y=18,00, Z=27,00, tp=5 )   Spot( 5, X=6,00, Y=12,00, Z=18,00, tp=5 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                               Spot( 9, X=10,00, Y=20,00, Z=30,00, tp=6 )   Spot( 6, X=7,00, Y=14,00, Z=21,00, tp=6 )
 *                                                                    │                                           │
 *                                                                    │                                           │
 *                                               Spot( 10, X=11,00, Y=22,00, Z=33,00, tp=7 )  Spot( 7, X=8,00, Y=16,00, Z=24,00, tp=7 )
 * </pre>
 *
 * <strong>Branch-Graph (i.e. Graph of BranchSpots)</strong>
 *
 * <pre>
 *                        branchSpotA
 * 	       ┌──────────────┴─────────────────┐
 * 	       │                                │
 * 	   branchSpotB                      branchSpotC
 * 	                                 ┌──────┴───────┐
 * 	                                 │              │
 *                                  branchSpotD    branchSpotE
 * </pre>
 *
 * <strong>First timepoints</strong>
 * <ul>
 * <li>{@code branchSpotA =  0}</li>
 * <li>{@code branchSpotB = 11}</li>
 * <li>{@code branchSpotC =  3}</li>
 * <li>{@code branchSpotD =  8}</li>
 * <li>{@code branchSpotE =  5}</li>
 * </ul>
 *
 * <strong>Last timepoints</strong>
 * <ul>
 * <li>{@code branchSpot0 =  2}</li>
 * <li>{@code branchSpot1 = 13}</li>
 * <li>{@code branchSpot2 =  4}</li>
 * <li>{@code branchSpot3 = 10}</li>
 * <li>{@code branchSpot4 =  7}</li>
 * </ul>
 */
public class BranchTimepointsFeature implements Feature< BranchSpot >
{
	public static final String KEY = "Branch Start and End Timepoints";

	private static final String INFO_STRING = "The first and the last timepoint of a branch.";

	public static final FeatureProjectionSpec START_PROJECTION_SPEC =
			new FeatureProjectionSpec( "First Timepoint", Dimension.NONE );

	public static final FeatureProjectionSpec END_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Last Timepoint", Dimension.NONE );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchTimepointsFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					BranchTimepointsFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					START_PROJECTION_SPEC,
					END_PROJECTION_SPEC
			);
		}
	}

	private final Map< FeatureProjectionKey, FeatureProjection< BranchSpot > > projectionMap;

	final IntPropertyMap< BranchSpot > startTimepointsMap;

	final IntPropertyMap< BranchSpot > endTimepointsMap;

	BranchTimepointsFeature( final IntPropertyMap< BranchSpot > startTimepointsMap, final IntPropertyMap< BranchSpot > endTimepointsMap )
	{
		this.startTimepointsMap = startTimepointsMap;
		this.endTimepointsMap = endTimepointsMap;
		this.projectionMap = new LinkedHashMap<>( 2 );
		projectionMap.put( key( START_PROJECTION_SPEC ),
				FeatureProjections.project( key( START_PROJECTION_SPEC ), startTimepointsMap, Dimension.NONE_UNITS ) );
		projectionMap.put( key( END_PROJECTION_SPEC ),
				FeatureProjections.project( key( END_PROJECTION_SPEC ), endTimepointsMap, Dimension.NONE_UNITS ) );
	}

	@Override
	public FeatureProjection< BranchSpot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< BranchSpot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final BranchSpot branch )
	{
		startTimepointsMap.remove( branch );
		endTimepointsMap.remove( branch );
	}
}
