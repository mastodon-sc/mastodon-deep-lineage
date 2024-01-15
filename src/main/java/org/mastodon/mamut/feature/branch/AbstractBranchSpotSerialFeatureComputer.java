package org.mastodon.mamut.feature.branch;

import org.mastodon.mamut.feature.CancelableImpl;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.plugin.Parameter;

public abstract class AbstractBranchSpotSerialFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{
	@Parameter
	protected Model model;

	@Override
	public void run()
	{
		computeSerial();
	}

	private void computeSerial()
	{
		super.deleteCancelReason();
		createOutput();
		for ( BranchSpot branchSpot : model.getBranchGraph().vertices() )
		{
			if ( isCanceled() )
				break;
			compute( branchSpot );
		}
	}

	protected abstract void compute( final BranchSpot branchSpot );
}
