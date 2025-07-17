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
package org.mastodon.mamut.io.exporter.graphml;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph4;
import org.mastodon.mamut.io.importer.graphml.GraphMLImporter;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

class GraphMLUtilsTest
{
	private ExampleGraph4 graph4;

	@BeforeEach
	void setUp()
	{
		graph4 = new ExampleGraph4();
	}

	@Test
	void testExportAllTracks() throws IOException
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
	void testExportBranches() throws IOException
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

	@Test
	void testExportSpots() throws IOException
	{
		RefSet< Spot > spots = RefCollections.createRefSet( graph4.getModel().getGraph().vertices() );
		spots.add( graph4.spot0 );
		spots.add( graph4.spot1 );
		spots.add( graph4.spot2 );
		spots.add( graph4.spot3 );
		spots.add( graph4.spot4 );
		spots.add( graph4.spot5 );
		spots.add( graph4.spot6 );
		spots.add( graph4.spot7 );
		RefSet< Link > links = RefCollections.createRefSet( graph4.getModel().getGraph().edges() );
		links.add( graph4.link0 );
		links.add( graph4.link1 );
		links.add( graph4.link2 );
		links.add( graph4.link3 );
		links.add( graph4.link4 );
		links.add( graph4.link5 );

		File tempFile = Files.createTempFile( "graphml-test", "" ).toFile();
		GraphMLUtils.exportSpots( spots, links, tempFile );

		// read file to string - java 8 style
		String actualContent = FileUtils.readFileToString( tempFile, StandardCharsets.UTF_8 );
		actualContent = actualContent.replaceAll( "[\\t\\n\\r\\s]", "" );

		// expected content
		String expectedContent =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
						+ "    <key id=\"key0\" for=\"node\" attr.name=\"x\" attr.type=\"float\"/>\n"
						+ "    <key id=\"key1\" for=\"node\" attr.name=\"y\" attr.type=\"float\"/>\n"
						+ "    <key id=\"key2\" for=\"node\" attr.name=\"z\" attr.type=\"float\"/>\n"
						+ "    <key id=\"key3\" for=\"node\" attr.name=\"frame\" attr.type=\"int\"/>\n"
						+ "    <key id=\"key4\" for=\"node\" attr.name=\"label\" attr.type=\"int\"/>\n"
						+ "    <graph edgedefault=\"directed\">\n"
						+ "        <node id=\"7\">\n"
						+ "            <data key=\"key0\">8.0</data>\n"
						+ "            <data key=\"key1\">12.0</data>\n"
						+ "            <data key=\"key2\">16.0</data>\n"
						+ "            <data key=\"key3\">2</data>\n"
						+ "            <data key=\"key4\">7</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"6\">\n"
						+ "            <data key=\"key0\">6.0</data>\n"
						+ "            <data key=\"key1\">10.0</data>\n"
						+ "            <data key=\"key2\">14.0</data>\n"
						+ "            <data key=\"key3\">2</data>\n"
						+ "            <data key=\"key4\">6</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"5\">\n"
						+ "            <data key=\"key0\">4.0</data>\n"
						+ "            <data key=\"key1\">6.0</data>\n"
						+ "            <data key=\"key2\">8.0</data>\n"
						+ "            <data key=\"key3\">1</data>\n"
						+ "            <data key=\"key4\">5</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"4\">\n"
						+ "            <data key=\"key0\">2.0</data>\n"
						+ "            <data key=\"key1\">4.0</data>\n"
						+ "            <data key=\"key2\">6.0</data>\n"
						+ "            <data key=\"key3\">0</data>\n"
						+ "            <data key=\"key4\">4</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"3\">\n"
						+ "            <data key=\"key0\">4.0</data>\n"
						+ "            <data key=\"key1\">8.0</data>\n"
						+ "            <data key=\"key2\">12.0</data>\n"
						+ "            <data key=\"key3\">2</data>\n"
						+ "            <data key=\"key4\">3</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"2\">\n"
						+ "            <data key=\"key0\">12.0</data>\n"
						+ "            <data key=\"key1\">24.0</data>\n"
						+ "            <data key=\"key2\">36.0</data>\n"
						+ "            <data key=\"key3\">2</data>\n"
						+ "            <data key=\"key4\">2</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"1\">\n"
						+ "            <data key=\"key0\">3.0</data>\n"
						+ "            <data key=\"key1\">6.0</data>\n"
						+ "            <data key=\"key2\">9.0</data>\n"
						+ "            <data key=\"key3\">1</data>\n"
						+ "            <data key=\"key4\">1</data>\n"
						+ "        </node>\n"
						+ "        <node id=\"0\">\n"
						+ "            <data key=\"key0\">1.0</data>\n"
						+ "            <data key=\"key1\">2.0</data>\n"
						+ "            <data key=\"key2\">3.0</data>\n"
						+ "            <data key=\"key3\">0</data>\n"
						+ "            <data key=\"key4\">0</data>\n"
						+ "        </node>\n"
						+ "        <edge source=\"5\" target=\"7\"/>\n"
						+ "        <edge source=\"5\" target=\"6\"/>\n"
						+ "        <edge source=\"4\" target=\"5\"/>\n"
						+ "        <edge source=\"1\" target=\"3\"/>\n"
						+ "        <edge source=\"1\" target=\"2\"/>\n"
						+ "        <edge source=\"0\" target=\"1\"/>\n"
						+ "    </graph>\n"
						+ "</graphml>";

		expectedContent = expectedContent.replaceAll( "[\\t\\n\\r\\s]", "" );
		assertEquals( expectedContent, actualContent );
	}

