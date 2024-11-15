package org.mastodon.mamut.feature.dimensionalityreduction.tsne.feature;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionAlgorithm;
import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionController;
import org.mastodon.mamut.feature.dimensionalityreduction.util.InputDimension;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.scijava.Context;

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
