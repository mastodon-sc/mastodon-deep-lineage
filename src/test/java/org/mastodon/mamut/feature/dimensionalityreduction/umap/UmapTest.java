package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import org.junit.jupiter.api.Test;
import tagbio.umap.Umap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UmapTest
{
	@Test
	void test()
	{
		double[][] sampleData = SimpleUmapDemo.generateSampleData();
		Umap umap = SimpleUmapDemo.setUpUmap();
		double[][] umapResult = umap.fitTransform( sampleData );

		assertEquals( umapResult.length, sampleData.length );
		assertEquals( 2, umapResult[ 0 ].length );
		for ( int i = 0; i < 50; i++ )
			assertTrue( umapResult[ i ][ 0 ] < 0 );
		for ( int i = 50; i < 150; i++ )
			assertTrue( umapResult[ i ][ 0 ] > 0 );
	}
}
