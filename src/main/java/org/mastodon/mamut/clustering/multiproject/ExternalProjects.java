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
package org.mastodon.mamut.clustering.multiproject;

import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of external projects.
 * <br>
 * This class manages a collection of external projects. It holds a mapping of project files to project sessions and a mapping of projects that failed to be loaded and the reason why they failed to load.
 */
public class ExternalProjects implements AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Context context;

	private final Map< File, ProjectModel > projects;

	private final Map< File, String > failingProjects;

	public ExternalProjects( final Context context )
	{
		this.context = context;
		this.projects = new HashMap<>();
		this.failingProjects = new HashMap<>();
	}

	/**
	 * Gets all project models of the external projects
	 * @return the project models
	 */
	public Collection< ProjectModel > getProjectModels()
	{
		return projects.values();
	}

	public Set< Map.Entry< File, ProjectModel > > getProjects()
	{
		return projects.entrySet();
	}

	/**
	 * Get the number of external projects
	 * @return the number of external projects
	 */
	public int size()
	{
		return projects.size();
	}

	/**
	 * Check if the external projects is empty
	 * @return {@code true} if the external projects is empty, {@code false} otherwise
	 */
	public boolean isEmpty()
	{
		return projects.isEmpty();
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
	public void setProjects( final File[] projects, final File currentProject )
	{
		List< File > projectsList = projects == null ? Collections.emptyList() : Arrays.asList( projects );
		projectsList = projectsList.stream().distinct().collect( Collectors.toList() ); // remove duplicates
		projectsList.remove( currentProject ); // remove current project
		removeProjects( projectsList );
		cleanUpFailingProjects( projectsList );
		addProjects( projects );
		logger.debug( "Set {} external projects.", projectsList.size() );
	}

	/**
	 * Remove files from the externalProjects map that are not in the projects list
	 */
	private void removeProjects( final List< File > projectsList )
	{
		Iterator< Map.Entry< File, ProjectModel > > iterator = projects.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			Map.Entry< File, ProjectModel > entry = iterator.next();
			File file = entry.getKey();
			if ( !projectsList.contains( file ) )
			{
				ProjectModel projectModel = entry.getValue();
				projectModel.close();
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
			if ( !projects.containsKey( file ) )
			{
				try
				{
					// load project model from file
					long start = System.currentTimeMillis();
					ProjectModel projectModel =
							ProjectLoader.open( file.getAbsolutePath(), context, false, true );
					logger.debug( "Loaded project from file: {} in {} ms", file.getAbsolutePath(), System.currentTimeMillis() - start );
					projects.put( file, projectModel );
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
		for ( ProjectModel projectModel : projects.values() )
			projectModel.close();
	}
}
