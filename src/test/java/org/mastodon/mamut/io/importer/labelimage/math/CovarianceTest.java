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
package org.mastodon.mamut.io.importer.labelimage.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CovarianceTest
{
	@Test
	void testGet()
	{
		double[] x = { 1, 2, 3, 4, 5 };
		double[] y = { 2, 3, 4, 5, 6 };
		org.mastodon.mamut.io.importer.labelimage.math.Covariance covariance =
				new org.mastodon.mamut.io.importer.labelimage.math.Covariance();
		for ( int i = 0; i < x.length; i++ )
			covariance.addValues( x[ i ], y[ i ] );
		double actual = covariance.get();
		assertEquals( 2.5d, actual, 0.0001d );
		assertEquals( 3d, covariance.getMeanX(), 0.0001d );
		assertEquals( 4d, covariance.getMeanY(), 0.0001d );
	}

	@Test
	void testException()
	{
		org.mastodon.mamut.io.importer.labelimage.math.Covariance covariance =
				new org.mastodon.mamut.io.importer.labelimage.math.Covariance();
		covariance.addValues( 1, 1 );
		assertThrows( IllegalArgumentException.class, covariance::get );
	}
}
