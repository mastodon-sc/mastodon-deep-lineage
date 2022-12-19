package org.mastodon.mamut.feature.branch;

import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Computes the total number of successors of a branch spot in the whole track
 * sub-tree of this branch spot.
 *
 * In the following example this number would equal to following branchSpots as
 * follows:
 * 
 * <pre>
 *                         branchSpot0
 *  	       ┌──────────────┴─────────────────┐
 *  	       │                                │
 *  	   branchspot1                      branchSpot2
 *  	┌──────┴───────┐
 *  	│              │
 *  branchspot3 branchSpot4
 * </pre>
 * 
 * <ul>
 * <li>{@code branchSpot0 = 4}</li>
 * <li>{@code branchSpot1 = 2}</li>
 * <li>{@code branchSpot2 = 0}</li>
 * <li>{@code branchSpot3 = 0}</li>
 * <li>{@code branchSpot4 = 0}</li>
 * </ul>
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchNSubBranchSpotsFeatureComputer implements MamutFeatureComputer
{
	@Parameter
	private ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchNSubBranchSpotsFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchNSubBranchSpotsFeature( new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		computeNumberOfSubtreeNodes();
	}

	private void computeNumberOfSubtreeNodes()
	{
		final RefSet< BranchSpot > roots = RootFinder.getRoots( branchGraph );
		for ( final BranchSpot root : roots )
		{
			numberOfNodes( root, null, output );
		}
		output.map.getMap().forEachKey( object -> {
			output.map.set( object, output.map.get( object ) - 1 );
			return true;
		} );
	}

	private static void numberOfNodes(BranchSpot branchSpot1, BranchSpot branchSpot2, BranchNSubBranchSpotsFeature output)
	{
		output.map.set( branchSpot1, 1 );
		for ( BranchLink branchLink: branchSpot1.outgoingEdges() )
		{
			BranchSpot childBranchSpot = branchLink.getTarget();

			// condition to omit reverse path from children to parent
			if(childBranchSpot.equals( branchSpot2 ))
				continue;

			// recursive call
			numberOfNodes(childBranchSpot, branchSpot1, output);

			// update count value of parent using its children
			output.map.set( branchSpot1, output.get( branchSpot1 ) + output.get(childBranchSpot) );
		}
	}
}
