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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.TestUtils;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;
import org.scijava.command.DynamicCommand;

import mpicbg.spim.data.SpimDataException;

class FindLineageMotifsBasedOnImportCommandTest
{
	@Test
	void testFindLinageMotifsCommand()
			throws NoSuchFieldException, IllegalAccessException, IOException, SpimDataException
	{
		try (final Context context = new Context())
		{
			File demoProjectFile = TestUtils.getTempFileCopy(
					"src/test/resources/org/mastodon/mamut/lineagemotifs/util/lineage_motifs.mastodon", "model",
					".mastodon"
			);
			File graphMLFile = TestUtils.getTempFileCopy(
					"src/test/resources/org/mastodon/mamut/lineagemotifs/util/motif.graphml", "motif",
					".graphml"
			);
			ProjectModel projectModel = ProjectLoader.open( demoProjectFile.getAbsolutePath(), context, false, true );
			Model model = projectModel.getModel();
			Spot spotRef = model.getGraph().vertexRef();
			BranchSpot branchSpotRef = model.getBranchGraph().vertexRef();
			try
			{
				FindLineageMotifsBasedOnImportCommand findLineageMotifsBasedOnImportCommand =
						new FindLineageMotifsBasedOnImportCommand();
				Field contextField = DynamicCommand.class.getDeclaredField( "context" );
				contextField.setAccessible( true );
				contextField.set( findLineageMotifsBasedOnImportCommand, context );
				Field projectModelField = AbstractFindLineageMotifsCommand.class.getDeclaredField( "projectModel" );
				projectModelField.setAccessible( true );
				projectModelField.set( findLineageMotifsBasedOnImportCommand, projectModel );
				Field motifFileField = FindLineageMotifsBasedOnImportCommand.class.getDeclaredField( "motifFile" );
				motifFileField.setAccessible( true );
				motifFileField.set( findLineageMotifsBasedOnImportCommand, graphMLFile );
				assertEquals( 2, projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().size() );

				findLineageMotifsBasedOnImportCommand.run();

				assertNotNull( findLineageMotifsBasedOnImportCommand );
				Awaitility.await().atMost( 5, TimeUnit.SECONDS )
						.until( () -> projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().size() == 3 );
				assertEquals( 10, projectModel.getModel().getTagSetModel().getTagSetStructure().getTagSets().get( 2 ).getTags().size() );

			}
			finally
			{
				model.getGraph().releaseRef( spotRef );
				model.getBranchGraph().releaseRef( branchSpotRef );
				projectModel.close();
			}
		}
	}
}
