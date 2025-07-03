package org.mastodon.mamut.lineagemotifs.util;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.RefPool;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.clustering.treesimilarity.TreeDistances;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.treesimilarity.tree.Tree;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.util.ColorUtils;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.util.DepthFirstIteration;
import org.mastodon.util.TagSetUtils;
import org.mastodon.util.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineageMotifsUtils
{

	public static final String DEFAULT_LINEAGE_MOTIF_NAME = "Lineage Motif";

	private LineageMotifsUtils()
	{
		// Utility class, no instantiation allowed.
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String TAG_NAME = "Lineage Motif ";

	/**
	 * Retrieves the {@link BranchSpotTree} for the selected lineage motif based on the given model and the current selection of the user.
	 * It ensures a valid selection of exactly one lineage motif and throws an exception if none or multiple are selected.
	 *
	 * @param model the {@link Model} containing the graph and branch data
	 * @param selectionModel the {@link SelectionModel} indicating the currently selected spots and links
	 * @return the {@link BranchSpotTree} representing the selected lineage motif
	 * @throws InvalidLineageMotifSelection if the selection is invalid (e.g., no spots selected, or multiple motifs selected)
	 */
	public static BranchSpotTree getSelectedMotif( final Model model, final SelectionModel< Spot, Link > selectionModel )
			throws InvalidLineageMotifSelection
	{
		RefSet< Spot > selectedRootSpots =
				SelectedRootsFinder.getRoots( model.getGraph(), selectionModel );
		if ( selectedRootSpots.isEmpty() )
			throw new InvalidLineageMotifSelection(
					"No selected spots found. Thus no lineage motif can be found.", "No spots selected",
					"Please select spots to define a lineage motif."
			);
		if ( selectedRootSpots.size() > 1 )
			throw new InvalidLineageMotifSelection(
					"Multiple lineage motifs (" + selectedRootSpots.size() + ") found. Only one is allowed.",
					"Multiple motifs selected",
					"You have selected " + selectedRootSpots.size() + " lineage motifs. Please select only one."
			);
		Spot selectedRoot = selectedRootSpots.iterator().next();

		int startTimepoint = selectedRoot.getTimepoint();
		int endTimepoint = getEndTimepoint( selectedRoot, model, selectionModel );
		BranchSpot ref = model.getBranchGraph().vertexRef();
		BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( selectedRoot, ref );
		return new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
	}

	/**
	 * Gets the end timepoint of the selected lineage motif.
	 * @param selectedRoot the selected root {@link Spot} of the lineage motif
	 * @param model the {@link Model} containing the graph
	 * @param selectionModel the {@link SelectionModel} containing the selected {@link Spot}s
	 * @return the end timepoint of the lineage motif, i.e. the maximum timepoint of all selected {@link Spot}s in the lineage motif
	 */
	static int getEndTimepoint( final Spot selectedRoot, final Model model, final SelectionModel< Spot, Link > selectionModel )
	{
		AtomicInteger highestTimepoint = new AtomicInteger( selectedRoot.getTimepoint() );
		Iterable< DepthFirstIteration.Step< Spot > > depthFirstIteration = DepthFirstIteration.forRoot( model.getGraph(), selectedRoot );
		depthFirstIteration.forEach( spotStep -> {
			if ( !spotStep.isFirstVisit() )
				return; // only consider the first visit to each spot
			Spot spot = spotStep.node();
			boolean isSelected = selectionModel.isSelected( spot );
			if ( isSelected && spot.getTimepoint() > highestTimepoint.get() )
				highestTimepoint.set( spot.getTimepoint() );
			if ( !isSelected )
				spotStep.truncate(); // do not traverse further if the spot is not selected
		} );
		return highestTimepoint.get();
	}

	/**
	 * Gets the similarity of the given lineage motif to all other motifs in the model.
	 * The method iterates over all spots in the graph and constructs a lineage motif for each of these spots with the same length as the given lineage motif.
	 * The similarity is calculated as the normalized distance between the two motifs using the {@link TreeDistances#normalizedDistance(Tree, Tree, ToDoubleBiFunction)} method.
	 *
	 * @param model the {@link Model} containing the graph
	 * @param lineageMotif the {@link BranchSpotTree} representing the given lineage motif
	 * @param branchRef a reference to the branch graph
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage motif
	 */
	static RefDoubleMap< Spot > getMotifSimilarityBySpotIteration( final Model model, final BranchSpotTree lineageMotif,
			final BranchSpot branchRef )
	{
		final int motifLength = getMotifLength( lineageMotif );
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( model.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		RefSet< Spot > roots = RootFinder.getRoots( model.getGraph() );
		for ( Spot root : roots )
		{
			Iterable< DepthFirstIteration.Step< Spot > > depthFirstIteration =
					DepthFirstIteration.forRoot( model.getGraph(), root );
			depthFirstIteration.forEach( spotStep -> {
				if ( !spotStep.isFirstVisit() )
					return; // only consider the first visit to each spot
				Spot spot = spotStep.node();
				if ( maxTimepoint - spot.getTimepoint() < motifLength )
					spotStep.truncate(); // do not traverse further if the module does not fit into the remaining time points
				else
				{
					int startTimepoint = spot.getTimepoint();
					int endTimepoint = startTimepoint + motifLength;
					BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( spot, branchRef );
					BranchSpotTree candidateModule = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
					double distance = TreeDistances.normalizedDistance(
							lineageMotif, candidateModule,
							TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION
					);
					candidates.put( spot, distance );
				}
			} );
		}
		return candidates;
	}

	/**
	 * Gets the similarity of the given lineage module to all other modules in the model.
	 * The method iterates over all spots in the graph and constructs a lineage module for each of these spots with the same length as the given lineage module.
	 * The similarity is calculated as the normalized distance between the two modules using the {@link TreeDistances#normalizedDistance(Tree, Tree, ToDoubleBiFunction)} method.
	 *
	 * @param model the {@link Model} containing the graph
	 * @param lineageMotif the {@link BranchSpotTree} representing the given lineage module
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage module
	 */
	static RefDoubleMap< Spot > getMotifSimilarityByBranchSpotIteration( final Model model, final BranchSpotTree lineageMotif )
	{
		final int motifLength = getMotifLength( lineageMotif );
		int moduleStartTimepoint = lineageMotif.getStartTimepoint();
		int firstDivisionTimepoint = lineageMotif.getBranchSpot().getTimepoint();
		int timepointsUntilFirstDivision = firstDivisionTimepoint - moduleStartTimepoint;
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( model.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		logger.debug( "motifLength: {}, maxTimepoint: {}", motifLength, maxTimepoint );
		for ( BranchSpot branchSpot : model.getBranchGraph().vertices() )
		{
			int startTimepoint = branchSpot.getTimepoint() - timepointsUntilFirstDivision;
			if ( maxTimepoint - startTimepoint >= motifLength )
			{
				int endTimepoint = startTimepoint + motifLength;
				BranchSpotTree candidateMotifs = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
				double distance = TreeDistances.normalizedDistance(
						lineageMotif, candidateMotifs,
						TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION
				);
				Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( branchSpot );
				// we need to iterate over the spots in the branch to get the correct spot for the candidate
				while ( spotIterator.hasNext() )
				{
					Spot spot = spotIterator.next();
					if ( spot.getTimepoint() == startTimepoint )
					{
						candidates.put( spot, distance );
						break;
					}
				}
				model.getBranchGraph().releaseIterator( spotIterator );
			}
		}
		return candidates;
	}

	static int getMotifLength( final BranchSpotTree lineageMotif )
	{
		return lineageMotif.getEndTimepoint() - lineageMotif.getStartTimepoint();
	}

	/**
	 * Gets the most similar lineage motifs to a given motif based on their similarity scores.
	 * This method identifies and ranks other motifs compared to a reference motif
	 * and returns a list of the most similar ones up to a specified maximum number, including their similarity scores.
	 *
	 * @param model           the {@link Model} containing the graph and branch data for the lineage
	 * @param lineageMotifs   the {@link BranchSpotTree} representing the lineage motif to compare
	 * @param maxNumberOfMotifs the maximum number of similar motifs to retrieve
	 * @param spotRef         a reference {@link Spot} used for accessing the graph's objects
	 * @param branchRef       a reference {@link BranchSpot} associated with the branch graph
	 * @return a {@link List} of {@link BranchSpotTree} objects representing the most similar lineage motifs, including their similarity scores.
	 */
	public static List< Pair< BranchSpotTree, Double > > getMostSimilarMotifs( final Model model, final BranchSpotTree lineageMotifs,
			int maxNumberOfMotifs, final Spot spotRef, final BranchSpot branchRef, boolean isSpotIteration )
	{
		int motifLength = getMotifLength( lineageMotifs );
		RefDoubleMap< Spot > candidates;
		if ( isSpotIteration )
			candidates = getMotifSimilarityBySpotIteration( model, lineageMotifs, branchRef );
		else
			candidates = getMotifSimilarityByBranchSpotIteration( model, lineageMotifs );
		List< Pair< BranchSpotTree, Double > > motifs = new ArrayList<>();
		RefPool< Spot > refPool = model.getGraph().vertices().getRefPool();
		Map< Integer, Double > distances = candidates.keySet().stream()
				.collect( Collectors.toMap( refPool::getId, candidates::get ) );

		List< Pair< Integer, Double > > sortedDistances = distances.entrySet().stream()
				.map( e -> Pair.of( e.getKey(), e.getValue() ) )
				.sorted( Comparator.comparingDouble( Pair::getValue ) )
				.collect( Collectors.toList() );

		int max = Math.min( sortedDistances.size(), maxNumberOfMotifs );
		int entries = 0;
		int i = 0;

		while ( entries < max )
		{
			Pair< Integer, Double > entry = sortedDistances.get( i );
			i++;
			Spot spot = refPool.getObject( entry.getLeft(), spotRef );
			double distance = entry.getRight();
			logger.debug( "Spot: {} has a distance of: {}", spot.getLabel(), distance );
			BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( spot, model.getBranchGraph().vertexRef() );
			int startTimepoint = spot.getTimepoint();
			int endTimepoint = startTimepoint + motifLength;
			BranchSpotTree motif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
			motifs.add( Pair.of( motif, distance ) );
			if ( !lineageMotifs.getBranchSpot().equals( branchSpot ) )
				entries++;
		}
		return motifs;
	}

	/**
	 * Tags a list of lineage motifs (and thereby all spots and links within them) with unique identifiers and colors.<br>
	 *
	 * This method generates a new tag set to categorize the given lineage motifs, where each motif is assigned
	 * a distinct tag and color. The tags and colors are applied to the spots within each motif.
	 * The tagging information is saved in the project's tag set structure. <br>
	 *
	 * The colors for the tags are generated as a saturation fade from the provided base color. The first motif will get the given color,
	 * all later motifs will get colors with less saturation compared to the base colors.<br>
	 *
	 * @param model The {@link Model} containing the branch graph for the lineage motifs.
	 * @param tagSetName The name to be assigned to the new tag set.
	 * @param lineageMotifs A {@link List} of {@link BranchSpotTree} objects representing the lineage motifs to tag.
	 * @param color The {@link Color} used as the base for generating unique colors for each motif's tag.
	 */
	public static void tagLineageMotifs( final Model model, final String tagSetName,
			final List< Pair< BranchSpotTree, Double > > lineageMotifs, final Color color )
	{
		final int count = lineageMotifs.size();
		final List< Color > colors = ColorUtils.generateSaturationFade( color, count );
		final List< Map.Entry< String, Integer > > tagsAndColors = new ArrayList<>();
		final ReentrantReadWriteLock.WriteLock lock = model.getGraph().getLock().writeLock();
		lock.lock();
		try
		{
			for ( int i = 0; i < count; i++ )
			{
				BranchSpotTree lineageMotif = lineageMotifs.get( i ).getLeft();
				double distance = lineageMotifs.get( i ).getRight();
				String lineageMotifName = getLineageMotifName( model, lineageMotif );
				String tag = TAG_NAME + " " + lineageMotifName + " (distance: " + String.format( "%.2f", distance ) + ")";
				tagsAndColors.add( Pair.of( tag, colors.get( i ).getRGB() ) );
			}
			TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, tagSetName, tagsAndColors );
			final AtomicInteger tagIndex = new AtomicInteger();
			lineageMotifs.forEach( motif -> {
				TagSetStructure.Tag tag = tagSet.getTags().get( tagIndex.getAndIncrement() );
				BranchSpotTree lineageMotif = motif.getLeft();
				tagSpotsAndLinksWithinTimeInterval( model, lineageMotif, lineageMotif.getStartTimepoint(), lineageMotif.getEndTimepoint(),
						tagSet, tag );
			} );
			model.setUndoPoint();
		}
		finally
		{
			lock.unlock();
		}
	}

	private static void tagSpotsAndLinksWithinTimeInterval( final Model model, final BranchSpotTree lineageMotif, final int startTimepoint,
			final int endTimepoint, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag )
	{
		DepthFirstIteration.forRoot( model.getBranchGraph(), lineageMotif.getBranchSpot() ).forEach(
				iterationStep -> {
					if ( !iterationStep.isFirstVisit() )
						return; // only consider the first visit to each branch spot
					BranchSpot branchSpot = iterationStep.node();
					Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( branchSpot );
					while ( spotIterator.hasNext() )
					{
						Spot spot = spotIterator.next();
						if ( spot.getTimepoint() < startTimepoint || spot.getTimepoint() > endTimepoint )
							continue; // skip spots outside the time range
						TagSetUtils.tagSpot( model, tagSet, tag, spot );
						if ( spot.getTimepoint() < endTimepoint )
							TagSetUtils.tagLinks( model, tagSet, tag, spot.outgoingEdges() );
					}
					model.getBranchGraph().releaseIterator( spotIterator );
				} );
	}

	/**
	 * Retrieves the name of a lineage motif based on its first spot's label at the start timepoint.
	 * If no label is found, a default name {@link #DEFAULT_LINEAGE_MOTIF_NAME} is returned.
	 *
	 * @param model the {@link Model} containing the graph and branch information
	 * @param lineageMotif the {@link BranchSpotTree} representing the lineage motif
	 * @return the name of the lineage motif as a {@link String}, or a default name if no label is found
	 */
	public static String getLineageMotifName( final Model model, final BranchSpotTree lineageMotif )
	{
		String name = null;
		Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( lineageMotif.getBranchSpot() );
		while ( spotIterator.hasNext() )
		{
			Spot spot = spotIterator.next();
			if ( spot.getTimepoint() == lineageMotif.getStartTimepoint() )
			{
				name = spot.getLabel();
				break; // we only need the label of the first spot in the lineage motif
			}
		}
		model.getBranchGraph().releaseIterator( spotIterator );
		if ( name == null )
		{
			logger.debug( "Could not find a label for the lineage motif. Using default name." );
			name = DEFAULT_LINEAGE_MOTIF_NAME;
		}
		return name;
	}

	/**
	 * Counts the number of divisions (branching points) in a given lineage motif
	 * represented by a {@link BranchSpotTree} within the time interval of the motif, using the provided {@link Model}.
	 * A division is identified as a spot within the motif that has more than one outgoing edge.
	 *
	 * @param lineageMotif the {@link BranchSpotTree} representing the lineage motif to analyze
	 * @param model the {@link Model} containing the graph information used for traversal and analysis
	 * @return the number of division events within the specified lineage motif,
	 *         or 0 if no valid root spot is found or no divisions are present
	 */
	public static int getNumberOfDivisions( final BranchSpotTree lineageMotif, final Model model )
	{
		Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( lineageMotif.getBranchSpot() );
		AtomicInteger divisionCount = new AtomicInteger();
		try
		{
			Spot rootSpot = null;
			while ( spotIterator.hasNext() )
			{
				Spot spot = spotIterator.next();
				if ( spot.getTimepoint() == lineageMotif.getStartTimepoint() )
				{
					rootSpot = spot;
					break;
				}
			}
			if ( rootSpot == null )
			{
				logger.debug( "Could not find a root spot for the lineage motif. Returning 0 as division count." );
				return 0;
			}
			DepthFirstIteration.forRoot( model.getGraph(), rootSpot ).forEach( iterationStep -> {
				Spot spot = iterationStep.node();
				if ( iterationStep.isFirstVisit() )
				{
					if ( spot.getTimepoint() >= lineageMotif.getEndTimepoint() )
						iterationStep.truncate(); // do not traverse further if the spot is after the end timepoint of the motif
					if ( spot.outgoingEdges().size() > 1 )
						divisionCount.incrementAndGet();
				}
			} );
		}
		finally
		{
			model.getBranchGraph().releaseIterator( spotIterator );
		}
		return divisionCount.get();
	}
}
