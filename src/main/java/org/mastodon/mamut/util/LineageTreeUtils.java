package org.mastodon.mamut.util;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.BreadthFirstIterator;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.spatial.SpatioTemporalIndex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineageTreeUtils
{

	private LineageTreeUtils()
	{}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	/**
	 * Performs a depth-first iteration through the specified {@link ModelBranchGraph} starting from each root.
	 * <p>
	 * The iteration is performed in a single thread. The given {@code action} is called for each vertex in the graph, when all its descendants in the search tree have been iterated through or when it is a leaf in the tree.
	 *
	 * @param branchGraph the graph to iterate through.
	 * @param action      the action to perform on each vertex.
	 */
	public static void callDepthFirst( @Nonnull ModelBranchGraph branchGraph, @Nonnull Consumer< BranchSpot > action )
	{
		DepthFirstSearch< BranchSpot, BranchLink > search = new DepthFirstSearch<>( branchGraph, GraphSearch.SearchDirection.DIRECTED );
		search.setTraversalListener( new SearchListener< BranchSpot, BranchLink, DepthFirstSearch< BranchSpot, BranchLink > >()
		{
			@Override
			public void processVertexLate( BranchSpot vertex, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				action.accept( vertex );
			}

			@Override
			public void processVertexEarly( BranchSpot vertex, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void processEdge( BranchLink edge, BranchSpot from, BranchSpot to, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}

			@Override
			public void crossComponent( BranchSpot from, BranchSpot to, DepthFirstSearch< BranchSpot, BranchLink > search )
			{
				// Do nothing here. We only care about the vertices after all their descendants have been processed (see processVertexLate).
			}
		} );
		final RefSet< BranchSpot > roots = RootFinder.getRoots( branchGraph );
		roots.forEach( search::start );
	}

	private static final String GRAPHML_VERTEX_LABEL_ATTRIBUTE_NAME = "branchSpotName";

	private static final String GRAPHML_VERTEX_LIFESPAN_ATTRIBUTE_NAME = "lifespan";

	/**
	 * Gets the first time point that has at least the given number of spots ({@code numberOfSpots})
	 * by iterating through the given spatio-temporal index ({@code spotSpatioTemporalIndex})
	 * from the given minimum time point ({@code minTimePoint}) to the given maximum time point ({@code maxTimePoint}).
	 *
	 * @param spotSpatioTemporalIndex the index to search in
	 * @param minTimePoint the minimum time point to search in (inclusive)
	 * @param maxTimePoint the maximum time point to search in (inclusive)
	 * @param numberOfSpots the number of spots to search for
	 * @return the first time point with the given number of spots (or more)
	 * @throws NoSuchElementException if no time point with the given number of spots (or more) exists
	 */
	public static int getTimePointWithNSpots( @Nonnull SpatioTemporalIndex< Spot > spotSpatioTemporalIndex,
			int minTimePoint, int maxTimePoint, int numberOfSpots )
	{
		for ( int timePoint = minTimePoint; timePoint <= maxTimePoint; timePoint++ )
			if ( spotSpatioTemporalIndex.getSpatialIndex( timePoint ).size() >= numberOfSpots )
				return timePoint;
		throw new NoSuchElementException(
				"No time point with at least " + numberOfSpots + " spots in the range [" + minTimePoint + ", " + maxTimePoint + "]." );
	}

	/**
	 * Gets the time point with the most spots in the range specified by minTimePoint and maxTimePoint.
	 * If there are no spots in the range, -1 is returned.
	 * If there are multiple time points with the same maximum number of spots, the last one is returned.
	 *
	 * @param spotSpatioTemporalIndex the spatio-temporal index to search in
	 * @param minTimePoint the minimum time point to search in (inclusive)
	 * @param maxTimePoint the maximum time point to search in (inclusive)
	 * @return the time point with the most spots
	 */
	public static int getTimePointWithMostSpots( @Nonnull SpatioTemporalIndex< Spot > spotSpatioTemporalIndex,
			int minTimePoint, int maxTimePoint )
	{
		int maxSpots = 0;
		int timePointWithMaxSpots = -1;
		for ( int timePoint = minTimePoint; timePoint <= maxTimePoint; timePoint++ )
		{
			int spots = spotSpatioTemporalIndex.getSpatialIndex( timePoint ).size();
			if ( spots > maxSpots )
			{
				maxSpots = spots;
				timePointWithMaxSpots = timePoint;
			}
		}
		return timePointWithMaxSpots;
	}

	/**
	 * Write the track scheme(s) of the given branch spots downwards to a file.
	 * @param branchSpots the branch spots to start from
	 * @param file the file to write to
	 * @param modelBranchGraph the model branch graph
	 */
	public static void writeModelBranchGraphOfBranchSpotsToFile( @Nonnull ModelBranchGraph modelBranchGraph,
			@Nonnull Collection< BranchSpot > branchSpots, @Nonnull File file )
	{
		writeModelBranchGraphOfBranchSpotsToFile( branchSpots, file, modelBranchGraph, null, null );
	}

	/**
	 * Exports the trackschemes of the given model branch graph to one file per root node.
	 * @param modelBranchGraph the model branch graph
	 * @param cutoffTimePoint the time point after which the trackscheme is cut off. branches that have not started by this time point are not exported.
	 * @param directory the directory to write the files to
	 * @param fileNamePrefix the prefix of the file names
	 */
	public static void exportAllModelBranchGraphsPerRootNode( @Nonnull ModelBranchGraph modelBranchGraph, int cutoffTimePoint,
			@Nonnull File directory, @Nonnull String fileNamePrefix )
	{
		final RefSet< BranchSpot > roots = RootFinder.getRoots( modelBranchGraph );

		logger.debug( "Export to {} with prefix {}", directory.getAbsolutePath(), fileNamePrefix );

		roots.forEach( branchSpot -> {
			String rootName = replaceCapitalizedLetter( branchSpot.getFirstLabel() );
			File file = new File( directory + File.separator + fileNamePrefix + "-" + rootName + ".graphml" );
			logger.debug( "rootName: {}", rootName );
			writeModelBranchGraphOfBranchSpotsToFile( Collections.singleton( branchSpot ), file, modelBranchGraph, cutoffTimePoint,
					rootName );
		} );
	}

	/**
	 * Returns a {@link Pair} of values where the first value is a {@link Graph} of the branch spots and the second value is a {@link Map} from branch spot names to their life span.
	 * @param branchSpots the branch spots to start from
	 * @param modelBranchGraph the model branch graph
	 * @param maxTimePoint the maximum time point to consider. Branch spots that start after this time point are not included in the result.
	 * @param rootName optional name of the root node. If not specified, the first label of the first branch spot is used.
	 * @param branchSpotNamePrefix optional prefix for the branch spot names.
	 * @return a {@link Pair} of values where the first value is a {@link Graph} of the branch spots and the second value is a {@link Map} from branch spot names to their life span.
	 */
	public static Pair< Graph< String, DefaultEdge >, Map< String, Integer > > getBranchGraphAsJGraphTObjects(
			@Nonnull Collection< BranchSpot > branchSpots, @Nonnull ModelBranchGraph modelBranchGraph,
			@Nullable Integer maxTimePoint, @Nullable String rootName, @Nullable String branchSpotNamePrefix )
	{
		// Create a new directed graph
		Graph< String, DefaultEdge > graph = new SimpleDirectedGraph<>( DefaultEdge.class );
		Map< String, Integer > branchSpotNameToLifeSpanMap = new HashMap<>();

		// Add the selected spots and their links to the graph
		branchSpots.forEach( branchSpot -> {
			// Omit the branch spot, if its timepoint is larger than the maxTimePoint
			if ( maxTimePoint != null && branchSpot.getFirstTimePoint() > maxTimePoint )
				return;

			// Create a breadth first iterator to traverse the model branch graph
			BreadthFirstIterator< BranchSpot, BranchLink > breadthFirstIterator =
					new BreadthFirstIterator<>( branchSpot, modelBranchGraph );

			// Add the selected branch spot and its links to the graph
			addBranchSpotToGraph( graph, branchSpot, branchSpotNameToLifeSpanMap, maxTimePoint, rootName, branchSpotNamePrefix );

			// Traverse downwards (breadth first) from the selected branchSpot in the model branch graph and add the branch spots
			breadthFirstIterator.forEachRemaining( subBranchSpot -> {
				if ( subBranchSpot.equals( branchSpot ) )
					return;
				BranchSpot branchSpotCopy = modelBranchGraph.vertexRef();
				branchSpotCopy.refTo( subBranchSpot );
				// Add the branch spot and its links to the graph
				addBranchSpotToGraph( graph, branchSpotCopy, branchSpotNameToLifeSpanMap, maxTimePoint, null, branchSpotNamePrefix );
			} );
		} );
		return new ImmutablePair<>( graph, branchSpotNameToLifeSpanMap );
	}

	private static void writeModelBranchGraphOfBranchSpotsToFile( @Nonnull Collection< BranchSpot > branchSpots, @Nonnull File file,
			@Nonnull ModelBranchGraph modelBranchGraph, @Nullable Integer maxTimePoint, @Nullable String rootName )
	{
		// Create a graph from the model branch graph
		Pair< Graph< String, DefaultEdge >, Map< String, Integer > > graphAndBranchSpotNameToLifeSpanMap =
				getBranchGraphAsJGraphTObjects( branchSpots, modelBranchGraph, maxTimePoint, rootName, null );
		Graph< String, DefaultEdge > graph = graphAndBranchSpotNameToLifeSpanMap.getKey();
		Map< String, Integer > branchSpotNameToLifeSpanMap = graphAndBranchSpotNameToLifeSpanMap.getValue();

		// Do not export an empty graph
		if ( graph.vertexSet().isEmpty() )
		{
			logger.debug( "Skipping file {}, since there are no branch spots to export.", file.getName() );
			return;
		}

		// Export the graph to a GraphML file
		GraphMLExporter< String, DefaultEdge > exporter = new GraphMLExporter<>();
		exporter.setExportVertexLabels( true );
		exporter.setVertexLabelAttributeName( GRAPHML_VERTEX_LABEL_ATTRIBUTE_NAME );
		exporter.registerAttribute( GRAPHML_VERTEX_LIFESPAN_ATTRIBUTE_NAME, GraphMLExporter.AttributeCategory.NODE, AttributeType.INT );
		exporter.setVertexAttributeProvider( branchSpotName -> {
			Map< String, Attribute > map = new LinkedHashMap<>();
			map.put( GRAPHML_VERTEX_LIFESPAN_ATTRIBUTE_NAME,
					DefaultAttribute.createAttribute( branchSpotNameToLifeSpanMap.get( branchSpotName ) ) );
			return map;
		} );
		try (FileWriter fileWriter = new FileWriter( file ))
		{
			exporter.exportGraph( graph, fileWriter );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		logger.info( "Exported {} branch spot(s) to {}", graph.vertexSet().size(), file.getAbsolutePath() );
	}

	private static void addBranchSpotToGraph( @Nonnull Graph< String, DefaultEdge > graph, @Nonnull BranchSpot branchSpot,
			@Nonnull Map< String, Integer > branchSpotNameToLifeSpanMap, @Nullable Integer maxTimePoint, @Nullable String rootLabel,
			@Nullable String branchSpotNamePrefix )
	{
		// Omit the branch spot, if its first time point is larger than the given maxTimePoint
		if ( maxTimePoint != null && branchSpot.getFirstTimePoint() > maxTimePoint )
			return;

		String prefix = branchSpotNamePrefix == null ? "" : branchSpotNamePrefix;
		String branchSpotName = rootLabel == null ? prefix + branchSpot.getInternalPoolIndex() : prefix + rootLabel;

		graph.addVertex( branchSpotName );
		int endTimePoint = maxTimePoint != null && branchSpot.getTimepoint() > maxTimePoint ? maxTimePoint
				: branchSpot.getTimepoint();
		branchSpotNameToLifeSpanMap.put( branchSpotName, endTimePoint - branchSpot.getFirstTimePoint() );
		logger.trace( "Adding branch spot {} with lifespan {} ({}-{}) to graph.", branchSpotName,
				( endTimePoint - branchSpot.getFirstTimePoint() ), branchSpot.getFirstTimePoint(), endTimePoint );

		// Add the outgoing links of the branch spot to the graph
		branchSpot.outgoingEdges().forEach( branchLink -> {
			String targetLabel = String.valueOf( branchLink.getTarget().getInternalPoolIndex() );
			targetLabel = prefix + targetLabel;
			if ( maxTimePoint != null && branchLink.getTarget().getFirstTimePoint() > maxTimePoint )
				return;
			// Add the source branch spot to the graph if it is not already in the graph
			if ( !graph.containsVertex( branchSpotName ) )
				graph.addVertex( branchSpotName );
			// Add the target branch spot to the graph if it is not already in the graph
			if ( !graph.containsVertex( targetLabel ) )
				graph.addVertex( targetLabel );
			// Add the branch link to the graph
			if ( !graph.containsEdge( branchSpotName, targetLabel ) )
			{
				try
				{
					graph.addEdge( branchSpotName, targetLabel );
				}
				catch ( IllegalArgumentException e )
				{
					logger.info( "Could not add branch link from {} to {} to graph. Reason: {}", branchSpotName, targetLabel,
							e.getMessage() );
				}
			}
		} );
	}

	private static String replaceCapitalizedLetter( String str )
	{
		StringBuilder sb = new StringBuilder();
		// Traverse the string
		for ( int i = 0; i < str.length(); i++ )
		{
			char c = str.charAt( i );

			// Checking if the character is uppercase
			if ( Character.isUpperCase( c ) )
			{
				// Convert the character to lowercase and add an underscore before and after the character
				sb.append( '_' );
				sb.append( Character.toLowerCase( c ) );
				sb.append( '_' );
			}
			else
				sb.append( c );
		}

		return sb.toString();
	}
}
