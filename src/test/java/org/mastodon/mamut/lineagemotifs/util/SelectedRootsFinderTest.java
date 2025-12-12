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
package org.mastodon.mamut.lineagemotifs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

class SelectedRootsFinderTest
{

	private final ExampleGraph2 graph = new ExampleGraph2();

	@Test
	void testGetRoots_NoVerticesSelected_ReturnsEmptySet()
	{
		try (Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );
			RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );
			assertTrue( roots.isEmpty() );
		}
	}

	@Test
	void testGetRoots_OneVertexSelected()
	{
		try (Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );
			projectModel.getSelectionModel().setSelected( graph.spot2, true );

			RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );

			assertEquals( 1, roots.size() );
			assertTrue( roots.contains( graph.spot2 ) );
		}
	}

	@Test
	void testGetRoots_TwoRoots()
	{
		try (Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph.getModel(), context );

			projectModel.getSelectionModel().setSelected( graph.spot0, true );
			projectModel.getSelectionModel().setSelected( graph.spot1, true );
			projectModel.getSelectionModel().setSelected( graph.spot3, true );

			RefSet< Spot > roots = SelectedRootsFinder.getRoots( graph.getModel().getGraph(), projectModel.getSelectionModel() );

			assertEquals( 2, roots.size() );
			assertTrue( roots.contains( graph.spot0 ) );
			assertTrue( roots.contains( graph.spot3 ) );
		}
	}
}
