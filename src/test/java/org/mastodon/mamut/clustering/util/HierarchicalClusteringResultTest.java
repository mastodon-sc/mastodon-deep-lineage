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
package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.clustering.ClusterData;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HierarchicalClusteringResultTest
{
	private HierarchicalClusteringResult< String > hierarchicalClusteringResult;

	@BeforeEach
	void setUp()
	{
		hierarchicalClusteringResult =
				HierarchicalClusteringUtils.getClusteringUsingClusterNumber( ClusterData.example1.getKey(), ClusterData.example1.getValue(),
				new AverageLinkageStrategy(), 3 );
	}

	@Test
	void testGetMedian()
	{
		assertEquals( 51, hierarchicalClusteringResult.getMedian(), 0d );
	}

	@Test
	void testUpdateClusterNames()
	{
		hierarchicalClusteringResult.updateClusterNames();
		Map< Cluster, String > clusterNodesToObjects = hierarchicalClusteringResult.getClusterNodesToObjects();
		assertEquals( "A", clusterNodesToObjects.entrySet().iterator().next().getValue() );
	}

	@Test
	void testExportCsv() throws IOException
	{
		File tempFileCsv = File.createTempFile( "dendrogram", ".csv" );
		tempFileCsv.deleteOnExit();

		hierarchicalClusteringResult.exportCsv( tempFileCsv, null );
		CSVFormat csvFormat = CSVFormat.Builder.create().setDelimiter( ";" ).setQuote( '"' ).setEscape( '"' )
				.setRecordSeparator( "\n" ).build();
		try (
				Reader reader = new FileReader( tempFileCsv );
				CSVParser csvParser = new CSVParser( reader, csvFormat )
		)
		{
			List< CSVRecord > records = csvParser.getRecords();
			CSVRecord header = records.get( 0 );
			CSVRecord firstLine = records.get( 1 );

			assertEquals( 11, records.size() );
			assertEquals( 4, header.size() );
			assertEquals( "F", firstLine.get( 0 ) );
			assertEquals( "", firstLine.get( 1 ) );
			assertEquals( "Group 1", firstLine.get( 2 ) );
			assertEquals( "0", firstLine.get( 3 ) );
		}
	}
}
