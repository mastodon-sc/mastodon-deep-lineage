package org.mastodon.mamut.util;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Spot;
import org.mastodon.spatial.SpatioTemporalIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LineageTreeUtilsTest
{

	@Test
	public void testGetTimePointWithNSpots()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		assertEquals( 5, LineageTreeUtils.getTimePointWithNSpots( exampleGraph2.getModel().getSpatioTemporalIndex(), 0, 7, 3 ) );
		assertEquals( 3, LineageTreeUtils.getTimePointWithNSpots( exampleGraph2.getModel().getSpatioTemporalIndex(), 0, 7, 2 ) );
		assertEquals( 3, LineageTreeUtils.getTimePointWithNSpots( exampleGraph2.getModel().getSpatioTemporalIndex(), -1, 10, 2 ) );
		SpatioTemporalIndex< Spot > index = exampleGraph2.getModel().getSpatioTemporalIndex();
		NoSuchElementException exception =
				assertThrows( NoSuchElementException.class, () -> LineageTreeUtils.getTimePointWithNSpots( index, 0, 7, 5 ) );
		String expectedMessage = "No time point with at least 5 spots in the range [0, 7].";
		String actualMessage = exception.getMessage();
		assertTrue( actualMessage.contains( expectedMessage ) );
	}

	@Test
	public void testGetTimePointWithMostSpots()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		assertEquals( 5, LineageTreeUtils.getTimePointWithMostSpots( exampleGraph2.getModel().getSpatioTemporalIndex(), 0, 7 ) );
		assertEquals( 5, LineageTreeUtils.getTimePointWithMostSpots( exampleGraph2.getModel().getSpatioTemporalIndex(), -1, 10 ) );
		assertEquals( -1, LineageTreeUtils.getTimePointWithMostSpots( exampleGraph2.getModel().getSpatioTemporalIndex(), 8, 10 ) );
		ExampleGraph1 exampleGraph1 = new ExampleGraph1();
		assertEquals( 3, LineageTreeUtils.getTimePointWithMostSpots( exampleGraph1.getModel().getSpatioTemporalIndex(), 0, 7 ) );
	}

	@Test
	public void testWriteModelBranchGraphOfSpotsToFile() throws IOException
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		File file = File.createTempFile( "graphml-test", "" );
		LineageTreeUtils.writeModelBranchGraphOfBranchSpotsToFile( exampleGraph2.getModel().getBranchGraph(),
				Collections.singleton( exampleGraph2.branchSpotA ), file );

		// read file to string - java 8 style
		String actualContent = new String( Files.readAllBytes( file.toPath() ) );
		actualContent = actualContent.replace( "\n", "" ).replace( "\r", "" );
		System.out.println( actualContent );

		// expected content
		String expectedContent =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
						+
						"    <key id=\"vertex_label_key\" for=\"node\" attr.name=\"branchSpotName\" attr.type=\"string\"/>\n" +
						"    <key id=\"key0\" for=\"node\" attr.name=\"lifespan\" attr.type=\"int\"/>\n" +
						"    <graph edgedefault=\"undirected\">\n" +
						"        <node id=\"1\">\n" +
						"            <data key=\"vertex_label_key\">2</data>\n" +
						"            <data key=\"key0\">2</data>\n" +
						"        </node>\n" +
						"        <node id=\"2\">\n" +
						"            <data key=\"vertex_label_key\">4</data>\n" +
						"            <data key=\"key0\">1</data>\n" +
						"        </node>\n" +
						"        <node id=\"3\">\n" +
						"            <data key=\"vertex_label_key\">13</data>\n" +
						"            <data key=\"key0\">2</data>\n" +
						"        </node>\n" +
						"        <node id=\"4\">\n" +
						"            <data key=\"vertex_label_key\">7</data>\n" +
						"            <data key=\"key0\">2</data>\n" +
						"        </node>\n" +
						"        <node id=\"5\">\n" +
						"            <data key=\"vertex_label_key\">10</data>\n" +
						"            <data key=\"key0\">2</data>\n" +
						"        </node>\n" +
						"        <edge source=\"1\" target=\"2\"/>\n" +
						"        <edge source=\"1\" target=\"3\"/>\n" +
						"        <edge source=\"2\" target=\"4\"/>\n" +
						"        <edge source=\"2\" target=\"5\"/>\n" +
						"    </graph>\n" +
						"</graphml>\n";
		expectedContent = expectedContent.replace( "\n", "" ).replace( "\r", "" );
		assertEquals( expectedContent, actualContent );
	}

	@Test
	public void testExportAllModelBranchGraphsPerRootNode() throws IOException
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		File file = File.createTempFile( "graphml-test", "" );
		String tempDirectory = file.getParent();
		System.out.println( tempDirectory );
		LineageTreeUtils.exportAllModelBranchGraphsPerRootNode( exampleGraph2.getModel().getBranchGraph(), 4, new File( tempDirectory ),
				"embyro" );

		File file1 = new File( tempDirectory + "/embyro-0.graphml" );
		assertTrue( file1.exists() );

		// read file to string - java 8 style
		String actualContent = new String( Files.readAllBytes( file1.toPath() ) );
		System.out.println( actualContent );
		actualContent = actualContent.replace( "\n", "" ).replace( "\r", "" );

		// expected content
		String expectedContent =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
						+
						"    <key id=\"vertex_label_key\" for=\"node\" attr.name=\"branchSpotName\" attr.type=\"string\"/>\n" +
						"    <key id=\"key0\" for=\"node\" attr.name=\"lifespan\" attr.type=\"int\"/>\n" +
						"    <graph edgedefault=\"undirected\">\n" +
						"        <node id=\"1\">\n" +
						"            <data key=\"vertex_label_key\">0</data>\n" +
						"            <data key=\"key0\">2</data>\n" +
						"        </node>\n" +
						"        <node id=\"2\">\n" +
						"            <data key=\"vertex_label_key\">4</data>\n" +
						"            <data key=\"key0\">1</data>\n" +
						"        </node>\n" +
						"        <node id=\"3\">\n" +
						"            <data key=\"vertex_label_key\">13</data>\n" +
						"            <data key=\"key0\">1</data>\n" +
						"        </node>\n" +
						"        <edge source=\"1\" target=\"2\"/>\n" +
						"        <edge source=\"1\" target=\"3\"/>\n" +
						"    </graph>\n" +
						"</graphml>";
		expectedContent = expectedContent.replace( "\n", "" ).replace( "\r", "" );
		assertEquals( expectedContent, actualContent );
	}

	@Test
	public void testGetBranchGraphAsJGraphTObjects()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		Pair< Graph< String, DefaultEdge >, Map< String, Integer > > graphAndCostsMap =
				LineageTreeUtils.getBranchGraphAsJGraphTObjects( Collections.singleton( exampleGraph2.branchSpotA ),
						exampleGraph2.getModel().getBranchGraph(), 10, null, null );
		Graph< String, DefaultEdge > graph = graphAndCostsMap.getKey();
		Map< String, Integer > costsMap = graphAndCostsMap.getValue();
		assertEquals( 5, costsMap.size() );
		assertEquals( 5, graph.vertexSet().size() );
		assertTrue( graph.containsVertex( exampleGraph2.branchSpotA.getLabel() ) );
		assertTrue( graph.containsVertex( exampleGraph2.branchSpotB.getLabel() ) );
		assertTrue( graph.containsVertex( exampleGraph2.branchSpotC.getLabel() ) );
		assertTrue( graph.containsVertex( exampleGraph2.branchSpotD.getLabel() ) );
		assertTrue( graph.containsVertex( exampleGraph2.branchSpotE.getLabel() ) );
		assertEquals( 2, costsMap.get( exampleGraph2.branchSpotA.getLabel() ).intValue() );
		assertEquals( 1, costsMap.get( exampleGraph2.branchSpotB.getLabel() ).intValue() );
		assertEquals( 2, costsMap.get( exampleGraph2.branchSpotC.getLabel() ).intValue() );
		assertEquals( 2, costsMap.get( exampleGraph2.branchSpotD.getLabel() ).intValue() );
		assertEquals( 2, costsMap.get( exampleGraph2.branchSpotE.getLabel() ).intValue() );
	}
}
