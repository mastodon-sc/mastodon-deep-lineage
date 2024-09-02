package org.mastodon.mamut.feature.dimensionalityreduction.umap.ui;

import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.TestUtils;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;

public class UmapViewDemo
{

	public static void main( String[] args ) throws IOException, SpimDataException
	{
		// Set Windows Look and Feel
		try
		{
			UIManager.setLookAndFeel( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
		}
		catch ( Exception ignored )
		{
			// ignore exception
		}

		try (Context context = new Context())
		{
			File tempFile1 = TestUtils.getTempFileCopy( "src/test/resources/org/mastodon/mamut/classification/model1.mastodon", "model",
					".mastodon" );
			ProjectModel projectModel = ProjectLoader.open( tempFile1.getAbsolutePath(), context, false, true );

			UmapView umapView = new UmapView( projectModel.getModel(), context );
			umapView.setVisible( true );
			umapView.setDefaultCloseOperation( UmapView.EXIT_ON_CLOSE );
		}
	}
}
