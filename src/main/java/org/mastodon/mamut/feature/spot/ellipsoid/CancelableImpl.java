package org.mastodon.mamut.feature.spot.ellipsoid;

import org.scijava.Cancelable;

public class CancelableImpl implements Cancelable, Runnable
{
	private String cancelReason;

	@Override
	public void run()
	{
		cancelReason = null;
	}

	@Override
	public boolean isCanceled()
	{
		return null != cancelReason;
	}

	@Override
	public void cancel( final String reason )
	{
		cancelReason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}
}
