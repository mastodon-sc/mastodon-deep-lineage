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

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.branch.BranchSpotFeatureUtils;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Spot;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Utility class for exporting a model (branch) graph to GraphML.
 */
public class GraphMLUtils
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String BRANCH_SPOT_LABEL_ATTRIBUTE_NAME = "branchName";

	private static final String DURATION_ATTRIBUTE_NAME = "duration";

	private static final String X_ATTRIBUTE_NAME = "x";

	private static final String Y_ATTRIBUTE_NAME = "y";

	private static final String Z_ATTRIBUTE_NAME = "z";

	private static final String FRAME_ATTRIBUTE_NAME = "frame";

	private static final String LABEL_ATTRIBUTE_NAME = "label";

	/**
	 * Private constructor to prevent instantiation.
	 */

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

	public static void exportSelectedSpotsAndLinks( final ProjectModel projectModel, final File file )
	{
		// Get the selected spots from the UI
		RefSet< Spot > selectedSpots = projectModel.getSelectionModel().getSelectedVertices();
		RefSet< Link > selectedLinks = projectModel.getSelectionModel().getSelectedEdges();

		logger.debug( "Selected spots: {}", selectedSpots.size() );
		logger.debug( "Selected links: {}", selectedLinks.size() );

		ReentrantReadWriteLock.ReadLock lock = projectModel.getModel().getGraph().getLock().readLock();
		lock.lock();

		try
		{
			// Export the selected spots and their links to a GraphML file
			GraphMLUtils.exportSpots( selectedSpots, selectedLinks, file );
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Exports the given spots and links to a GraphML file.
	 * <br><br>
	 * The graph is directed. The spots are the vertices and the links are the edges.
	 * <br><br>
	 * <ul>
	 *     <li>The vertices receive a label attribute with the spot label.</li>
	 *     <li>The vertices receive attributes with x,y,z and t coordinates.</li>
	 *     <li>The vertices receive attributes with all 9 elements of the covariance matrix.</li>
	 *     <li>The edges are not labeled.</li>
	 *     <li>The edges are not attributed.</li>
	 * </ul>
	 *
	 * @param spots the spots
	 * @param links the links. Only links between spots in the spot set are added to the graph.
	 * @param file the file to write the graph to
	 */
	static void exportSpots( final RefSet< Spot > spots, final RefSet< Link > links, final File file )
	{
		// Create a new directed graph
		Graph< CustomLabelSpot, DefaultEdge > graph = new SimpleDirectedGraph<>( DefaultEdge.class );

		// Add spots
		for ( Spot spot : spots )
		{
			Spot copy = spots.createRef();
			copy.refTo( spot );
			graph.addVertex( new CustomLabelSpot( copy ) );
		}
		// Add links
		for ( Link link : links )
		{
			CustomLabelSpot source = new CustomLabelSpot( link.getSource() );
			CustomLabelSpot target = new CustomLabelSpot( link.getTarget() );
			if ( graph.containsVertex( source ) && graph.containsVertex( target ) )
				graph.addEdge( source, target );
		}

		// Create exporter and make settings for it
		GraphMLExporter< CustomLabelSpot, DefaultEdge > exporter = new GraphMLExporter<>();
		exporter.setExportVertexLabels( false );
		exporter.registerAttribute( X_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.FLOAT );
		exporter.registerAttribute( Y_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.FLOAT );
		exporter.registerAttribute( Z_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.FLOAT );
		exporter.registerAttribute( FRAME_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.INT );
		exporter.registerAttribute( LABEL_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.INT );
		exporter.setVertexAttributeProvider( customLabelSpot -> {
			Map< String, Attribute > map = new LinkedHashMap<>();
			double[][] covariance = new double[ 3 ][ 3 ];
			customLabelSpot.spot.getCovariance( covariance );
			map.put( X_ATTRIBUTE_NAME, DefaultAttribute.createAttribute( customLabelSpot.spot.getDoublePosition( 0 ) ) );
			map.put( Y_ATTRIBUTE_NAME, DefaultAttribute.createAttribute( customLabelSpot.spot.getDoublePosition( 1 ) ) );
			map.put( Z_ATTRIBUTE_NAME, DefaultAttribute.createAttribute( customLabelSpot.spot.getDoublePosition( 2 ) ) );
			map.put( FRAME_ATTRIBUTE_NAME, DefaultAttribute.createAttribute( customLabelSpot.spot.getTimepoint() ) );
			map.put( LABEL_ATTRIBUTE_NAME, DefaultAttribute.createAttribute( customLabelSpot.spot.getInternalPoolIndex() ) );
			return map;
		} );
		exporter.setVertexIdProvider( customLabelSpot -> String.valueOf( customLabelSpot.spot.getInternalPoolIndex() ) );

		// Export the graph to file
		try (FileWriter fileWriter = new FileWriter( file ))
		{
			exporter.exportGraph( graph, fileWriter );
		}
		catch ( IOException e )
		{
			logger.warn( "Could not export graph to file: {}. Message: {}", file.getAbsolutePath(), e.getMessage() );
		}

		logger.info( "Exported {} spot(s) and {} link(s) to {}", graph.vertexSet().size(), graph.edgeSet().size(),
				file.getAbsolutePath() );
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
		exporter.setVertexLabelAttributeName( BRANCH_SPOT_LABEL_ATTRIBUTE_NAME );
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

		logger.info( "Exported {} branch-spot(s) and {} branch-link(s) to {}", graph.vertexSet().size(), graph.edgeSet().size(),
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

	private static class CustomLabelSpot
	{
		private final Spot spot;

		private CustomLabelSpot( final Spot spot )
		{
			this.spot = spot;
		}

		@Override
		public String toString()
		{
			return spot.getLabel();
		}

		@Override
		public boolean equals( Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;
			CustomLabelSpot that = ( CustomLabelSpot ) o;
			return spot.equals( that.spot );
		}

		@Override
		public int hashCode()
		{
			return spot.hashCode();
		}
	}
}
