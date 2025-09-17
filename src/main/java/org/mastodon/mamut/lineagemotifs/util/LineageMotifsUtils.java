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
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.ui.Notification;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.util.ColorGenerator;
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
	public static BranchSpotTree getSelectedMotif( final ProjectModel projectModel ) throws InvalidLineageMotifException
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
		final int motifLength = ( int ) ( lineageMotif.getDuration() / scaleFactor );
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
					int endTimepoint = startTimepoint + motifLength + 1;
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
		final int motifStartTimepoint = lineageMotif.getStartTimepoint();
		final int firstDivisionTimepoint = lineageMotif.getBranchSpot().getTimepoint();
		final int timepointsUntilFirstDivision = ( int ) ( ( firstDivisionTimepoint - motifStartTimepoint + 1 ) / scaleFactor );

		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( searchModel.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( searchModel );
		final MotifContext motifContext =
				new MotifContext( lineageMotif, similarityMeasure, scaleFactor, motifLength, timepointsUntilFirstDivision );

		for ( BranchSpot branchSpot : searchModel.getBranchGraph().vertices() )
		{
			findBranchSpotCandidates( branchSpot, motifContext, maxTimepoint, searchModel, candidates );
		}
		return candidates;
	}

	private static void findBranchSpotCandidates( final BranchSpot branchSpot, final MotifContext motifContext, final int maxTimepoint,
			final Model searchModel, final RefDoubleMap< Spot > candidates )
	{
		int startTimepoint = branchSpot.getTimepoint() - motifContext.timepointsUntilFirstDivision + 1;
		int candidateMotifLength = ( int ) ( motifContext.motifLength / motifContext.scaleFactor );

		if ( maxTimepoint - startTimepoint < candidateMotifLength )
			return;

		BranchSpotTree candidateMotif =
				new BranchSpotTree( branchSpot, startTimepoint, startTimepoint + candidateMotifLength - 1, searchModel );

		double distance = motifContext.similarityMeasure.compute( motifContext.lineageMotif, candidateMotif, motifContext.scaleFactor );
		Spot candidateSpot = findCandidateSpotInBranchSpot( branchSpot, startTimepoint, searchModel );

		if ( candidateSpot != null )
			candidates.put( candidateSpot, distance );
	}

	private static Spot findCandidateSpotInBranchSpot( final BranchSpot branchSpot, final int startTimepoint, final Model searchModel )
	{
		Iterator< Spot > spotIterator = searchModel.getBranchGraph().vertexBranchIterator( branchSpot );
		while ( spotIterator.hasNext() )
		{
			Spot spot = spotIterator.next();
			if ( spot.getTimepoint() == startTimepoint )
				return spot;
		}
		// fallback: return the first spot, if available
		Iterator< Spot > fallback = searchModel.getBranchGraph().vertexBranchIterator( branchSpot );
		return fallback.hasNext() ? fallback.next() : null;
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
			int motifLength = ( int ) ( lineageMotif.getDuration() / scaleFactor );
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
			if ( searchModel.equals( lineageMotif.getModel() ) && scaleFactor == 1 ) // add the selected motif itself in case of scale equals 1
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
					int endTimepoint = startTimepoint + motifLength - 1;
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

	private static List< Pair< BranchSpotTree, Double > > getMotifsSortedByGraphDepth( final List< Pair< BranchSpotTree, Double > > motifs )
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
	 * The colors for the tags are generated as a gradient interpolation between the provided colors. The first motif (i.e., the most similar motif) will get {@code color1},
	 * the last motif (i.e., the least similar motif) will get {@code color2}. The motifs in between get colors interpolated between these two colors.<br>
	 *
	 * @param model The {@link Model} containing the graph data and tag sets.
	 * @param tagSetName The name to be assigned to the new tag set.
	 * @param lineageMotifs A {@link List} of {@link BranchSpotTree} objects representing the lineage motifs to tag.
	 * @param color1 The {@link Color} to be used for the first motif (most similar).
	 * @param color2 The {@link Color} to be used for the last motif (least similar).
	 *
	 */
	public static void tagLineageMotifs( final Model model, final String tagSetName,
			final List< Pair< BranchSpotTree, Double > > lineageMotifs, final Color color1, final Color color2 )
	{
		final int count = lineageMotifs.size();
		final List< Color > colors = ColorGenerator.interpolateColors( color1, color2, count );
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
	 * color for coloring the tags.
	 *
	 * @param model the {@link Model} containing the graph data and tag sets
	 * @param originalMotif the {@link BranchSpotTree} representing the reference lineage motif
	 * @param similarMotifs a {@link List} of pairs where each pair consists of a {@link BranchSpotTree}
	 *                      representing a similar motif and a {@link Double} value denoting its similarity score
	 * @param color1 the {@link ColorRGB} specifying the first color for color interpolation to generate motif tags
	 * @param color2 the {@link ColorRGB} specifying the second color for color interpolation to generate motif tags
	 */
	public static void tagMotifs( final Model model, final BranchSpotTree originalMotif,
			final List< Pair< BranchSpotTree, Double > > similarMotifs, final ColorRGB color1, final ColorRGB color2,
			final double scaleFactor )
	{
		String tagSetName;
		int numberOfDivisions = originalMotif.getNumberOfDivisions();
		String lineageMotifName = originalMotif.getStartSpotName();
		String optionalPlural = numberOfDivisions == 1 ? "" : "s";
		String optionalScale = scaleFactor == 1 ? "" : ", scaled by " + 1 / scaleFactor;
		tagSetName = TAG_SET_NAME + lineageMotifName + " (" + numberOfDivisions + " division" + optionalPlural + optionalScale + ")";
		LineageMotifsUtils.tagLineageMotifs( model, tagSetName, similarMotifs, new Color( color1.getARGB() ),
				new Color( color2.getARGB() ) );
		Notification.showSuccess( "Finding similar lineage motifs finished.", "New tag set added: " + tagSetName );
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

	private static class MotifContext
	{
		private final BranchSpotTree lineageMotif;

		private final SimilarityMeasure similarityMeasure;

		private final double scaleFactor;

		private final int motifLength;

		private final int timepointsUntilFirstDivision;

		private MotifContext( final BranchSpotTree lineageMotif, final SimilarityMeasure similarityMeasure, final double scaleFactor,
				final int motifLength, final int timepointsUntilFirstDivision )
		{
			this.lineageMotif = lineageMotif;
			this.similarityMeasure = similarityMeasure;
			this.scaleFactor = scaleFactor;
			this.motifLength = motifLength;
			this.timepointsUntilFirstDivision = timepointsUntilFirstDivision;
		}
	}
}
