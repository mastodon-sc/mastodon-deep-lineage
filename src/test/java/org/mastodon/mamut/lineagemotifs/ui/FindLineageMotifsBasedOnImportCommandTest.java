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
