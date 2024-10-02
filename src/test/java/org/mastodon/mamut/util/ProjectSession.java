/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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

import org.mastodon.mamut.ProjectModel;

import java.io.File;

/**
 * A session for a Mastodon project.
 * <p>
 * This class represents a session for a Mastodon project. It holds the project model and the file the project was loaded from.
 * <p>
 * The session is created by the {@link MastodonProjectService} and should be closed by the client when it is no longer needed.
 */
public class ProjectSession implements AutoCloseable
{
	private final MastodonProjectService service;

	private final File file;

	private final ProjectModel projectModel;

	/**
	 * Creates a new project session.
	 * @param file the file the project was loaded from.
	 * @param projectModel the project model.
	 * @param service the service that created this session.
	 */
	ProjectSession( final File file, final ProjectModel projectModel, final MastodonProjectService service )
	{
		this.file = file;
		this.projectModel = projectModel;
		this.service = service;
	}

	/**
	 * Closes this session.
	 */
	@Override
	public void close()
	{
		service.releaseSession( this );
	}

	/**
	 * Gets the project model.
	 * @return the project model.
	 */
	public ProjectModel getProjectModel()
	{
		return projectModel;
	}

	/**
	 * Gets the file the project was loaded from.
	 * @return the file.
	 */
	public File getFile()
	{
		return file;
	}
}
