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
package org.mastodon.mamut.feature.branch.exampleGraph;

import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Represents a {@link AbstractExampleGraph} with the following {@link ModelGraph}:
 *
 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
 * <pre>
 * Spot( 0, X=1, Y=1, tp=0 )        Spot( 1, X=0, Y=1, tp=0 )       Spot( 2, X=2, Y=1, tp=0 )
 *
 * Spot( 0, X=1, Y=2, tp=1 )        Spot( 1, X=0, Y=0, tp=1 )       Spot( 2, X=2, Y=0, tp=1 )
 *
 * Spot( 0, X=1, Y=3, tp=2 )        Spot( 1, X=0, Y=-1, tp=2 )      Spot( 3, X=2, Y=-1, tp=2 )
 *
 * Spot( 0, X=1, Y=4, tp=3 )        Spot( 0, X=0, Y=-2, tp=3 )
 * </pre>
 */
public class ExampleGraph6 extends AbstractExampleGraph
{

	public final Spot spot0;

	public final Spot spot1;

	public final Spot spot2;

	public final Spot spot3;

	public final Spot spot4;

	public final Spot spot5;

	public final Spot spot6;

	public final Spot spot7;

	public final Spot spot8;

	public final Spot spot9;

	public final Spot spot10;

	public ExampleGraph6()
	{
		spot0 = addNode( "0", 0, new double[] { 1d, 1d, 0d } );
		spot1 = addNode( "0", 1, new double[] { 1d, 2d, 0d } );
		spot2 = addNode( "0", 2, new double[] { 1d, 3d, 0d } );
		spot3 = addNode( "1", 0, new double[] { 0d, 1d, 0d } );
		spot4 = addNode( "1", 1, new double[] { 0d, 0d, 0d } );
		spot5 = addNode( "1", 2, new double[] { 0d, -1d, 0d } );
		spot6 = addNode( "2", 0, new double[] { 2d, 1d, 0d } );
		spot7 = addNode( "2", 1, new double[] { 2d, 0d, 0d } );
		spot8 = addNode( "3", 2, new double[] { 2d, -1d, 0d } );
		spot9 = addNode( "0", 3, new double[] { 1d, 4d, 0d } );
		spot10 = addNode( "0", 3, new double[] { 0d, -2d, 0d } );
	}
}
