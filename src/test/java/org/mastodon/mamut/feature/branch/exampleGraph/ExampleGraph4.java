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

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;

/**
 * Represents a {@link AbstractExampleGraphDeepLineage} with the following {@link ModelGraph} and {@link ModelBranchGraph}:
 *
 * <h1>Model-Graph (i.e. Graph of Spots)</h1>
 * <pre>
 *                     Spot(0,X=1,Y=2,Z=3,tp=0 )                             Spot(4,X=2,Y=4,Z=6,tp=0 )
 *                            │ link0                                               │ link3
 *                     Spot(1,X=3,Y=6,Z=9,tp=1 )                             Spot(5,X=4,Y=6,Z=8,tp=1 )
 *                            │                                                     │
 *            ┌───────────────┴───────────────┐                          ┌──────────┴──────────────────┐
 *            │ link1                         │ link2                    │ link4                       │ link5
 *     Spot(2,X=12,Y=24,Z=36,tp=2 )    Spot(3,X=4,Y=8,Z=12,tp=2 ) Spot(6,X=6,Y=10,Z=14,tp=2 )  Spot(7,X=8,Y=12,Z=16,tp=2 )

 * </pre>
 * <h1>Branch-Graph (i.e. Graph of BranchSpots)</h1>
 * <pre>
 *            branchSpotA			                branchSpotD
 * 	       ┌──────┴───────────┐	      	         ┌──────┴─────────────────┐
 * 	       │ branchLink0      │  branchLink1	 │ branchLink2            │ branchLink3
 * 	   branchSpotB         branchSpotC        branchSpotE    		   branchSpotF
 * </pre>
 */
public class ExampleGraph4 extends AbstractExampleGraphDeepLineage
{

	public final BranchSpot branchSpotA;

	public final BranchSpot branchSpotB;

	public final BranchSpot branchSpotC;

	public final BranchSpot branchSpotD;

	public final BranchSpot branchSpotE;

	public final BranchSpot branchSpotF;

	public final Spot spot0;

	public final Spot spot1;

	public final Spot spot2;

	public final Spot spot3;

	public final Spot spot4;

	public final Spot spot5;

	public final Spot spot6;

	public final Spot spot7;

	public final Link link0;

	public final Link link1;

	public final Link link2;

	public final Link link3;

	public final Link link4;

	public final Link link5;

	public final BranchLink branchLink0;

	public final BranchLink branchLink1;

	public final BranchLink branchLink2;

	public final BranchLink branchLink3;

	public ExampleGraph4()
	{
		spot0 = addNode( "0", 0, new double[] { 1d, 2d, 3d } );
		spot1 = addNode( "1", 1, new double[] { 3d, 6d, 9d } );
		spot2 = addNode( "2", 2, new double[] { 12d, 24d, 36d } );
		spot3 = addNode( "3", 2, new double[] { 4d, 8d, 12d } );
		spot4 = addNode( "4", 0, new double[] { 2d, 4d, 6d } );
		spot5 = addNode( "5", 1, new double[] { 4d, 6d, 8d } );
		spot6 = addNode( "6", 2, new double[] { 6d, 10d, 14d } );
		spot7 = addNode( "7", 2, new double[] { 8d, 12d, 16d } );

		link0 = addEdge( spot0, spot1 );
		link1 = addEdge( spot1, spot2 );
		link2 = addEdge( spot1, spot3 );
		link3 = addEdge( spot4, spot5 );
		link4 = addEdge( spot5, spot6 );
		link5 = addEdge( spot5, spot7 );

		branchSpotA = getBranchSpot( spot0 );
		branchSpotB = getBranchSpot( spot2 );
		branchSpotC = getBranchSpot( spot3 );
		branchSpotD = getBranchSpot( spot4 );
		branchSpotE = getBranchSpot( spot6 );
		branchSpotF = getBranchSpot( spot7 );

		branchLink0 = getBranchLink( link1 );
		branchLink1 = getBranchLink( link2 );
		branchLink2 = getBranchLink( link4 );
		branchLink3 = getBranchLink( link5 );

	}
}
