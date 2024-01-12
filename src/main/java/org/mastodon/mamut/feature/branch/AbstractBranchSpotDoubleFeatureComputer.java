package org.mastodon.mamut.feature.branch;

import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.plugin.Parameter;

public abstract class AbstractBranchSpotDoubleFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{
	@Parameter
	protected Model model;

	@Override
	public void run()
	{
		super.deleteCancelReason();
		computeAll( getOutput() );
	}

	protected abstract AbstractDoublePropertyFeature< BranchSpot > getOutput();

	private void computeAll( final AbstractDoublePropertyFeature< BranchSpot > output )
	{
		for ( BranchSpot branchSpot : model.getBranchGraph().vertices() )
		{
			if ( isCanceled() )
				break;
			output.map.set( branchSpot, computeValue( branchSpot ) );
		}
	}

	protected abstract double computeValue( final BranchSpot branchSpot );
}