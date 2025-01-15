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
package org.mastodon.mamut.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An SciJava service that manages Mastodon projects.
 * <br>
 * This service is capable of loading, giving access and closing Mastodon projects, when no longer needed.
 * <br>
 * The motivation for this service is that loading a Mastodon project is a time and memory consuming operation.
 * Thus, it is beneficial to keep the project models that have been opened in memory and share it among different parts of the application.
 * <br>
 * The service will keep track of all project models that have been opened and the sessions that are currently using them.
 * For this purpose, the service provides a method to create a new session for a project file and a method to release a session.
 * <br>
 * When a session is released, the service will close the project model, if no other session is using it.
 */
@Plugin( type = Service.class )
public class MastodonProjectService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final BiMap< File, ProjectModel > projectModels;

	private final Map< ProjectModel, List< ProjectSession > > sessions;

	/**
	 * Creates a new Mastodon project service.
	 */
	public MastodonProjectService()
	{
		projectModels = HashBiMap.create();
		sessions = new HashMap<>();
	}

	/**
	 * Creates a new session for the project file.
	 * <br>
	 * If the project model for the file has already been loaded, the existing project model is used.
	 * <br>
	 * If the project model has not been loaded yet, it is loaded from the file.
	 *
	 * @param file the project file to create a session for.
	 * @return the project session.
	 * @throws SpimDataException if the project file could not be loaded.
	 * @throws IOException if the project file could not be loaded.
	 */
	public ProjectSession createSession( final File file ) throws SpimDataException, IOException
	{
		// deliver existing project model, if it exists
		if ( projectModels.containsKey( file ) )
		{
			ProjectModel projectModel = projectModels.get( file );
			ProjectSession projectSession = new ProjectSession( file, projectModel, this );
			sessions.get( projectModel ).add( projectSession );
			return projectSession;
		}

		// load project model from file
		long start = System.currentTimeMillis();
		ProjectModel projectModel =
				ProjectLoader.open( file.getAbsolutePath(), getContext(), false, true );
		logger.debug( "Loaded project from file: {} in {} ms", file.getAbsolutePath(), System.currentTimeMillis() - start );
		ProjectSession projectSession = new ProjectSession( file, projectModel, this );
		projectModels.put( file, projectModel );
		List< ProjectSession > projectSessionList = new ArrayList<>();
		projectSessionList.add( projectSession );
		sessions.put( projectModel, projectSessionList );
		return projectSession;
	}

	/**
	 * Releases the session.
	 * <br>
	 * If the project model is not used by any other session, the project model is closed.
	 * @param projectSession the session to release.
	 */
	public void releaseSession( final ProjectSession projectSession )
	{
		ProjectModel projectModel = projectSession.getProjectModel();
		List< ProjectSession > projectSessionList = sessions.get( projectModel );
		projectSessionList.remove( projectSession );
		if ( projectSessionList.isEmpty() )
		{
			projectModel.close();
			projectModels.inverse().remove( projectModel );
			sessions.remove( projectModel );
		}
	}

	/**
	 * Returns the number of active sessions.
	 * @return the number of active sessions.
	 */
	public int activeSessions()
	{
		return sessions.size();
	}
}
