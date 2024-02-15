package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.mamut.feature.spot.relativemovement.SpotRelativeMovementFeatureComputer;
import org.mastodon.mamut.feature.spot.relativemovement.SpotRelativeMovementFeatureSettings;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class RelativeMovementController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	public RelativeMovementController( final Model model )
	{
		this.model = model;
	}

	public void computeRelativeMovement( final boolean forceComputeAll, final SpotRelativeMovementFeatureSettings settings,
			final Context context )
	{
		logger.debug( "Computing relative movement." );
		SpotRelativeMovementFeatureComputer featureComputer = new SpotRelativeMovementFeatureComputer( model, context );
		featureComputer.computeFeature( forceComputeAll, settings );
		logger.debug( "Successfully computed relative movement." );
	}
}