	@Test
	void testExportSelectedSpotsAndLinks() throws IOException
	{

		File tempFile = Files.createTempFile( "graphml-test", "" ).toFile();
		try (final Context context = new Context())
		{
			final Img< FloatType > img = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, graph4.getModel(), context );
			// select all spots
			projectModel.getSelectionModel().setSelected( graph4.spot0, true );
			projectModel.getSelectionModel().setSelected( graph4.spot1, true );
			projectModel.getSelectionModel().setSelected( graph4.spot2, true );
			projectModel.getSelectionModel().setSelected( graph4.spot3, true );
			projectModel.getSelectionModel().setSelected( graph4.spot4, true );
			projectModel.getSelectionModel().setSelected( graph4.spot5, true );
			projectModel.getSelectionModel().setSelected( graph4.spot6, true );
			projectModel.getSelectionModel().setSelected( graph4.spot7, true );
			// select all links
			projectModel.getSelectionModel().setSelected( graph4.link0, true );
			projectModel.getSelectionModel().setSelected( graph4.link1, true );
			projectModel.getSelectionModel().setSelected( graph4.link2, true );
			projectModel.getSelectionModel().setSelected( graph4.link3, true );
			projectModel.getSelectionModel().setSelected( graph4.link4, true );
			projectModel.getSelectionModel().setSelected( graph4.link5, true );

			GraphMLUtils.exportSelectedSpotsAndLinks( projectModel, tempFile );
			// after exporting, the graph should still contain all vertices and edges
			assertEquals( 8, projectModel.getModel().getGraph().vertices().size() );
			assertEquals( 6, projectModel.getModel().getGraph().edges().size() );
			projectModel.getSelectionModel().getSelectedVertices().forEach( spot -> graph4.getModel().getGraph().remove( spot ) );
			projectModel.getSelectionModel().getSelectedEdges().forEach( link -> graph4.getModel().getGraph().remove( link ) );
			// after removing the selected spots and links, the graph should be empty
			assertEquals( 0, projectModel.getModel().getGraph().vertices().size() );
			assertEquals( 0, projectModel.getModel().getGraph().edges().size() );
			GraphMLImporter.importGraphML( tempFile.getAbsolutePath(), projectModel, 0, 2 );
			// after re-import, the graph should be restored with the same number of vertices and edges
			assertEquals( 8, projectModel.getModel().getGraph().vertices().size() );
			assertEquals( 6, projectModel.getModel().getGraph().edges().size() );
		}
	}
}
