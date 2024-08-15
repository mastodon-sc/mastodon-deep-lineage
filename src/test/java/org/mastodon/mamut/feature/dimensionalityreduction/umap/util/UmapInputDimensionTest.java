package org.mastodon.mamut.feature.dimensionalityreduction.umap.util;

import org.junit.jupiter.api.Test;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UmapInputDimensionTest
{

	@Test
	void getListFromFeatureModel()
	{
		Model model = new Model();
		FeatureModel featureModel = model.getFeatureModel();
		List< UmapInputDimension< Spot > > umapInputDimensions = UmapInputDimension.getListFromFeatureModel( featureModel, Spot.class );
		assertNotNull( umapInputDimensions );
		assertFalse( umapInputDimensions.isEmpty() ); // NB: we do not test for specific content, as this is defined by the core and may change.
	}
}
