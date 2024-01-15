package org.mastodon.mamut.feature;

import org.mastodon.graph.Vertex;
import org.scijava.plugin.Parameter;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractResettableFeatureComputer extends CancelableImpl implements MamutFeatureComputer
{

	@Parameter
	protected AtomicBoolean forceComputeAll;

	@Override
	public void run()
	{
		super.deleteCancelReason();
		final boolean recomputeAll = forceComputeAll.get();

		if ( recomputeAll )
			reset();
	}

	protected abstract void reset();
}
