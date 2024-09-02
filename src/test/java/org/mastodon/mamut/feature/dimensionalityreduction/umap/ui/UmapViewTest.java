package org.mastodon.mamut.feature.dimensionalityreduction.umap.ui;

import org.junit.jupiter.api.Test;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UmapViewTest
{
	@Test
	void testUmapView()
	{
		try (Context context = new Context())
		{
			Model model = new Model();
			assertDoesNotThrow( () -> new UmapView( model, context ) );
		}
	}
}
