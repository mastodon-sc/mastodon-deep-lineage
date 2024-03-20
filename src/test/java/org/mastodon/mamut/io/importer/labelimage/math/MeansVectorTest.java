package org.mastodon.mamut.io.importer.labelimage.math;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class MeansVectorTest
{
	@Test
	public void testAddValuesException()
	{
		MeansVector meansVector = new MeansVector( 3 );
		assertThrows( IllegalArgumentException.class, () -> meansVector.addValues( new int[] { 1, 2 } ) );
	}
}
