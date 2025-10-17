/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.feature;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.model.Model;
import org.scijava.plugin.Parameter;

import java.util.Collection;

/**
 * Abstract class for computing features in a serial way, i.e., one vertex after
 * the other.
 * @param <V> the type of vertex.
 */
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
