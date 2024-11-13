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
package org.mastodon.mamut.feature.dimensionalityreduction.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StandardScalerTest
{
	@Test
	void standardizeColumns_withMultipleColumns()
	{
		double[][] array = {
				{ 1, 1, 1 },
				{ 2, 2, 2 },
				{ 3, 3, 3 },
				{ 4, 4, 4 },
				{ 5, 5, 5 }
		};
		double[][] expected = {
				{ -1.2649110640673518, -1.2649110640673518, -1.2649110640673518 },
				{ -0.6324555320336759, -0.6324555320336759, -0.6324555320336759 },
				{ 0, 0, 0 },
				{ 0.6324555320336759, 0.6324555320336759, 0.6324555320336759 },
				{ 1.2649110640673518, 1.2649110640673518, 1.2649110640673518 }
		};
		StandardScaler.standardizeColumns( array );
		assertArrayEquals( expected, array );
	}

	@Test
	void standardizeColumns_withSingleColumn()
	{
		double[][] array = {
				{ 1 },
				{ 2 },
				{ 3 },
				{ 4 },
				{ 5 }
		};
		double[][] expected = {
				{ -1.2649110640673518 },
				{ -0.6324555320336759 },
				{ 0 },
				{ 0.6324555320336759 },
				{ 1.2649110640673518 }
		};
		StandardScaler.standardizeColumns( array );
		assertArrayEquals( expected, array );
	}

	@Test
	void standardizeColumns_withSingleRow()
	{
		double[][] array = { { 1, 2, 3 } };
		double[][] expected = { { 0, 0, 0 } };
		StandardScaler.standardizeColumns( array );
		assertArrayEquals( expected, array );
	}

	@Test
	void standardizeColumns_withEmptyArray()
	{
		double[][] array = {};
		double[][] expected = {};
		StandardScaler.standardizeColumns( array );
		assertArrayEquals( expected, array );
	}

	@Test
	void standardizeColumns_withZeroVarianceColumn()
	{
		double[][] array = {
				{ 1, 2, 1 },
				{ 2, 2, 2 },
				{ 3, 2, 3 },
				{ 4, 2, 4 },
				{ 5, 2, 5 }
		};
		double[][] expected = {
				{ -1.2649110640673518, 0, -1.2649110640673518 },
				{ -0.6324555320336759, 0, -0.6324555320336759 },
				{ 0, 0, 0 },
				{ 0.6324555320336759, 0, 0.6324555320336759 },
				{ 1.2649110640673518, 0, 1.2649110640673518 }
		};
		StandardScaler.standardizeColumns( array );
		assertArrayEquals( expected, array );
	}

	@Test
	void standardizeColumns_withNaNValue()
	{
		double[][] array = {
				{ 1, 2, 1 },
				{ 2, 2, 2 },
				{ 3, Double.NaN, 3 },
				{ 4, 2, 4 },
				{ 5, 2, 5 }
		};
		double[][] expected = {
				{ -1.2649110640673518, 0, -1.2649110640673518 },
				{ -0.6324555320336759, 0, -0.6324555320336759 },
				{ 0, 0, 0 },
				{ 0.6324555320336759, 0, 0.6324555320336759 },
				{ 1.2649110640673518, 0, 1.2649110640673518 }
		};
		StandardScaler.standardizeColumns( array );
		assertArrayEquals( expected, array );
	}
}
