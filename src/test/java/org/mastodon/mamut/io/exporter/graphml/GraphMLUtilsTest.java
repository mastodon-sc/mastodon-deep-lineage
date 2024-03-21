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
package org.mastodon.mamut.io.exporter.graphml;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph4;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GraphMLUtilsTest
{

	private ExampleGraph4 graph4;

	@Before
	public void setUp()
	{
		graph4 = new ExampleGraph4();
	}

	@Test
	public void testExportAllTracks() throws IOException
	{
		File tempDirectory = Files.createTempDirectory( "prefix" ).toFile();
		String prefix = "test";
		GraphMLUtils.exportAllTracks( graph4.getModel().getBranchGraph(), tempDirectory, prefix );

		File[] files = tempDirectory.listFiles();
		assertNotNull( files );
		Set< String > actual = new HashSet<>( Arrays.asList( Arrays.stream( files ).map( File::getName ).toArray( String[]::new ) ) );

		Set< String > expected = new HashSet<>();
		expected.add( "test-0.graphml" );
		expected.add( "test-4.graphml" );
		assertEquals( expected, actual );
	}

	@Test
	public void testExportBranches() throws IOException
	{
		RefSet< BranchSpot > branchSpots = RefCollections.createRefSet( graph4.getModel().getBranchGraph().vertices() );
		branchSpots.add( graph4.branchSpotA );
		branchSpots.add( graph4.branchSpotB );
		branchSpots.add( graph4.branchSpotC );
		branchSpots.add( graph4.branchSpotD );
		branchSpots.add( graph4.branchSpotE );
		RefSet< BranchLink > branchLinks = RefCollections.createRefSet( graph4.getModel().getBranchGraph().edges() );
		branchLinks.add( graph4.branchLink0 );
		branchLinks.add( graph4.branchLink1 );
		branchLinks.add( graph4.branchLink2 );

		File tempFile = Files.createTempFile( "graphml-test", "" ).toFile();
		GraphMLUtils.exportBranches( branchSpots, branchLinks, tempFile );

		// read file to string - java 8 style
		String actualContent = FileUtils.readFileToString( tempFile, StandardCharsets.UTF_8 );
		actualContent = actualContent.replaceAll( "[\\t\\n\\r\\s]", "" );

		// expected content
		String expectedContent =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
						+ "    <key id=\"vertex_label_key\" for=\"node\" attr.name=\"branchName\" attr.type=\"string\"/>\n"
						+ "    <key id=\"key0\" for=\"node\" attr.name=\"duration\" attr.type=\"int\"/>\n"
						+ "    <graph edgedefault=\"directed\">\n"
						+ "        <node id=\"4\">\n"
						+ "            <data key=\"vertex_label_key\">6</data>\n"
						+ "            <data key=\"key0\">1</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"3\">\n"
						+ "            <data key=\"vertex_label_key\">4</data>\n"
						+ "            <data key=\"key0\">2</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"2\">\n"
						+ "            <data key=\"vertex_label_key\">3</data>\n"
						+ "            <data key=\"key0\">1</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"1\">\n"
						+ "            <data key=\"vertex_label_key\">2</data>\n"
						+ "            <data key=\"key0\">1</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"0\">\n"
						+ "            <data key=\"vertex_label_key\">0</data>\n"
						+ "            <data key=\"key0\">2</data>\n"
						+ "        </node>\n"
						+ "        <edge source=\"3\" target=\"4\"/>\n"
						+ "        <edge source=\"0\" target=\"2\"/>\n"
						+ "        <edge source=\"0\" target=\"1\"/>\n"
						+ "    </graph>\n"
						+ "</graphml>";

		expectedContent = expectedContent.replaceAll( "[\\t\\n\\r\\s]", "" );
		assertEquals( expectedContent, actualContent );
	}
}
