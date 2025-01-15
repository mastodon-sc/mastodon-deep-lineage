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
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

/**
 * Represents a {@link AbstractExampleGraph} with the following {@link ModelGraph} and {@link ModelBranchGraph}:
 *
 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
 * <pre>
 *    Spot(0,X=0,Y=1,Z=0,tp=0)                       Spot(6,X=2,Y=2,Z=0,tp=0)
 *               │                                              │
 *    Spot(1,X=0,Y=2,Z=0,tp=1)                       Spot(7,X=2,Y=4,Z=0,tp=1)
 *               │                                              │
 *    Spot(2,X=0,Y=3,Z=0,tp=2)                       Spot(8,X=2,Y=6,Z=0,tp=2)
 *               │                                              │
 *    Spot(3,X=0,Y=4,Z=0,tp=3)                       Spot(9,X=2,Y=8,Z=0,tp=3)
 *               │                               ┌──────────────┴─────────────────┐
 *    Spot(4,X=0,Y=5,Z=0,tp=4)       Spot(10,X=2,Y=9,Z=0,tp=4)        Spot(12,X=4,Y=11,Z=0,tp=4)
 *               │                               │                                │
 *    Spot(5,X=0,Y=6,Z=0,tp=5)       Spot(11,X=2,Y=10,Z=0,tp=5)       Spot(13,X=4,Y=14,Z=0,tp=5)
 * </pre>
 * <h1>Branch-Graph (i.e. Graph of BranchSpots)</h1>
 * <pre>
 *   branchSpotA                     branchSpotB
 * 	                             ┌────────┴─────────┐
 * 	                             │                  │
 * 	                        branchSpotC        branchSpotD
 * </pre>
 */
public class ExampleGraph5 extends AbstractExampleGraph
{
	public final Spot spot0;

	public final Spot spot1;

	public final Spot spot2;

	public final Spot spot3;

	public final Spot spot4;

	public final Spot spot5;

	public final BranchSpot branchSpotA;

	public ExampleGraph5()
	{
		spot0 = addNode( "0", 0, new double[] { 0d, 1d, 0d } );
		spot1 = addNode( "1", 1, new double[] { 0d, 2d, 0d } );
		spot2 = addNode( "2", 2, new double[] { 0d, 3d, 0d } );
		spot3 = addNode( "3", 3, new double[] { 0d, 4d, 0d } );
		spot4 = addNode( "4", 4, new double[] { 0d, 5d, 0d } );
		spot5 = addNode( "5", 5, new double[] { 0d, 6d, 0d } );
		Spot spot6 = addNode( "6", 0, new double[] { 2d, 2d, 0d } );
		Spot spot7 = addNode( "7", 1, new double[] { 2d, 4d, 0d } );
		Spot spot8 = addNode( "8", 2, new double[] { 2d, 6d, 0d } );
		Spot spot9 = addNode( "9", 3, new double[] { 2d, 8d, 0d } );
		Spot spot10 = addNode( "10", 4, new double[] { 2d, 9d, 0d } );
		Spot spot11 = addNode( "11", 5, new double[] { 2d, 10d, 0d } );
		Spot spot12 = addNode( "12", 4, new double[] { 4d, 11d, 0d } );
		Spot spot13 = addNode( "13", 5, new double[] { 4d, 14d, 0d } );

		addEdge( spot0, spot1 );
		addEdge( spot1, spot2 );
		addEdge( spot2, spot3 );
		addEdge( spot3, spot4 );
		addEdge( spot4, spot5 );

		addEdge( spot6, spot7 );
		addEdge( spot7, spot8 );
		addEdge( spot8, spot9 );
		addEdge( spot9, spot10 );
		addEdge( spot9, spot12 );
		addEdge( spot10, spot11 );
		addEdge( spot12, spot13 );

		branchSpotA = getBranchSpot( spot0 );
	}
}
