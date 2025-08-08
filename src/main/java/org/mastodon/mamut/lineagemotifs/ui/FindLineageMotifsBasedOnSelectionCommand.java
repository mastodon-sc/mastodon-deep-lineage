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

import org.mastodon.mamut.clustering.config.SimilarityMeasure;
import org.mastodon.mamut.clustering.treesimilarity.tree.BranchSpotTree;
import org.mastodon.mamut.lineagemotifs.util.InvalidLineageMotifException;
import org.mastodon.mamut.lineagemotifs.util.LineageMotifsUtils;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * Command to find similar lineage motifs based on a selected lineage motif.
 */
@Plugin( type = Command.class, name = "Find similar lineage motif based on a selected lineage" )
public class FindLineageMotifsBasedOnSelectionCommand extends AbstractFindLineageMotifsCommand
{

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Find similar lineage motifs</h1>\n"
			+ "<p>This command finds a specifiable number of lineage motifs that are similar to the currently selected motif and assigns tags to them.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@Parameter( label = "Number of similar lineage motifs", min = "1", max = "1000", stepSize = "1" )
	protected int numberOfSimilarLineage = 10;

	@Parameter( label = "Color" )
	protected ColorRGB color = new ColorRGB( "red" );

	@SuppressWarnings( "unused" )
	@Parameter( label = "Similarity measure", initializer = "initSimilarityMeasureChoices", callback = "update" )
	public String similarityMeasure = SimilarityMeasure.NORMALIZED_ZHANG_DIFFERENCE.getName();

	@Parameter( label = "Run on branch graph", required = false, persist = false, description = "Running this command on the branch graph (recommended option) will be much faster, but a bit less accurate. Running it on the model graph will be more accurate, but slower." )
	protected boolean runOnBranchGraph = true;

	@Override
	protected BranchSpotTree getMotif() throws InvalidLineageMotifException
	{
		return LineageMotifsUtils.getSelectedMotif( projectModel );
	}

	@Override
	protected double getScaleFactor()
	{
		return 1d;
	}

	@Override
	protected int getNumberOfSimilarLineage()
	{
		return numberOfSimilarLineage;
	}

	@Override
	protected ColorRGB getColor()
	{
		return color;
	}

	@Override
	protected String getSimilarityMeasure()
	{
		return similarityMeasure;
	}

	@Override
	protected boolean isRunOnBranchGraph()
	{
		return runOnBranchGraph;
	}
}
