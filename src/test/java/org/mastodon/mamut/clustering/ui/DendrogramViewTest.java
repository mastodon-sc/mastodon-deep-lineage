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
import org.junit.Test;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;

public class DendrogramViewTest
{
	@Test
	public void testGetPanel()
	{
		Cluster cluster = new Cluster( "test" );
		Classification< BranchSpotTree > classification =
				new Classification<>( Collections.singletonList( Pair.of( null, cluster ) ), new Cluster( null ), 0d, 0d );
		DendrogramView< BranchSpotTree > dendrogramView = new DendrogramView<>( classification, "test" );
		DendrogramView< BranchSpotTree > dendrogramViewNull = new DendrogramView<>( null, "test" );
		assertNotNull( dendrogramView );
		assertNotNull( dendrogramView.getPanel() );
		assertNotNull( dendrogramViewNull );
		assertNotNull( dendrogramViewNull.getPanel() );
	}
}
