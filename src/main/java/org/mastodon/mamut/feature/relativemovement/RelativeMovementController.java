package org.mastodon.mamut.feature.relativemovement;

import org.mastodon.mamut.feature.branch.movement.relative.BranchRelativeMovementFeatureComputer;
import org.mastodon.mamut.feature.spot.movement.relative.SpotRelativeMovementFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * A controller to compute relative movement of spots and branches in relation to nearest neighbors.
 */
public class RelativeMovementController
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final Model model;

	/**
	 * Creates a new relative movement controller.
	 * @param model the model to operate on.
	 */
	public RelativeMovementController( final Model model )
	{
		this.model = model;
	}

	/**
	 * Computes relative movement of spots and branches in relation to nearest neighbors.
	 * <p>
	 * Adds new features on spot and branch level to the model, except features with the same settings already exist.
	 *
	 * @param settings the settings to use for computing the relative movement.
	 * @param context the scijava context to operate in.
	 */
	public void computeRelativeMovement( final RelativeMovementFeatureSettings settings,
			final Context context )
	{
		logger.debug( "Computing relative movement on spot level using this settings: {}", settings );
		SpotRelativeMovementFeatureComputer spotFeatureComputer = new SpotRelativeMovementFeatureComputer( model, context );
		spotFeatureComputer.computeFeature( settings );
		logger.debug( "Computing relative movement on branch spot level using this settings: {}", settings );
		BranchRelativeMovementFeatureComputer branchFeatureComputer =
				new BranchRelativeMovementFeatureComputer( model, context, spotFeatureComputer.getFeature() );
		branchFeatureComputer.computeFeature();
		logger.debug( "Successfully computed relative movement." );
	}
}
