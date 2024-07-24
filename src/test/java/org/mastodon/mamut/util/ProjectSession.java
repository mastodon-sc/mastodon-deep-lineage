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
