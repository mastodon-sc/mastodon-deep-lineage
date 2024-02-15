/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
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
package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.mamut.feature.FeatureUtils;
import org.mastodon.mamut.feature.spot.relativemovement.SpotRelativeMovementFeature;
import org.mastodon.mamut.feature.spot.relativemovement.SpotRelativeMovementFeatureSettings;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Plugin( type = Command.class, label = "Compute movement in relation to nearest neighbors", initializer = "init" )
public class RelativeMovementView implements Command
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final int WIDTH = 15;

	@SuppressWarnings( "all" )
	@Parameter( visibility = ItemVisibility.MESSAGE, required = false, persist = false )
	private String documentation = "<html>\n"
			+ "<body width=" + WIDTH + "cm align=left>\n"
			+ "<h1>Export label image using ellipsoids</h1>\n"
			+ "<p>This plugin is capable of saving a label image to a file using the existing ellipsoids in Mastodon.</p>\n"
			+ "</body>\n"
			+ "</html>\n";

	@Parameter
	private Model model;

	@Parameter
	private Context context;

	@Parameter( label = "Force compute all" )
	private boolean forceComputeAll;

	@Parameter( label = "Number of nearest neighbors", min = "1", persist = false )
	private int nNearestNeighbors;

	@Override
	public void run()
	{
		RelativeMovementController controller = new RelativeMovementController( model );
		SpotRelativeMovementFeatureSettings settings = new SpotRelativeMovementFeatureSettings( nNearestNeighbors );
		controller.computeRelativeMovement( forceComputeAll, settings, context );
	}

	@SuppressWarnings( "unused" )
	private void init()
	{
		logger.debug( "init parameters from feature model, if a feature exists already" );
		SpotRelativeMovementFeature spotRelativeMovementFeature =
				FeatureUtils.getFeatureGeneric( model, SpotRelativeMovementFeature.SpotRelativeMovementFeatureSpec.class );
		SpotRelativeMovementFeatureSettings settings =
				spotRelativeMovementFeature == null ? new SpotRelativeMovementFeatureSettings() : spotRelativeMovementFeature.settings;
		nNearestNeighbors = settings.numberOfNeighbors;
	}
}
