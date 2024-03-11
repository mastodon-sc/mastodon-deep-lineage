package org.mastodon.mamut.feature;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.model.Model;
import org.scijava.plugin.Parameter;

import java.util.Collection;

public abstract class AbstractSerialFeatureComputer< V extends Vertex< ? > > extends AbstractResettableFeatureComputer
{
	@Parameter
	protected Model model;

	@Parameter
	private FeatureComputationStatus status;

	@Override
	public void run()
	{
		super.run();
		computeSerial();
	}

	private void computeSerial()
	{
		createOutput();
		Collection< V > vertices = getVertices();
		int done = 0;
		final int numberOfVertices = vertices.size();
		for ( V vertex : vertices )
		{
			if ( isCanceled() )
				break;
			int outputRate = ( int ) Math.pow( 10, Math.floor( Math.log10( numberOfVertices ) ) );
			// Limit overhead by only update progress every ~10%.
			if ( done++ % outputRate == 0 )
				notifyProgress( done, numberOfVertices );
			// Skip if we are not forced to recompute all and if a value is already computed.
			if ( forceComputeAll.get() || !getEvaluator().valueIsSet( vertex ) )
				compute( vertex );
		}
		notifyProgress( numberOfVertices, numberOfVertices );
	}

	protected void notifyProgress( final int finished, final int total )
	{
		status.notifyProgress( ( double ) finished / total );
	}

	protected abstract void compute( final V vertex );

	protected abstract ValueIsSetEvaluator< V > getEvaluator();

	protected abstract Collection< V > getVertices();
}
