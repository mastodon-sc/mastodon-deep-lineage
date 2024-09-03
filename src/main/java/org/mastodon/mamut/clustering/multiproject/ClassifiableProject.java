package org.mastodon.mamut.clustering.multiproject;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;

import java.io.File;
import java.util.List;

/**
 * Small helper class to hold a project file, its {@link ProjectModel} and the trees that are to be classified.
 */
public class ClassifiableProject
{
	private final File file;

	private final ProjectModel projectModel;

	private final List< BranchSpotTree > trees;

	public ClassifiableProject( final File file, final ProjectModel projectModel, final List< BranchSpotTree > trees )
	{
		this.file = file;
		this.projectModel = projectModel;
		this.trees = trees;
	}

	public File getFile()
	{
		return file;
	}

	public ProjectModel getProjectModel()
	{
		return projectModel;
	}

	public List< BranchSpotTree > getTrees()
	{
		return trees;
	}
}
