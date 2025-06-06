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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CovarianceMatrixTest
{
	@Test
	void testGet()
	{
		int[][] dataInt = { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 6 } };
		CovarianceMatrix matrix = new CovarianceMatrix( 2 );
		for ( int[] values : dataInt )
			matrix.addValues( values );
		double[][] actual = matrix.get();

		assertArrayEquals( new double[] { 3d, 4d }, matrix.getMeans(), 0.0001d );
		assertArrayEquals( new double[] { 2.5d, 2.5d }, actual[ 0 ], 0.0001d );
		assertArrayEquals( new double[] { 2.5d, 2.5d }, actual[ 1 ], 0.0001d );
	}

	@Test
	void testException1()
	{
		CovarianceMatrix covarianceMatrix = new CovarianceMatrix( 2 );
		covarianceMatrix.addValues( new int[] { 1, 1 } );
		assertThrows( IllegalArgumentException.class, covarianceMatrix::get );
	}

	@Test
	void testException2()
	{
		CovarianceMatrix covarianceMatrix = new CovarianceMatrix( 2 );
		assertThrows( IllegalArgumentException.class, () -> covarianceMatrix.addValues( new int[] { 1 } ) );
	}
}
