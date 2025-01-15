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
package org.mastodon.mamut.feature.branch.successors;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.ValueIsSetEvaluator;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Represents the total number of successors and predecessors of a branch spot in the whole track subtree of this branch spot.
 * <br>
 * In the following example this numbers would equal to following branchSpots as
 * follows:
 *
 * <pre>
 *                         branchSpot0
 *  	       ┌──────────────┴─────────────────┐
 *  	       │                                │
 *  	   branchspot1                      branchSpot2
 *  	┌──────┴───────┐
 *  	│              │
 *  branchspot3 branchSpot4
 * </pre>
 *
 * Successors:
 * <ul>
 * 	<li>{@code branchSpot0 = 4}</li>
 * 	<li>{@code branchSpot1 = 2}</li>
 * 	<li>{@code branchSpot2 = 0}</li>
 * 	<li>{@code branchSpot3 = 0}</li>
 * 	<li>{@code branchSpot4 = 0}</li>
 * </ul>
 * Predecessors:
 * <ul>
 *  <li>{@code branchSpot0 = 0}</li>
 *  <li>{@code branchSpot1 = 0}</li>
 *  <li>{@code branchSpot2 = 1}</li>
 *  <li>{@code branchSpot3 = 2}</li>
 *  <li>{@code branchSpot4 = 2}</li>
 * </ul>
 */
public class BranchNSuccessorsPredecessorsFeature implements Feature< BranchSpot >, ValueIsSetEvaluator< BranchSpot >
{
	public static final String KEY = "Branch N Successors and Predecessors";

	private static final String HELP_STRING = "Counts the successors and predecessors in the sub-tree of this branch spot.";

	public static final FeatureProjectionSpec SUCCESSORS_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Successors", Dimension.LENGTH );

	public static final FeatureProjectionSpec PREDECESSORS_PROJECTION_SPEC =
			new FeatureProjectionSpec( "Predecessors", Dimension.NONE );

	public final IntPropertyMap< BranchSpot > nSuccessors;

	public final IntPropertyMap< BranchSpot > nPredecessors;

	private final Map< FeatureProjectionKey, FeatureProjection< BranchSpot > > projectionMap;

	public static final Spec BRANCH_N_SUCCESSORS_PREDECESSORS_FEATURE = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< BranchNSuccessorsPredecessorsFeature, BranchSpot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					BranchNSuccessorsPredecessorsFeature.class,
					BranchSpot.class,
					Multiplicity.SINGLE,
					SUCCESSORS_PROJECTION_SPEC,
					PREDECESSORS_PROJECTION_SPEC );
		}
	}

	public BranchNSuccessorsPredecessorsFeature( final IntPropertyMap< BranchSpot > nSuccessors,
			final IntPropertyMap< BranchSpot > nPredecessors )
	{
		this.nSuccessors = nSuccessors;
		this.nPredecessors = nPredecessors;
		projectionMap = new LinkedHashMap<>( 2 );
		FeatureProjectionKey keySuccessors = key( SUCCESSORS_PROJECTION_SPEC );
		FeatureProjectionKey keyPredecessors = key( PREDECESSORS_PROJECTION_SPEC );
		projectionMap.put( keySuccessors, FeatureProjections.project( keySuccessors, nSuccessors, Dimension.NONE_UNITS ) );
		projectionMap.put( keyPredecessors, FeatureProjections.project( keyPredecessors, nPredecessors, Dimension.NONE_UNITS ) );
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
	public FeatureSpec< ? extends Feature< BranchSpot >, BranchSpot > getSpec()
	{
		return BRANCH_N_SUCCESSORS_PREDECESSORS_FEATURE;
	}

	@Override
	public void invalidate( final BranchSpot branchSpot )
	{
		nSuccessors.remove( branchSpot );
		nPredecessors.remove( branchSpot );
	}

	@Override
	public boolean valueIsSet( final BranchSpot vertex )
	{
		return nSuccessors.isSet( vertex ) && nPredecessors.isSet( vertex );
	}
}
