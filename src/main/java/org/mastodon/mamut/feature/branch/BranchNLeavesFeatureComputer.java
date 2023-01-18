package org.mastodon.mamut.feature.branch;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.LeafFinder;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Computes {@link BranchNLeavesFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchNLeavesFeatureComputer implements MamutFeatureComputer
{
	@Parameter
	private ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchNLeavesFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchNLeavesFeature( new IntPropertyMap<>( branchGraph.vertices().getRefPool(), 0 ) );
	}

	@Override
	public void run()
	{
		computeNumberOfSubtreeLeaves();
	}

	private void computeNumberOfSubtreeLeaves()
	{
		final RefSet< BranchSpot > leaves = LeafFinder.getLeaves( branchGraph );
		for ( final BranchSpot leaf : leaves )
		{
			InverseDepthFirstIterator< BranchSpot, BranchLink > inverseDepthFirstIterator =
					new InverseDepthFirstIterator<>( branchGraph );
			inverseDepthFirstIterator.reset( leaf );
			while ( inverseDepthFirstIterator.hasNext() )
			{
				BranchSpot branchSpot = inverseDepthFirstIterator.next();
				output.map.set( branchSpot, output.map.get( branchSpot ) + 1 );
			}
		}
	}
}
