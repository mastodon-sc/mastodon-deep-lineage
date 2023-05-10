package org.mastodon.mamut.feature.spot.ellipsoid;

import org.scijava.Cancelable;

public class CancelableImpl implements Cancelable
{
	private String cancelReason;

	public void deleteCancelReason()
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
