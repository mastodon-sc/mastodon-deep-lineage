package org.mastodon.mamut.feature.branch.leaves;

import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * Computes {@link BranchNLeavesFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchNLeavesFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{
	@Parameter
	protected ModelBranchGraph branchGraph;

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
		LineageTreeUtils.callDepthFirst( branchGraph, this::computeLeaves, this::isCanceled );
	}

	private void computeLeaves( @Nonnull BranchSpot vertex )
	{
		boolean isLeaf = vertex.outgoingEdges().isEmpty();
		if ( isLeaf )
			output.map.set( vertex, 1 );
		else
		{
			BranchSpot ref = branchGraph.vertexRef();
			int n = 0;
			for ( BranchLink link : vertex.outgoingEdges() )
			{
				BranchSpot child = link.getTarget( ref );
				n += output.map.get( child );
			}
			output.map.set( vertex, n );
			branchGraph.releaseRef( ref );
		}
	}
}
