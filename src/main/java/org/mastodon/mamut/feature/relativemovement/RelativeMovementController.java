package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.mamut.feature.branch.movement.relative.BranchRelativeMovementFeatureComputer;
import org.mastodon.mamut.feature.spot.movement.relative.SpotRelativeMovementFeatureComputer;
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

	public void computeRelativeMovement( final boolean forceComputeAll, final RelativeMovementFeatureSettings settings,
			final Context context )
	{
		logger.debug( "Computing relative movement on spot level using this settings: {}", settings );
		SpotRelativeMovementFeatureComputer spotFeatureComputer = new SpotRelativeMovementFeatureComputer( model, context );
		spotFeatureComputer.computeFeature( forceComputeAll, settings );
		logger.debug( "Computing relative movement on branch spot level using this settings: {}", settings );
		BranchRelativeMovementFeatureComputer branchFeatureComputer =
				new BranchRelativeMovementFeatureComputer( model, context, spotFeatureComputer.getFeature() );
		branchFeatureComputer.computeFeature( forceComputeAll );
		logger.debug( "Successfully computed relative movement." );
	}
}
