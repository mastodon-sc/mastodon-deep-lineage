package org.mastodon.mamut.io.exporter.graphml;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.util.LineageTreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for exporting a model (branch) graph to GraphML.
 */
public class GraphMLUtils
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String VERTEX_LABEL_ATTRIBUTE_NAME = "branchName";

	private static final String DURATION_ATTRIBUTE_NAME = "duration";

	private GraphMLUtils()
	{
		// prevent instantiation
	}

	/**
	 * Finds all roots in the given branch graph and exports each root and its descendants to a separate GraphML file.
	 * <br><br>
	 * The root names are used for the file names. The files are written to the given directory with the given prefix.
	 * <br><br>
	 * The graph is directed. The branch spots are the vertices and the branch links are the edges.
	 * <br><br>
	 * <ul>
	      <li>The vertices receive a label attribute with the branch spot name.</li>
	      <li>The vertices receive a duration attribute with the branch duration.</li>
	      <li>The edges are not labeled.</li>
	      <li>The edges are not attributed.</li>
	 * </ul>
	 */
	public static void exportAllTracks( final ModelBranchGraph branchGraph, final File directory, final String prefix )
	{
		final RefSet< BranchSpot > roots = RootFinder.getRoots( branchGraph );

		logger.info( "Export {} roots to {} with prefix {}", roots.size(), directory.getAbsolutePath(), prefix );

		roots.forEach( root -> {
			String rootName = root.getFirstLabel();
			String fileName = prefix + "-" + rootName + ".graphml";
			File file = new File( directory + File.separator + fileName );
			logger.trace( "rootName: {}", rootName );
			RefSet< BranchSpot > branchSpots = LineageTreeUtils.getAllVertexSuccessors( root, branchGraph );
			RefSet< BranchLink > branchLinks = LineageTreeUtils.getAllEdgeSuccessors( root, branchGraph );
			exportBranches( branchSpots, branchLinks, file );
		} );
	}

	/**
	 * Exports the given branch spots and branch links to a GraphML file.
	 * <br><br>
	 * The graph is directed. The branch spots are the vertices and the branch links are the edges.
	 * <br><br>
	 * <ul>
	 *     <li>The vertices receive a label attribute with the branch spot name.</li>
	 *     <li>The vertices receive a duration attribute with the branch duration.</li>
	 *     <li>The edges are not labeled.</li>
	 *     <li>The edges are not attributed.</li>
	 * </ul>
	 *
	 * @param branchSpots the branch spots
	 * @param branchLinks the branch links. Only links between branch spots in the branchSpots set are added to the graph.
	 * @param file the file to write the graph to
	 */
	public static void exportBranches( final RefSet< BranchSpot > branchSpots, final RefSet< BranchLink > branchLinks,
			final File file )
	{
		// Create a new directed graph
		Graph< CustomLabelBranchSpot, DefaultEdge > graph = new SimpleDirectedGraph<>( DefaultEdge.class );

		// Add branch spots
		for ( BranchSpot branchSpot : branchSpots )
		{
			logger.trace( "Adding branchSpot: {}", branchSpot );
			BranchSpot copy = branchSpots.createRef();
			copy.refTo( branchSpot );
			graph.addVertex( new CustomLabelBranchSpot( copy ) );
		}
		// Add branch links
		for ( BranchLink branchLink : branchLinks )
		{
			logger.trace( "Adding branchLink: {}", branchLink );
			CustomLabelBranchSpot source = new CustomLabelBranchSpot( branchLink.getSource() );
			CustomLabelBranchSpot target = new CustomLabelBranchSpot( branchLink.getTarget() );
			if ( graph.containsVertex( source ) && graph.containsVertex( target ) )
				graph.addEdge( source, target );
		}

		// Create exporter and make settings for it
		GraphMLExporter< CustomLabelBranchSpot, DefaultEdge > exporter = new GraphMLExporter<>();
		exporter.setExportVertexLabels( true );
		exporter.setVertexLabelAttributeName( VERTEX_LABEL_ATTRIBUTE_NAME );
		exporter.registerAttribute( DURATION_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.INT );
		exporter.setVertexAttributeProvider( customLabelBranchSpot -> {
			Map< String, Attribute > map = new LinkedHashMap<>();
			map.put(
					DURATION_ATTRIBUTE_NAME,
					DefaultAttribute.createAttribute( BranchSpotFeatureUtils.branchDuration( customLabelBranchSpot.branchSpot ) ) );
			return map;
		} );
		exporter.setVertexIdProvider( customLabelBranchSpot -> String.valueOf( customLabelBranchSpot.branchSpot.getInternalPoolIndex() ) );

		// Export the graph to file
		try (FileWriter fileWriter = new FileWriter( file ))
		{
			exporter.exportGraph( graph, fileWriter );
		}
		catch ( IOException e )
		{
			logger.warn( "Could not export branch graph to file: {}. Message: {}", file.getAbsolutePath(), e.getMessage() );
		}

		logger.info( "Exported {} branch spot(s) and {} branch link(s) to {}", graph.vertexSet().size(), graph.edgeSet().size(),
				file.getAbsolutePath() );
	}

	private static class CustomLabelBranchSpot
	{
		private final BranchSpot branchSpot;

		private CustomLabelBranchSpot( final BranchSpot branchSpot )
		{
			this.branchSpot = branchSpot;
		}

		@Override
		public String toString()
		{
			return LineageTreeUtils.isRoot( branchSpot ) ? branchSpot.getFirstLabel() : branchSpot.getLabel();
		}

		@Override
		public boolean equals( Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;
			CustomLabelBranchSpot that = ( CustomLabelBranchSpot ) o;
			return branchSpot.equals( that.branchSpot );
		}

		@Override
		public int hashCode()
		{
			return branchSpot.hashCode();
		}
	}
}
