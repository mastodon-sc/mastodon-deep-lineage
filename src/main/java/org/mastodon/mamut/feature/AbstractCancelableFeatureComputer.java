package org.mastodon.mamut.feature;

public abstract class AbstractCancelableFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{
	@Override
	public void run()
	{
		super.deleteCancelReason();
	}
}
