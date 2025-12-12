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
package org.mastodon.mamut.lineagemotifs.ui;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
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
			LineageMotifsUtils.tagMotifs( projectModel.getModel(), lineageMotif, similarMotifs, getColor1(), getColor2(),
					getScaleFactor() );
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

	protected abstract ColorRGB getColor1();

	protected abstract ColorRGB getColor2();

	protected abstract String getSimilarityMeasure();

	protected abstract boolean isRunOnBranchGraph();
}
