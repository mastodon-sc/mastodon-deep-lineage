package org.mastodon.mamut.lineagemotifs.util;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.ui.Notification;
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
import org.scijava.util.ColorRGB;
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

	private static final String TAG_SET_NAME = "Lineage motifs similar to ";

	/**
	 * Retrieves the {@link BranchSpotTree} for the selected lineage motif based on the given model and the current selection of the user.
	 * It ensures a valid selection of exactly one lineage motif and throws an exception if none or multiple are selected.
	 *
	 * @param projectModel the {@link ProjectModel} containing the model and selection
	 * @return the {@link BranchSpotTree} representing the selected lineage motif
	 * @throws InvalidLineageMotifException if the selection is invalid (e.g., no spots selected, or multiple motifs selected)
	 */
	public static BranchSpotTree getSelectedMotif( final ProjectModel projectModel )
			throws InvalidLineageMotifException
	{
		Model model = projectModel.getModel();
		SelectionModel< Spot, Link > selectionModel = projectModel.getSelectionModel();
		RefSet< Spot > selectedRootSpots = SelectedRootsFinder.getRoots( model.getGraph(), selectionModel );
		if ( selectedRootSpots.isEmpty() )
			throw new InvalidLineageMotifException(
					"No selected spots found. Thus no lineage motif can be found.", "No spots selected",
					"Please select spots to define a lineage motif."
			);
		if ( selectedRootSpots.size() > 1 )
			throw new InvalidLineageMotifException(
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
	 * @param scaleFactor a scaling factor (i.e. in time) to apply to the similarity measure
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage motif
	 */
	static RefDoubleMap< Spot > getMotifSimilarityBySpotIteration( final BranchSpotTree lineageMotif,
			final SimilarityMeasure similarityMeasure, final BranchSpot branchRef, final double scaleFactor, final Model searchModel )
	{
		final int motifLength = lineageMotif.getDuration();
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( searchModel.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( searchModel );
		RefSet< Spot > roots = RootFinder.getRoots( searchModel.getGraph() );
		for ( Spot root : roots )
		{
			DepthFirstIterator< Spot, Link > depthFirstIterator = new DepthFirstIterator<>( searchModel.getGraph() );
			depthFirstIterator.reset( root );
			depthFirstIterator.forEachRemaining( spot -> {
				if ( maxTimepoint - spot.getTimepoint() + 1 >= motifLength )
				{
					int startTimepoint = spot.getTimepoint();
					int endTimepoint = startTimepoint + motifLength;
					BranchSpot branchSpot = searchModel.getBranchGraph().getBranchVertex( spot, branchRef );
					BranchSpotTree candidateMotif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, searchModel );
					double distance = similarityMeasure.compute( lineageMotif, candidateMotif, scaleFactor );
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
	 * @param scaleFactor a scaling factor (i.e. in time) to apply to the similarity measure
	 * @param searchModel the {@link Model} to search for similar lineage modules
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage module
	 */
	static RefDoubleMap< Spot > getMotifSimilarityByBranchSpotIteration( final BranchSpotTree lineageMotif,
			final SimilarityMeasure similarityMeasure, final double scaleFactor, final Model searchModel )
	{
		final int motifLength = lineageMotif.getDuration();
		int moduleStartTimepoint = lineageMotif.getStartTimepoint();
		int firstDivisionTimepoint = lineageMotif.getBranchSpot().getTimepoint();
		int timepointsUntilFirstDivision = firstDivisionTimepoint - moduleStartTimepoint;
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( searchModel.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( searchModel );
		for ( BranchSpot branchSpot : searchModel.getBranchGraph().vertices() )
		{
			int startTimepoint = branchSpot.getTimepoint() - timepointsUntilFirstDivision;
			if ( maxTimepoint - startTimepoint >= motifLength )
			{
				int endTimepoint = startTimepoint + motifLength;
				BranchSpotTree candidateMotif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, searchModel );
				double distance = similarityMeasure.compute( lineageMotif, candidateMotif, scaleFactor );
				Iterator< Spot > spotIterator = searchModel.getBranchGraph().vertexBranchIterator( branchSpot );
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
				searchModel.getBranchGraph().releaseIterator( spotIterator );
			}
		}
		return candidates;
	}

	/**
	 * Gets the most similar lineage motifs to a given motif based on their similarity scores.
	 * This method identifies and ranks other motifs compared to a reference motif
	 * and returns a list of the most similar ones up to a specified maximum number, including their similarity scores.
	 *
	 * @param lineageMotif    the {@link BranchSpotTree} representing the lineage motif to compare
	 * @param maxNumberOfMotifs the maximum number of similar motifs to retrieve
	 * @param similarityMeasure the {@link SimilarityMeasure} used to compute the similarity between motifs
	 * @param scaleFactor      a scaling factor (i.e. in time) to apply to the similarity measure
	 * @param isSpotIteration  a boolean indicating whether to use spot iteration or branch spot iteration for similarity calculation.
	 * Spot iteration may take significantly longer than branch spot iteration, but it is more accurate.
	 * @param searchModel     the {@link Model} in which to search for similar motifs
	 * @return a {@link List} of {@link BranchSpotTree} objects representing the most similar lineage motifs, including their similarity scores.
	 */
	public static List< Pair< BranchSpotTree, Double > > getMostSimilarMotifs( final BranchSpotTree lineageMotif,
			int maxNumberOfMotifs, final SimilarityMeasure similarityMeasure, final double scaleFactor, final boolean isSpotIteration,
			final Model searchModel )
	{
		Spot spotRef = searchModel.getGraph().vertexRef();
		BranchSpot branchRef = searchModel.getBranchGraph().vertexRef();
		try
		{
			int motifLength = lineageMotif.getDuration();
			RefDoubleMap< Spot > candidates;
			if ( isSpotIteration )
				candidates = getMotifSimilarityBySpotIteration( lineageMotif, similarityMeasure, branchRef, scaleFactor, searchModel );
			else
				candidates = getMotifSimilarityByBranchSpotIteration( lineageMotif, similarityMeasure, scaleFactor, searchModel );

			RefPool< Spot > refPool = searchModel.getGraph().vertices().getRefPool();
			List< Pair< BranchSpotTree, Double > > motifsSortedByDistance = new ArrayList<>();
			List< Pair< Integer, Double > > sortedMotifIds = getSortedMotifIds( candidates, refPool );
			logger.debug( "lineage motif: {}, length: {}", lineageMotif, motifLength );
			int addedMotifs = 0;
			if ( searchModel.equals( lineageMotif.getModel() ) )
				motifsSortedByDistance.add( Pair.of( lineageMotif, 0d ) );
			for ( Pair< Integer, Double > entry : sortedMotifIds )
			{
				Spot spot = refPool.getObject( entry.getLeft(), spotRef );
				double distance = entry.getRight();
				BranchSpot branchSpot = searchModel.getBranchGraph().getBranchVertex( spot, searchModel.getBranchGraph().vertexRef() );

				// Only proceed if this is not the motif itself
				if ( !lineageMotif.getBranchSpot().equals( branchSpot ) )
				{
					int startTimepoint = spot.getTimepoint();
					int endTimepoint = startTimepoint + motifLength;
					BranchSpotTree motif = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, searchModel );
					motifsSortedByDistance.add( Pair.of( motif, distance ) );
					addedMotifs++;
				}

				if ( addedMotifs >= maxNumberOfMotifs )
					break;
			}
			motifsSortedByDistance.forEach( motifDistancePair -> logger.debug( "motif: {}, distance: {}", motifDistancePair.getLeft(),
					motifDistancePair.getRight() ) );
			return getMotifsSortedByGraphDepth( motifsSortedByDistance );
		}
		finally
		{
			searchModel.getGraph().releaseRef( spotRef );
			searchModel.getBranchGraph().releaseRef( branchRef );
		}
	}

	static List< Pair< BranchSpotTree, Double > > getMotifsSortedByGraphDepth( final List< Pair< BranchSpotTree, Double > > motifs )
	{
		Map< Pair< BranchSpotTree, Double >, Integer > depthMap = new HashMap<>();
		motifs.forEach( pair -> depthMap.put( pair, pair.getKey().getGraphDepth() ) );
		return motifs.stream().sorted( Comparator.comparingInt( depthMap::get ) ).collect( Collectors.toList() );
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
									TAG_NAME + indexedMotif.motifAndDistance.getKey().getStartSpotName() + " (distance: "
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

	/**
	 * Tags similar lineage motifs in a model using a specified color and updates the tag set structure.
	 * <br>
	 * This method generates a uniquely named tag set based on the original lineage motif's name and
	 * number of divisions, assigns distinct tags to the provided list of similar motifs, and applies the given base
	 * color for coloring the tags. A notification is displayed upon successful completion, and an optional
	 * latch can be used for synchronization.
	 *
	 * @param model the {@link Model} containing the graph data and tag sets
	 * @param originalMotif the {@link BranchSpotTree} representing the reference lineage motif
	 * @param similarMotifs a {@link List} of pairs where each pair consists of a {@link BranchSpotTree}
	 *                      representing a similar motif and a {@link Double} value denoting its similarity score
	 * @param color the {@link ColorRGB} specifying the base color to generate motif tags
	 * @param latch an optional {@link CountDownLatch} to signal when the tagging is complete, can be null
	 */
	public static void tagMotifs( final Model model, final BranchSpotTree originalMotif,
			final List< Pair< BranchSpotTree, Double > > similarMotifs, final ColorRGB color, final CountDownLatch latch )
	{
		String tagSetName;
		int numberOfDivisions = originalMotif.getNumberOfDivisions();
		String lineageMotifName = originalMotif.getStartSpotName();
		String optionalPlural = numberOfDivisions == 1 ? "" : "s";
		tagSetName = TAG_SET_NAME + lineageMotifName + " (" + numberOfDivisions + " division" + optionalPlural + ")";
		LineageMotifsUtils.tagLineageMotifs( model, tagSetName, similarMotifs, new Color( color.getARGB() ) );
		Notification.showSuccess( "Finding similar lineage motifs finished.", "New tag set added: " + tagSetName );
		if ( latch != null )
			latch.countDown();
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
