package org.mastodon.mamut.feature.branch.successors;

import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.feature.branch.IntPropertyFeature;
import org.mastodon.mamut.feature.branch.IntPropertyFeatureComputer;
import org.mastodon.mamut.feature.branch.leaves.BranchNLeavesFeature;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.annotation.Nonnull;

/**
 * Computes {@link BranchNSuccessorsFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchNSuccessorsFeatureComputer extends IntPropertyFeatureComputer< BranchNSuccessorsFeature >
{
	@Parameter( type = ItemIO.OUTPUT )
	private BranchNSuccessorsFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchNSuccessorsFeature( new IntPropertyMap<>( branchGraph.vertices().getRefPool(), -1 ) );
	}

	@Override
	protected void computeIntProperty( @Nonnull BranchSpot vertex )
	{
		computeSuccessors( vertex );
	}

	private void computeSuccessors( @Nonnull BranchSpot vertex )
	{
		boolean isLeaf = vertex.outgoingEdges().isEmpty();
		if ( isLeaf )
			output.map.set( vertex, 0 );
		else
		{
			BranchSpot ref = branchGraph.vertexRef();
			int n = 0;
			for ( BranchLink link : vertex.outgoingEdges() )
			{
				BranchSpot child = link.getTarget( ref );
				n += 1 + output.map.get( child );
			}
			output.map.set( vertex, n );
			branchGraph.releaseRef( ref );
		}
	}
}
