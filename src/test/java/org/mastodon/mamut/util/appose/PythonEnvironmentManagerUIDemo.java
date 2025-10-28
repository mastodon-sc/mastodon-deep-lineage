package org.mastodon.mamut.util.appose;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;

public class PythonEnvironmentManagerUIDemo
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static void main( String[] args )
	{
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( Exception e )
		{
			logger.error( e.getMessage(), e );
		}

		SwingUtilities.invokeLater( () -> {
			PythonEnvironmentManagerUI dialog = new PythonEnvironmentManagerUI( null );
			dialog.setVisible( true );
		} );
	}
}
