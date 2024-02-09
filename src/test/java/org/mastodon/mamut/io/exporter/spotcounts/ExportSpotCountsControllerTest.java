/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.io.exporter.spotcounts;

import com.opencsv.CSVReader;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.scijava.Context;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

public class ExportSpotCountsControllerTest
{
	private ExampleGraph2 exampleGraph2;

	@Before
	public void setUp()
	{
		exampleGraph2 = new ExampleGraph2();
	}

	@Test
	public void testWriteSpotCountsToFile() throws IOException
	{
		File outputFile = File.createTempFile( "spotcounts", ".csv" );
		outputFile.deleteOnExit();
		ExportSpotCountsController controller = new ExportSpotCountsController( exampleGraph2.getModel(), new Context() );
		controller.writeSpotCountsToFile( outputFile );

		// Check if the file was written correctly
		try (CSVReader reader = new CSVReader( new FileReader( outputFile ) );)
		{
			Iterator< String[] > iterator = reader.iterator();
			String[] header = iterator.next();
			String[] line0 = iterator.next();
			String[] line1 = iterator.next();
			String[] line2 = iterator.next();
			String[] line3 = iterator.next();
			String[] line4 = iterator.next();
			String[] line5 = iterator.next();
			String[] line6 = iterator.next();
			String[] line7 = iterator.next();

			assertArrayEquals( new String[] { "timepoint", "spots" }, header );
			assertArrayEquals( new String[] { "0", "1" }, line0 );
			assertArrayEquals( new String[] { "1", "1" }, line1 );
			assertArrayEquals( new String[] { "2", "1" }, line2 );
			assertArrayEquals( new String[] { "3", "2" }, line3 );
			assertArrayEquals( new String[] { "4", "1" }, line4 );
			assertArrayEquals( new String[] { "5", "3" }, line5 );
			assertArrayEquals( new String[] { "6", "1" }, line6 );
			assertArrayEquals( new String[] { "7", "2" }, line7 );
		}
	}

	@Test
	public void testException()
	{
		ExportSpotCountsController controller = new ExportSpotCountsController( exampleGraph2.getModel(), new Context() );
		assertThrows( IllegalArgumentException.class, () -> controller.writeSpotCountsToFile( null ) );
	}
}
