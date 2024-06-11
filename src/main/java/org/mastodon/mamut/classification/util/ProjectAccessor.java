package org.mastodon.mamut.classification.util;

import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used to manage a list of project files and their corresponding ProjectModel instances.
 * When instantiated, it loads the ProjectModels from the given project file objects.
 * <br>
 * It implements the close method from the AutoCloseable interface, which is used to close all loaded ProjectModel instances when they are no longer needed.
 */
public class ProjectAccessor implements AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final List< File > projectFiles;

	private final Context context;

	private final List< ProjectModel > projectModels;

	/**
	 * Loads the ProjectModel instances from the project files and stores the returned list internally.
	 *
	 * @param projectFiles A List of File objects representing the project files to load the ProjectModel instances from.
	 * @param context A Context object used for loading the ProjectModel instances.
	 */
	public ProjectAccessor( final List< File > projectFiles, final Context context )
	{
		this.projectFiles = projectFiles;
		this.context = context;
		this.projectModels = loadProjectModels();
	}

	/**
	* Retrieves the list of ProjectModel instances loaded from the project files.
	* <br>
	* This method returns the list of ProjectModel instances that were loaded when this ProjectAccessor instance was created.
	* The returned list is a reference to the internal list, not a copy. Therefore, changes to the returned list will affect the internal list.
	*
	* @return A List of ProjectModel instances loaded from the project files.
	*/
	public List< ProjectModel > getProjectModels()
	{
		return projectModels;
	}

	private List< ProjectModel > loadProjectModels()
	{
		List< ProjectModel > externalProjectModels = new ArrayList<>();
		for ( File project : projectFiles )
		{
			try
			{
				ProjectModel projectModel =
						ProjectLoader.open( project.getAbsolutePath(), context, false, true );
				externalProjectModels.add( projectModel );
			}
			catch ( IOException | SpimDataException e )
			{
				logger.warn( "Could not load project from file: {} ", project.getAbsolutePath(), e );
			}
		}
		return externalProjectModels;
	}

	@Override
	public void close()
	{
		for ( ProjectModel projectModel : projectModels )
		{
			projectModel.close();
		}
	}
}
