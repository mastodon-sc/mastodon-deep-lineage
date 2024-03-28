package org.mastodon.mamut.clustering.ui;

import org.mastodon.mamut.treesimilarity.tree.BranchSpotTree;

/**
 * Extends a {@link BranchSpotTree} with an extra label.
 */
public class BranchSpotTreeWithExtraLabel extends BranchSpotTree
{

	private final String extraLabel;

	/**
	 * Create a new {@link BranchSpotTreeWithExtraLabel} with the specified
	 * {@link BranchSpotTree} and the extra label.
	 *
	 * @param tree
	 *            the {@link BranchSpotTree} to extend.
	 * @param extraLabel
	 *            the extra label.
	 */
	public BranchSpotTreeWithExtraLabel( final BranchSpotTree tree, final String extraLabel )
	{
		super( tree );
		this.extraLabel = extraLabel;
	}

	@Override
	public String toString()
	{
		return super.toString().concat( extraLabel == null ? "" : " " + extraLabel );
	}
}
