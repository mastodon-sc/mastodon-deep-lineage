package org.mastodon.mamut.lineagemotifs.util;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.RefPool;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
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
		DepthFirstIterator< Spot, Link > depthFirstIterator = new DepthFirstIterator<>( model.getGraph() );
		depthFirstIterator.reset( selectedRoot );
		depthFirstIterator.forEachRemaining( spot -> {
			boolean isSelected = selectionModel.isSelected( spot );
			if ( isSelected && spot.getTimepoint() > highestTimepoint.get() )
				highestTimepoint.set( spot.getTimepoint() );
		}
		);
		return highestTimepoint.get();
	}

	/**
	 * Gets the similarity of the given lineage motif to all other motifs in the model.
	 * The method iterates over all spots in the graph and constructs a lineage motif for each of these spots with the same length as the given lineage motif.
	 *
	 * @param lineageMotif the {@link BranchSpotTree} representing the given lineage motif
	 * @param branchRef a reference to the branch graph
	 * @param similarityMeasure the {@link SimilarityMeasure} to use for calculating the similarity
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage motif
	 */
	static RefDoubleMap< Spot > getMotifSimilarityBySpotIteration( final BranchSpotTree lineageMotif,
			final SimilarityMeasure similarityMeasure, final BranchSpot branchRef )
	{
		Model model = lineageMotif.getModel();
		final int motifLength = getMotifLength( lineageMotif );
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( model.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		RefSet< Spot > roots = RootFinder.getRoots( model.getGraph() );
		for ( Spot root : roots )
		{
			DepthFirstIterator< Spot, Link > depthFirstIterator = new DepthFirstIterator<>( model.getGraph() );
			depthFirstIterator.reset( root );
			depthFirstIterator.forEachRemaining( spot -> {
				if ( maxTimepoint - spot.getTimepoint() + 1 >= motifLength )
				{
					int startTimepoint = spot.getTimepoint();
					int endTimepoint = startTimepoint + motifLength;
					BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( spot, branchRef );
					BranchSpotTree candidateMotif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
					double distance = similarityMeasure.compute( lineageMotif, candidateMotif );
					candidates.put( spot, distance );
				}
			} );
		}
		return candidates;
	}

	/**
	 * Gets the similarity of the given lineage module to all other modules in the model.
	 * The method iterates over all spots in the graph and constructs a lineage module for each of these spots with the same length as the given lineage module.
	 *
	 * @param lineageMotif the {@link BranchSpotTree} representing the given lineage module
	 * @param similarityMeasure the {@link SimilarityMeasure} to use for calculating the similarity
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage module
	 */
	static RefDoubleMap< Spot > getMotifSimilarityByBranchSpotIteration( final BranchSpotTree lineageMotif,
			final SimilarityMeasure similarityMeasure )
	{
		final int motifLength = getMotifLength( lineageMotif );
		int moduleStartTimepoint = lineageMotif.getStartTimepoint();
		int firstDivisionTimepoint = lineageMotif.getBranchSpot().getTimepoint();
		int timepointsUntilFirstDivision = firstDivisionTimepoint - moduleStartTimepoint;
		Model model = lineageMotif.getModel();
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( model.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		for ( BranchSpot branchSpot : model.getBranchGraph().vertices() )
		{
			int startTimepoint = branchSpot.getTimepoint() - timepointsUntilFirstDivision;
			if ( maxTimepoint - startTimepoint >= motifLength )
			{
				int endTimepoint = startTimepoint + motifLength;
				BranchSpotTree candidateMotif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
				double distance = similarityMeasure.compute( lineageMotif, candidateMotif );
				Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( branchSpot );
				// we need to iterate over the spots in the branch to get the correct spot for the candidate
				boolean foundMatchingSpot = false;
				Spot spot = null;
				while ( spotIterator.hasNext() )
				{
					spot = spotIterator.next();
					if ( spot.getTimepoint() == startTimepoint )
					{
						candidates.put( spot, distance );
						foundMatchingSpot = true;
						break;
					}
				}
				if ( !foundMatchingSpot && spot != null )
					candidates.put( spot, distance ); // if we don't find a matching spot, we still add the candidate with the distance using the last spot in the branch
				model.getBranchGraph().releaseIterator( spotIterator );
			}
		}
		return candidates;
	}

	static int getMotifLength( final BranchSpotTree lineageMotif )
	{
		return lineageMotif.getEndTimepoint() - lineageMotif.getStartTimepoint() + 1; // +1 because the end timepoint is inclusive
	}

	/**
	 * Gets the most similar lineage motifs to a given motif based on their similarity scores.
	 * This method identifies and ranks other motifs compared to a reference motif
	 * and returns a list of the most similar ones up to a specified maximum number, including their similarity scores.
	 *
	 * @param lineageMotif    the {@link BranchSpotTree} representing the lineage motif to compare
	 * @param maxNumberOfMotifs the maximum number of similar motifs to retrieve
	 * @param similarityMeasure the {@link SimilarityMeasure} used to compute the similarity between motifs
	 * @param spotRef         a reference {@link Spot} used for accessing the graph's objects
	 * @param branchRef       a reference {@link BranchSpot} associated with the branch graph
	 * @param isSpotIteration  a boolean indicating whether to use spot iteration or branch spot iteration for similarity calculation.
	 * Spot iteration may take significantly longer than branch spot iteration, but it is more accurate.
	 * @return a {@link List} of {@link BranchSpotTree} objects representing the most similar lineage motifs, including their similarity scores.
	 */
	public static List< Pair< BranchSpotTree, Double > > getMostSimilarMotifs( final BranchSpotTree lineageMotif,
			int maxNumberOfMotifs, final SimilarityMeasure similarityMeasure, final Spot spotRef, final BranchSpot branchRef,
			boolean isSpotIteration )
	{
		int motifLength = getMotifLength( lineageMotif );
		RefDoubleMap< Spot > candidates;
		if ( isSpotIteration )
			candidates = getMotifSimilarityBySpotIteration( lineageMotif, similarityMeasure, branchRef );
		else
			candidates = getMotifSimilarityByBranchSpotIteration( lineageMotif, similarityMeasure );

		Model model = lineageMotif.getModel();
		RefPool< Spot > refPool = model.getGraph().vertices().getRefPool();
		List< Pair< BranchSpotTree, Double > > motifsSortedByDistance = new ArrayList<>();
		List< Pair< Integer, Double > > sortedMotifIds = getSortedMotifIds( candidates, refPool );
		logger.debug( "lineage motif: {}, length: {}", lineageMotif, motifLength );
		int addedMotifs = 0;
		motifsSortedByDistance.add( Pair.of( lineageMotif, 0d ) );
		for ( Pair< Integer, Double > entry : sortedMotifIds )
		{
			Spot spot = refPool.getObject( entry.getLeft(), spotRef );
			double distance = entry.getRight();
			BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( spot, model.getBranchGraph().vertexRef() );

			// Only proceed if this is not the motif itself
			if ( !lineageMotif.getBranchSpot().equals( branchSpot ) )
			{
				int startTimepoint = spot.getTimepoint();
				int endTimepoint = startTimepoint + motifLength;
				BranchSpotTree motif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
				motifsSortedByDistance.add( Pair.of( motif, distance ) );
				addedMotifs++;
			}

			if ( addedMotifs >= maxNumberOfMotifs )
				break;
		}
		motifsSortedByDistance.forEach(
				motifDistancePair -> logger.debug( "motif: {}, distance: {}", motifDistancePair.getLeft(), motifDistancePair.getRight() ) );
		return getMotifsSortedByGraphDepth( motifsSortedByDistance );
	}

	static List< Pair< BranchSpotTree, Double > > getMotifsSortedByGraphDepth( final List< Pair< BranchSpotTree, Double > > motifs )
	{
		Map< Pair< BranchSpotTree, Double >, Integer > depthMap = new HashMap<>();
		motifs.forEach( pair -> depthMap.put( pair, getGraphDepth( pair.getKey() ) ) );
		return motifs.stream().sorted( Comparator.comparingInt( depthMap::get ) ).collect( Collectors.toList() );
	}

	static int getGraphDepth( final BranchSpotTree motif )
	{
		final Model model = motif.getModel();
		InverseDepthFirstIterator< BranchSpot, BranchLink > iterator = new InverseDepthFirstIterator<>( model.getBranchGraph() );
		iterator.reset( motif.getBranchSpot() );
		AtomicInteger depth = new AtomicInteger( 0 );
		iterator.forEachRemaining( bs -> depth.incrementAndGet() );
		return depth.get();
	}

	private static List< Pair< Integer, Double > > getSortedMotifIds( final RefDoubleMap< Spot > candidates, final RefPool< Spot > refPool )
	{
		Map< Integer, Double > idsAndDistances = candidates.keySet().stream()
				.collect( Collectors.toMap( refPool::getId, candidates::get ) );

		return idsAndDistances.entrySet().stream()
				.map( e -> Pair.of( e.getKey(), e.getValue() ) )
				.sorted( Comparator.comparingDouble( Pair::getValue ) )
				.collect( Collectors.toList() );
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
	 * @param tagSetName The name to be assigned to the new tag set.
	 * @param lineageMotifs A {@link List} of {@link BranchSpotTree} objects representing the lineage motifs to tag.
	 * @param color The {@link Color} used as the base for generating unique colors for each motif's tag.
	 */
	public static void tagLineageMotifs( final Model model, final String tagSetName,
			final List< Pair< BranchSpotTree, Double > > lineageMotifs, final Color color )
	{
		final int count = lineageMotifs.size();
		final List< Color > colors = ColorUtils.generateSaturationFade( color, count );
		final ReentrantReadWriteLock.WriteLock lock = model.getGraph().getLock().writeLock();
		lock.lock();
		try
		{
			List< IndexedMotif > indexedMotifs = new ArrayList<>();
			for ( int i = 0; i < count; i++ )
			{
				indexedMotifs.add( new IndexedMotif( lineageMotifs.get( i ), i ) );
			}
			indexedMotifs.sort( Comparator.comparingDouble( indexedMotif -> indexedMotif.motifAndDistance.getValue() ) );
			AtomicInteger colorIndex = new AtomicInteger( 0 );
			final List< Map.Entry< String, Integer > > tagsAndColors =
					indexedMotifs.stream()
							.map( indexedMotif -> Pair.of(
									TAG_NAME + getLineageMotifName( indexedMotif.motifAndDistance.getKey() ) + " (distance: "
											+ String.format( "%.2f", indexedMotif.motifAndDistance.getValue() ) + ")",
									colors.get( colorIndex.getAndIncrement() ).getRGB() ) )
							.collect( Collectors.toList() );
			TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, tagSetName, tagsAndColors );

			Map< Integer, Integer > originalToNewIndexMap = new HashMap<>();

			for ( int newIndex = 0; newIndex < indexedMotifs.size(); newIndex++ )
			{
				IndexedMotif indexMotif = indexedMotifs.get( newIndex );
				originalToNewIndexMap.put( indexMotif.originalIndex, newIndex );
			}

			AtomicInteger tagIndex = new AtomicInteger( 0 );
			lineageMotifs.forEach( motif -> {
				TagSetStructure.Tag tag = tagSet.getTags().get( originalToNewIndexMap.get( tagIndex.getAndIncrement() ) );
				BranchSpotTree lineageMotif = motif.getLeft();
				tagSpotsAndLinksWithinTimeInterval( lineageMotif, lineageMotif.getStartTimepoint(), lineageMotif.getEndTimepoint(), tagSet,
						tag );
			} );
			model.setUndoPoint();
		}
		finally
		{
			lock.unlock();
		}
		logger.info( "Tagged {} lineage motifs", count );
	}

	private static void tagSpotsAndLinksWithinTimeInterval( final BranchSpotTree lineageMotif, final int startTimepoint,
			final int endTimepoint, final TagSetStructure.TagSet tagSet, final TagSetStructure.Tag tag )
	{
		Model model = lineageMotif.getModel();
		DepthFirstIterator< BranchSpot, BranchLink > depthFirstIterator = new DepthFirstIterator<>( model.getBranchGraph() );
		depthFirstIterator.reset( lineageMotif.getBranchSpot() );
		DepthFirstIteration.forRoot( model.getBranchGraph(), lineageMotif.getBranchSpot() ).forEach(
				iterationStep -> {
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
	 *
	 * @param lineageMotif the {@link BranchSpotTree} representing the lineage motif
	 * @return the name of the lineage motif as a {@link String}, or a default name if no label is found
	 */
	public static String getLineageMotifName( final BranchSpotTree lineageMotif )
	{
		return lineageMotif.getRootSpot().getLabel();
	}

	/**
	 * Counts the number of divisions (branching points) in a given lineage motif
	 * represented by a {@link BranchSpotTree} within the time interval of the motif, using the provided {@link Model}.
	 * A division is identified as a spot within the motif that has more than one outgoing edge.
	 *
	 * @param lineageMotif the {@link BranchSpotTree} representing the lineage motif to analyze
	 * @return the number of division events within the specified lineage motif,
	 *         or 0 if no valid root spot is found or no divisions are present
	 */
	public static int getNumberOfDivisions( final BranchSpotTree lineageMotif )
	{
		Model model = lineageMotif.getModel();
		Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( lineageMotif.getBranchSpot() );
		AtomicInteger divisionCount = new AtomicInteger();
		try
		{
			Spot rootSpot = lineageMotif.getRootSpot();
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

	private static class IndexedMotif
	{
		Pair< BranchSpotTree, Double > motifAndDistance;

		int originalIndex;

		IndexedMotif( final Pair< BranchSpotTree, Double > motifAndDistance, final int originalIndex )
		{
			this.motifAndDistance = motifAndDistance;
			this.originalIndex = originalIndex;
		}
	}
}
