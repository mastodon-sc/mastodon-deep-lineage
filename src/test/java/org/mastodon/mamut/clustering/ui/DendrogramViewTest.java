/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.clustering.util.HierarchicalClusteringResult;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.util.TagSetUtils;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DendrogramViewTest
{
	@Test
	void testInitCanvas()
	{
		Cluster cluster = new Cluster( "test" );
		HierarchicalClusteringResult< BranchSpotTree > hierarchicalClusteringResult =
				new HierarchicalClusteringResult<>( Collections.singletonList( Pair.of( null, cluster ) ), new Cluster( null ), 0d, 0d );
		DendrogramView< BranchSpotTree > dendrogramView = new DendrogramView<>( hierarchicalClusteringResult, "test" );
		DendrogramView< BranchSpotTree > dendrogramViewNull = new DendrogramView<>( null, "test" );
		assertNotNull( dendrogramView );
		assertDoesNotThrow( dendrogramView::initCanvas );
		assertNotNull( dendrogramViewNull );
		assertDoesNotThrow( dendrogramView::initCanvas );
	}

	@Test
	void testTagSetStructureChanged()
	{
		Cluster cluster = new Cluster( "test" );
		HierarchicalClusteringResult< BranchSpotTree > hierarchicalClusteringResult =
				new HierarchicalClusteringResult<>( Collections.singletonList( Pair.of( null, cluster ) ), new Cluster( null ), 0d, 0d );
		Model model = new Model();
		final String tagSetName = "tagSet";
		final String tagName1 = "tag1";
		final String tagName2 = "tag2";
		try (Context context = new Context())
		{
			PrefService prefService = context.getService( PrefService.class );
			DendrogramView< BranchSpotTree > dendrogramView =
					new DendrogramView<>( hierarchicalClusteringResult, "test", model, prefService, null );
			assertNotNull( prefService );
			assertNotNull( dendrogramView );
			assertDoesNotThrow( () -> TagSetUtils.addNewTagSetToModel( model,
					tagSetName, Arrays.asList( Pair.of( tagName1, 0x01020304 ), Pair.of( tagName2, 0x05060708 ) ) ) );
		}
	}
}
