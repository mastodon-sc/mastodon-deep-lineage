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
 * Computes {@link BranchNSubBranchSpotsFeature}
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
			output =
					new BranchNSubBranchSpotsFeature( new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ) );
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

	private void numberOfNodes( BranchSpot branchSpot1, BranchSpot branchSpot2, BranchNSubBranchSpotsFeature output )
	{
		output.map.set( branchSpot1, 1 );
		BranchSpot branchSpot = branchGraph.vertexRef();
		for ( BranchLink branchLink : branchSpot1.outgoingEdges() )
		{
			BranchSpot childBranchSpot = branchLink.getTarget( branchSpot );

			// condition to omit reverse path from children to parent
			if ( childBranchSpot.equals( branchSpot2 ) )
				continue;

			// recursive call
			numberOfNodes( childBranchSpot, branchSpot1, output );

			// update count value of parent using its children
			output.map.set( branchSpot1, output.get( branchSpot1 ) + output.get( childBranchSpot ) );
		}
		branchGraph.releaseRef( branchSpot );
	}
}
