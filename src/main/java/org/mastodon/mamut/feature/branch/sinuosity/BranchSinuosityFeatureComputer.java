package org.mastodon.mamut.feature.branch.sinuosity;

import net.imglib2.util.LinAlgHelpers;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Computes {@link BranchSinuosityFeature}
 */
@Plugin( type = MamutFeatureComputer.class )
public class BranchSinuosityFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelBranchGraph branchGraph;

	@Parameter( type = ItemIO.OUTPUT )
	private BranchSinuosityFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new BranchSinuosityFeature( new DoublePropertyMap<>( branchGraph.vertices().getRefPool(), 0 ) );
	}

	@Override
	public void run()
	{
		computeBranchSinuosity();
	}

	private void computeBranchSinuosity()
	{
		branchGraph.vertices().forEach( branchSpot -> output.map.set( branchSpot, computeSinuosity( branchSpot ) ) );
	}

	private double computeSinuosity( final BranchSpot branchSpot )
	{
		final double[] currentSpotCoordinates = new double[ branchSpot.numDimensions() ];

		Spot spot;
		double accumulatedLifeCycleDistance = 0d;

		final Iterator< Spot > spotIterator = branchGraph.vertexBranchIterator( branchSpot );
		if ( !spotIterator.hasNext() )
			return Double.NaN;
		spot = spotIterator.next();
		if ( !spotIterator.hasNext() )
			return Double.NaN;

		spot.localize( currentSpotCoordinates );
		double[] previousSpotCoordinates = Arrays.copyOf( currentSpotCoordinates, currentSpotCoordinates.length );
		double[] branchStartCoordinates = Arrays.copyOf( currentSpotCoordinates, currentSpotCoordinates.length );

		while ( spotIterator.hasNext() )
		{
			spot = spotIterator.next();
			spot.localize( currentSpotCoordinates );
			accumulatedLifeCycleDistance += LinAlgHelpers.distance( currentSpotCoordinates, previousSpotCoordinates );
			previousSpotCoordinates = Arrays.copyOf( currentSpotCoordinates, currentSpotCoordinates.length );
		}
		double directLifeCycleDistance = LinAlgHelpers.distance( currentSpotCoordinates, branchStartCoordinates );
		return accumulatedLifeCycleDistance / directLifeCycleDistance;
	}
}
