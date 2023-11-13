/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.feature.spot.ellipsoid;

import org.mastodon.mamut.feature.AbstractFeatureTest;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public abstract class AbstractEllipsoidFeatureTest extends AbstractFeatureTest< Spot >
{
	protected final Model model = new Model();

	protected final ModelGraph graph = model.getGraph();

	protected final Spot spot = initEllipsoidSpot( graph );

	// eigenvalues for given covariance matrix
	// cf. https://matrixcalc.org/de/vectors.html#eigenvectors({{6, 2, 3}, {2, 7, 4}, {3, 4, 8}})
	protected final double[] eigenValues = new double[] { 3.2695842d, 4.4422001d, 13.2882158d };

	protected final double expectedLongAxis = Math.sqrt( eigenValues[ 2 ] );

	protected final double expectedMiddleAxis = Math.sqrt( eigenValues[ 1 ] );

	// compute semi-axes from eigenvalues
	protected final double expectedShortAxis = Math.sqrt( eigenValues[ 0 ] );

	private static Spot initEllipsoidSpot( ModelGraph graph )
	{
		Spot spot = graph.addVertex();
		spot.init( 1, new double[] { 1, 2, 3 }, 0 );
		spot.setCovariance( new double[][] { { 6, 2, 3 }, { 2, 7, 4 }, { 3, 4, 8 } } );
		return spot;
	}
}
