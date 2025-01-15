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
package org.mastodon.mamut.feature.dimensionalityreduction.tsne.feature;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionAlgorithm;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionController;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

@Disabled( "mvn test takes too long" )
class AbstractTSneFeatureComputerTest
{

	@Test
	void testDataDrivenExceptions()
	{
		ExampleGraph2 graph2 = new ExampleGraph2();
		try (Context context = new Context())
		{
			FeatureModel featureModel = graph2.getModel().getFeatureModel();
			DimensionalityReductionController controller = new DimensionalityReductionController( graph2.getModel(), context );
			controller.setAlgorithm( DimensionalityReductionAlgorithm.TSNE );
			Supplier< List< InputDimension< Spot > > > inputDimensionsSupplier =
					() -> InputDimension.getListFromFeatureModel( featureModel, Spot.class, Link.class );
			assertThrows( IllegalArgumentException.class, () -> controller.computeFeature( inputDimensionsSupplier ) );
			controller.getTSneSettings().setPerplexity( 2 );
			assertThrows( ArrayIndexOutOfBoundsException.class, () -> controller.computeFeature( inputDimensionsSupplier ) );
		}
	}
}
