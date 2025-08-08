package org.mastodon.mamut.lineagemotifs.ui;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.clustering.config.HasName;
import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.clustering.ui.Notification;
import org.mastodon.mamut.lineagemotifs.util.InvalidLineageMotifException;
import org.mastodon.mamut.lineagemotifs.util.LineageMotifsUtils;
import org.scijava.Context;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.util.ColorRGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for finding similar lineage motifs.
 * Subclasses only need to provide the motif source and scale factor.
 */
public abstract class AbstractFindLineageMotifsCommand extends DynamicCommand
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	protected static final float WIDTH = 15f;

	@SuppressWarnings( "unused" )
	@Parameter
	protected ProjectModel projectModel;

	protected CountDownLatch latch;

	@Override
	public void run()
	{
		Context context = getContext();
		ThreadService threadService = context.getService( ThreadService.class );
		threadService.run( () -> findMotifs( isRunOnBranchGraph() ) );
	}

	private void findMotifs( boolean runOnBranchGraph )
	{
		BranchSpotTree lineageMotif;
		try
		{
			lineageMotif = getMotif();
		}
		catch ( InvalidLineageMotifException e )
		{
			logger.warn( e.getLogMessage() );
			Notification.showError( e.getUiTitle(), e.getUiMessage() );
			return;
		}
		catch ( IOException e )
		{
			logger.warn( e.getMessage(), e );
			Notification.showError( "Error", "An error occurred while reading the lineage motif: " + e.getMessage() );
			return;
		}
		if ( runOnBranchGraph )
			projectModel.getBranchGraphSync().sync();
		try
		{
			List< Pair< BranchSpotTree, Double > > similarMotifs = LineageMotifsUtils.getMostSimilarMotifs( lineageMotif,
					getNumberOfSimilarLineage(), SimilarityMeasure.getByName( getSimilarityMeasure() ), getScaleFactor(), !runOnBranchGraph,
					projectModel.getModel() );
			LineageMotifsUtils.tagMotifs( projectModel.getModel(), lineageMotif, similarMotifs, getColor(), latch );
		}
		catch ( Exception e )
		{
			logger.error( "Error while finding similar lineage motifs.", e );
			Notification.showError( "Error", "An error occurred while finding similar lineage motifs: " + e.getMessage() );
		}
		finally
		{
			cleanUp();
		}
	}

	@SuppressWarnings( "unused" )
	private void initSimilarityMeasureChoices()
	{
		getInfo().getMutableInput( "similarityMeasure", String.class ).setChoices( enumNamesAsList( SimilarityMeasure.values() ) );
	}

	private static List< String > enumNamesAsList( final HasName[] values )
	{
		return Arrays.stream( values ).map( HasName::getName ).collect( Collectors.toList() );
	}

	/**
	 * Implement this to provide the motif to compare against.
	 */
	protected abstract BranchSpotTree getMotif() throws InvalidLineageMotifException, IOException;

	/**
	 * Implement this to provide the scaling factor. For motifs from the same project this .
	 */
	protected abstract double getScaleFactor();

	/**
	 * Cleans up resources or operations after the execution of the command.
	 * This method is designed to be implemented by subclasses to handle any
	 * necessary cleanup tasks specific to their functionality.
	 */
	protected void cleanUp()
	{}

	protected abstract int getNumberOfSimilarLineage();

	protected abstract ColorRGB getColor();

	protected abstract String getSimilarityMeasure();

	protected abstract boolean isRunOnBranchGraph();
}
