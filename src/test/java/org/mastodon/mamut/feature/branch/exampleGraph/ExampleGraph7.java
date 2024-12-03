/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.feature.branch.exampleGraph;

import java.util.Random;

import org.mastodon.mamut.model.Spot;

public class ExampleGraph7 extends AbstractExampleGraph
{
	private int counter = 0;

	private final Random random = new Random( 42 );

	public ExampleGraph7()
	{
		for ( int i = 0; i < 50; i++ )
		{
			addBranchSpot();
		}
	}

	private void addBranchSpot()
	{

		Spot spot0 = addSpot( 0 );
		counter++;
		Spot spot1 = addSpot( 1 );
		counter++;
		Spot spot2 = addSpot( 2 );
		counter++;

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );

		getBranchSpot( spot0 );
	}

	private Spot addSpot( int timepoint )
	{
		int range = 20;
		return addNode( String.valueOf( counter ), timepoint,
				new double[] { random.nextInt( range ), random.nextInt( range ), random.nextInt( range ) } );
	}
}
