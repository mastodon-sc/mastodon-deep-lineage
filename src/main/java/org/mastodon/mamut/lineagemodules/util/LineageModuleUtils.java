package org.mastodon.mamut.lineagemodules.util;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.RefPool;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.mamut.ProjectModel;
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

public class LineageModuleUtils
{

	public static final String DEFAULT_LINEAGE_MODULE_NAME = "Lineage Module";

	private LineageModuleUtils()
	{
		// Utility class, no instantiation allowed.
	}

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String TAG_NAME = "Lineage Module ";

	/**
	 * Retrieves the {@link BranchSpotTree} for the selected lineage module based on the given model and the current selection of the user.
	 * It ensures a valid selection of exactly one lineage module and throws an exception if none or multiple are selected.
	 *
	 * @param model the {@link Model} containing the graph and branch data
	 * @param selectionModel the {@link SelectionModel} indicating the currently selected spots and links
	 * @return the {@link BranchSpotTree} representing the selected lineage module
	 * @throws InvalidLineageModuleSelection if the selection is invalid (e.g., no spots selected, or multiple modules selected)
	 */
	public static BranchSpotTree getSelectedModule( final Model model, final SelectionModel< Spot, Link > selectionModel )
			throws InvalidLineageModuleSelection
	{
		RefSet< Spot > selectedRootSpots =
				SelectedRootsFinder.getRoots( model.getGraph(), selectionModel );
		if ( selectedRootSpots.isEmpty() )
			throw new InvalidLineageModuleSelection(
					"No selected spots found thus no lineage module can be found.", "No spots selected",
					"Please select spots to define a lineage module."
			);
		if ( selectedRootSpots.size() > 1 )
			throw new InvalidLineageModuleSelection(
					"Multiple lineage modules (" + selectedRootSpots.size() + ") found. Only one is allowed.",
					"Multiple modules selected",
					"You have selected " + selectedRootSpots.size() + " lineage modules. Please select only one."
			);
		Spot selectedRoot = selectedRootSpots.iterator().next();

		int startTimepoint = selectedRoot.getTimepoint();
		int endTimepoint = getEndTimepoint( selectedRoot, model, selectionModel );
		BranchSpot ref = model.getBranchGraph().vertexRef();
		BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( selectedRoot, ref );
		return new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
	}

