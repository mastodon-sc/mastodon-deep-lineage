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
package org.mastodon.mamut.lineagemotifs.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.ui.ClusterLineagesCommand;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.lineagemotifs.util.LineageMotifsUtils;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.SelectionModel;
import org.scijava.Context;
import org.scijava.thread.ThreadService;

class FindLinageMotifsCommandTest
{

	@Test
	void testFindLinageMotifsCommand() throws NoSuchFieldException, IllegalAccessException
	{
		try (final Context context = new Context())
		{
			final ExampleGraph2 graph2 = new ExampleGraph2();
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph2.getModel(), context );
			ThreadService threadService = context.getService( ThreadService.class );

			FindLineageMotifsCommand findLineageMotifsCommand = new FindLineageMotifsCommand();

			Field projectModelField = FindLineageMotifsCommand.class.getDeclaredField( "projectModel" );
			projectModelField.setAccessible( true );
			projectModelField.set( findLineageMotifsCommand, projectModel );
			Field threadServiceField = FindLineageMotifsCommand.class.getDeclaredField( "threadService" );
			threadServiceField.setAccessible( true );
			threadServiceField.set( findLineageMotifsCommand, threadService );

			SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();

			List< String > list =
					Arrays.asList( "218", "219", "220", "221", "222", "223", "224", "225", "226", "227", "228", "229", "230",
							"231", "232", "255", "256", "257", "258", "259", "260", "261", "262", "263", "264", "265", "266", "267",
							"268", "269", "270", "271", "272", "273", "274" );

			for ( Spot spot : graph2.getModel().getGraph().vertices() )
			{
				if ( list.contains( spot.getLabel() ) )
					selectionModel.setSelected( spot, true );
			}

			findLineageMotifsCommand.run();
			assertNotNull( findLineageMotifsCommand );
			assertEquals( 6, projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().size() );
		}
	}
}
