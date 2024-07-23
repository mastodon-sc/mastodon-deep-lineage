package org.mastodon.mamut.classification.multiproject;

import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.util.MastodonProjectService;
import org.mastodon.mamut.util.ProjectSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A collection of external projects.
 * <br>
 * This class manages a collection of external projects. It holds a mapping of project files to project sessions and a mapping of projects that failed to be loaded and the reason why they failed to load.
 */
public class ExternalProjects implements AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final MastodonProjectService projectService;

	private final Map< File, ProjectSession > projectSessions;

	private final Map< File, String > failingProjects;

	public ExternalProjects( final MastodonProjectService projectService )
	{
		this.projectService = projectService;
		this.projectSessions = new HashMap<>();
		this.failingProjects = new HashMap<>();
	}

	/**
	 * Gets all project models of the external projects
	 * @return the project models
	 */
	public Collection< ProjectModel > getProjectModels()
	{
		return projectSessions.values().stream().map( ProjectSession::getProjectModel ).collect( Collectors.toList() );
	}

	/**
	 * Gets a list of {@link ProjectSession}
	 * @return the list
	 */
	public List< ProjectSession > getProjectSessions()
	{
		return new ArrayList<>( projectSessions.values() );
	}

	/**
	 * Get the number of external projects
	 * @return the number of external projects
	 */
	public int size()
	{
		return projectSessions.size();
	}

	/**
	 * Check if the external projects is empty
	 * @return {@code true} if the external projects is empty, {@code false} otherwise
	 */
	public boolean isEmpty()
	{
		return projectSessions.isEmpty();
	}

	/**
	 * Get a Collection failing projects and the reason why they failed to load.
	 * @return the failing projects
	 */
	public Collection< String > getFailingProjectMessages()
	{
		return failingProjects.values();
	}

	/**
	 * Set the external projects
	 * @param projects the external projects
	 */
	public void setProjects( final File[] projects )
	{
		List< File > projectsList = projects == null ? Collections.emptyList() : Arrays.asList( projects );
		removeProjects( projectsList );
		cleanUpFailingProjects( projectsList );
		addProjects( projects );
		logger.debug( "Set {} projects. Active project sessions: {}.", projectsList.size(), projectService.activeSessions() );
	}

	/**
	 * Remove files from the externalProjects map that are not in the projects list
	 */
	private void removeProjects( final List< File > projectsList )
	{
		Iterator< Map.Entry< File, ProjectSession > > iterator = projectSessions.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			Map.Entry< File, ProjectSession > entry = iterator.next();
			File file = entry.getKey();
			if ( !projectsList.contains( file ) )
			{
				ProjectSession projectSession = entry.getValue();
				projectSession.close();
				iterator.remove();
			}
		}
	}

	/**
	 * Remove files from the failingExternalProjects map that are not in the projects list
	 */
	private void cleanUpFailingProjects( final List< File > projectsList )
	{
		for ( Map.Entry< File, String > entry : failingProjects.entrySet() )
		{
			File file = entry.getKey();
			if ( !projectsList.contains( file ) )
				failingProjects.remove( file );
		}
	}

	/**
	 * Add files from projects to the map if they are not already present
	 */
	private void addProjects( final File[] files )
	{
		if ( files == null )
			return;
		for ( File file : files )
		{
			if ( !projectSessions.containsKey( file ) )
			{
				try
				{
					projectSessions.put( file, projectService.createSession( file ) );
					failingProjects.remove( file );
				}
				catch ( SpimDataException | IOException | RuntimeException e )
				{
					failingProjects.put( file,
							"Could not read project from file " + file.getAbsolutePath() + ".<br>Error: " + e.getMessage() );
					logger.warn( "Could not read project from file {}. Error: {}", file.getAbsolutePath(), e.getMessage() );
				}
			}
		}
	}

	@Override
	public void close()
	{
		for ( ProjectSession projectSession : projectSessions.values() )
			projectSession.close();
		logger.debug( "Remaining active project sessions: {}.", projectService.activeSessions() );
	}
}