	/**
	 * Gets the end timepoint of the selected lineage module.
	 * @param selectedRoot the selected root {@link Spot} of the lineage module
	 * @param model the {@link Model} containing the graph
	 * @param selectionModel the {@link SelectionModel} containing the selected {@link Spot}s
	 * @return the end timepoint of the lineage module, i.e. the maximum timepoint of all selected {@link Spot}s in the lineage module
	 */
	static int getEndTimepoint( final Spot selectedRoot, final Model model, final SelectionModel< Spot, Link > selectionModel )
	{
		AtomicInteger highestTimepoint = new AtomicInteger( selectedRoot.getTimepoint() );
		Iterable< DepthFirstIteration.Step< Spot > > depthFirstIteration = DepthFirstIteration.forRoot( model.getGraph(), selectedRoot );
		depthFirstIteration.forEach( spotStep -> {
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
	 * Gets the similarity of the given lineage module to all other modules in the model.
	 * The method iterates over all spots in the graph and constructs a lineage module for each of these spots with the same length as the given lineage module.
	 * The similarity is calculated as the normalized distance between the two modules using the {@link TreeDistances#normalizedDistance(Tree, Tree, ToDoubleBiFunction)} method.
	 *
	 * @param model the {@link Model} containing the graph
	 * @param lineageModule the {@link BranchSpotTree} representing the given lineage module
	 * @param branchRef a reference to the branch graph
	 * @return a {@link RefDoubleMap} of {@link Spot}s and their respective similarity to the given lineage module
	 */
	static RefDoubleMap< Spot > getModuleSimilarity( final Model model, final BranchSpotTree lineageModule, final BranchSpot branchRef )
	{
		final int moduleLength = lineageModule.getEndTimepoint() - lineageModule.getStartTimepoint();
		RefDoubleMap< Spot > candidates = new RefDoubleHashMap<>( model.getGraph().vertices().getRefPool(), Double.MAX_VALUE );
		final int maxTimepoint = TreeUtils.getMaxTimepoint( model );
		RefSet< Spot > roots = RootFinder.getRoots( model.getGraph() );
		for ( Spot root : roots )
		{
			Iterable< DepthFirstIteration.Step< Spot > > depthFirstIteration =
					DepthFirstIteration.forRoot( model.getGraph(), root );
			depthFirstIteration.forEach( spotStep -> {
				Spot spot = spotStep.node();
				if ( maxTimepoint - spot.getTimepoint() < moduleLength )
					spotStep.truncate(); // do not traverse further if the module does not fit into the remaining time points
				else
				{
					int startTimepoint = spot.getTimepoint();
					int endTimepoint = startTimepoint + moduleLength;
					BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( spot, branchRef );
					BranchSpotTree candidateModule = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
					double distance = TreeDistances.normalizedDistance(
							lineageModule, candidateModule,
							TreeDistances.LOCAL_ABSOLUTE_COST_FUNCTION
					);
					candidates.put( spot, distance );
				}
			} );
		}
		return candidates;
	}

	/**
	 * Gets the most similar lineage modules to a given module based on their similarity scores.
	 * This method identifies and ranks other modules compared to a reference module
	 * and returns a list of the most similar ones up to a specified maximum number.
	 *
	 * @param model           the {@link Model} containing the graph and branch data for the lineage
	 * @param lineageModule   the {@link BranchSpotTree} representing the lineage module to compare
	 * @param maxNumberOfModules the maximum number of similar modules to retrieve
	 * @param spotRef         a reference {@link Spot} used for accessing the graph's objects
	 * @param branchRef       a reference {@link BranchSpot} associated with the branch graph
	 * @return a {@link List} of {@link BranchSpotTree} objects representing the most similar lineage modules
	 */
	public static List< BranchSpotTree > getMostSimilarModules( final Model model, final BranchSpotTree lineageModule,
			int maxNumberOfModules, final Spot spotRef, final BranchSpot branchRef )
	{
		int moduleLength = lineageModule.getEndTimepoint() - lineageModule.getStartTimepoint();
		RefDoubleMap< Spot > candidates = getModuleSimilarity( model, lineageModule, branchRef );
		List< BranchSpotTree > modules = new ArrayList<>();
		RefPool< Spot > refPool = model.getGraph().vertices().getRefPool();
		Map< Integer, Double > distances = candidates.keySet().stream()
				.collect( Collectors.toMap( refPool::getId, candidates::get ) );

		List< Pair< Integer, Double > > sortedDistances = distances.entrySet().stream()
				.map( e -> Pair.of( e.getKey(), e.getValue() ) )
				.sorted( Comparator.comparingDouble( Pair::getValue ) )
				.collect( Collectors.toList() );

		int max = Math.min( sortedDistances.size(), maxNumberOfModules );
		int entries = 0;
		int i = 0;

		while ( entries < max )
		{
			Pair< Integer, Double > entry = sortedDistances.get( i );
			i++;
			Spot spot = refPool.getObject( entry.getLeft(), spotRef );
			logger.debug( "Spot: {} has a distance of: {}", spot.getLabel(), entry.getRight() );
			BranchSpot branchSpot = model.getBranchGraph().getBranchVertex( spot, model.getBranchGraph().vertexRef() );
			int startTimepoint = spot.getTimepoint();
			int endTimepoint = startTimepoint + moduleLength;
			BranchSpotTree module = new BranchSpotTree( branchSpot, startTimepoint, endTimepoint, model );
			if ( lineageModule.getBranchSpot().equals( branchSpot ) )
			{
				logger.debug( "Skipping module with the same branch spot as the selected module." );
				continue; // skip the module if it has the same branch spot as the selected module
			}
			modules.add( module );
			entries++;
		}
		return modules;
	}

	/**
	 * Tags a list of lineage modules (and thereby all spots within them) with unique identifiers and colors.<br>
	 *
	 * This method generates a new tag set to categorize the given lineage modules, where each module is assigned
	 * a distinct tag and color. The tags and colors are applied to the spots within each module.
	 * The tagging information is saved in the project's tag set structure. <br>
	 *
	 * The colors for the tags are generated as a saturation fade from the provided base color. The first module will get the given color,
	 * all later modules will get colors with less saturation compared to the base colors.<br>
	 *
	 * @param projectModel The {@link ProjectModel} containing the model data and branch graph for the lineage modules.
	 * @param tagSetName The name to be assigned to the new tag set.
	 * @param lineageModules A {@link List} of {@link BranchSpotTree} objects representing the lineage modules to tag.
	 * @param color The {@link Color} used as the base for generating unique colors for each module's tag.
	 */
	public static void tagLineageModules(
			final ProjectModel projectModel, final String tagSetName, final List< BranchSpotTree > lineageModules, final Color color )
	{
		final int count = lineageModules.size();
		final List< Color > colors = ColorUtils.generateSaturationFade( color, count );
		final List< Map.Entry< String, Integer > > tagsAndColors = new ArrayList<>();
		for ( int i = 0; i < count; i++ )
		{
			BranchSpotTree lineageModule = lineageModules.get( i );
			String lineageModuleName = getLineageModuleName( projectModel.getModel(), lineageModule );
			String tag = TAG_NAME + " " + lineageModuleName;
			tagsAndColors.add( Pair.of( tag, colors.get( i ).getRGB() ) );
		}
		final Model model = projectModel.getModel();
		TagSetStructure.TagSet tagSet = TagSetUtils.addNewTagSetToModel( model, tagSetName, tagsAndColors );
		final AtomicInteger tagIndex = new AtomicInteger();
		lineageModules.forEach( module -> {
			TagSetStructure.Tag tag = tagSet.getTags().get( tagIndex.getAndIncrement() );
			int startTimepoint = module.getStartTimepoint();
			int endTimepoint = module.getEndTimepoint();
			DepthFirstIteration.forRoot( projectModel.getModel().getBranchGraph(), module.getBranchSpot() ).forEach(
					spotBranch -> {
						BranchSpot branchSpot = spotBranch.node();
						Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( branchSpot );
						while ( spotIterator.hasNext() )
						{
							Spot spot = spotIterator.next();
							if ( spot.getTimepoint() < startTimepoint || spot.getTimepoint() > endTimepoint )
								continue; // skip spots outside the time range
							TagSetUtils.tagSpot( projectModel.getModel(), tagSet, tag, spot );
						}
						model.getBranchGraph().releaseIterator( spotIterator );
					} );
		} );
	}

	/**
	 * Retrieves the name of a lineage module based on its first spot's label at the start timepoint.
	 * If no label is found, a default name {@link #DEFAULT_LINEAGE_MODULE_NAME} is returned.
	 *
	 * @param model the {@link Model} containing the graph and branch information
	 * @param lineageModule the {@link BranchSpotTree} representing the lineage module
	 * @return the name of the lineage module as a {@link String}, or a default name if no label is found
	 */
	public static String getLineageModuleName( final Model model, final BranchSpotTree lineageModule )
	{
		String name = null;
		Iterator< Spot > spotIterator = model.getBranchGraph().vertexBranchIterator( lineageModule.getBranchSpot() );
		while ( spotIterator.hasNext() )
		{
			Spot spot = spotIterator.next();
			if ( spot.getTimepoint() == lineageModule.getStartTimepoint() )
			{
				name = spot.getLabel();
				break; // we only need the label of the first spot in the lineage module
			}
		}
		model.getBranchGraph().releaseIterator( spotIterator );
		if ( name == null )
		{
			logger.debug( "Could not find a label for the lineage module. Using default name." );
			name = DEFAULT_LINEAGE_MODULE_NAME;
		}
		return name;
	}
}
