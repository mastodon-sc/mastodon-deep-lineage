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
package org.mastodon.mamut.clustering;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.clustering.util.AverageLinkageUPGMAStrategy;
import org.mastodon.mamut.clustering.util.HierarchicalClusteringResult;
import org.mastodon.mamut.clustering.util.HierarchicalClusteringUtils;

public class ClusterData
{

	private static final String[] names1 = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

	private static final double[][] distances1 = new double[][] {
			{ 0, 51, 81, 35, 9, 95, 37, 19, 48, 21 },
			{ 51, 0, 51, 21, 51, 55, 26, 17, 95, 75 },
			{ 81, 51, 0, 9, 59, 66, 29, 73, 39, 3 },
			{ 35, 21, 9, 0, 81, 46, 71, 27, 26, 11 },
			{ 9, 51, 59, 81, 0, 29, 50, 68, 93, 84 },
			{ 95, 55, 66, 46, 29, 0, 85, 91, 87, 82 },
			{ 37, 26, 29, 71, 50, 85, 0, 34, 60, 80 },
			{ 19, 17, 73, 27, 68, 91, 34, 0, 42, 29 },
			{ 48, 95, 39, 26, 93, 87, 60, 42, 0, 94 },
			{ 21, 75, 3, 11, 84, 82, 80, 29, 94, 0 }
	};

	public static final Pair< String[], double[][] > example1 = Pair.of( names1, distances1 );

	private static final String[] names2 = new String[] { "A", "B", "C", "D", "E" };

	private static final double[][] distances2 = new double[][] {
			{ 0, 1, 1, 2, 2 },
			{ 1, 0, 0, 3, 3 },
			{ 1, 0, 0, 3, 3 },
			{ 2, 3, 3, 0, 0 },
			{ 2, 3, 3, 0, 0 },
	};

	public static final Pair< String[], double[][] > example2 = Pair.of( names2, distances2 );

	public static final double[][] distances3 = new double[][] {
			{ 0, 1, 9, 7, 11, 14 },
			{ 1, 0, 4, 3, 8, 10 },
			{ 9, 4, 0, 9, 2, 8 },
			{ 7, 3, 9, 0, 6, 13 },
			{ 11, 8, 2, 6, 0, 10 },
			{ 14, 10, 8, 13, 10, 0 }
	};

	public static final String[] names3 = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };

	public static HierarchicalClusteringResult< String > createSampleClassification3()
	{
		return HierarchicalClusteringUtils.getClusteringUsingClusterNumber( names3, distances3, new AverageLinkageUPGMAStrategy(), 2 );
	}
}
