package org.mastodon.mamut.feature.branch;

import java.util.Arrays;
import java.util.Iterator;

import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.DepthFirstIterator;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.util.GeometryUtil;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
		DepthFirstIterator<BranchSpot, BranchLink > depthFirstIterator = new DepthFirstIterator<>( branchGraph );
		for (BranchSpot root : RootFinder.getRoots( branchGraph ))
		{
			int numDimensions = root.numDimensions();
			final double[] currentSpotCoordinates = new double[ numDimensions ];
			double[] previousSpotCoordinates = new double[ numDimensions ];

			depthFirstIterator.reset( root );
			boolean isRoot = true;
			while ( depthFirstIterator.hasNext() )
			{
				BranchSpot currentBranchSpot = depthFirstIterator.next();
				double[] branchStartCoordinates = new double[3];
				boolean isBranchStartCoordinatesSet = false;
				boolean isPreviousCoordinatesSet = false;
				Spot currentSpot;
				double  totalDistanceDuringCellLifeCycle = 0d;
				final Iterator< Spot > it = branchGraph.vertexBranchIterator( currentBranchSpot );
				while ( it.hasNext() )
				{
					currentSpot = it.next();
					currentSpot.localize( currentSpotCoordinates );
					if (!isBranchStartCoordinatesSet)
					{
						if (isRoot)
						{
							branchStartCoordinates = Arrays.copyOf( currentSpotCoordinates, currentSpotCoordinates.length );	
						}
						else
						{
							for ( int d = 0; d < numDimensions; d++ )
							{
								branchStartCoordinates[ d ] = currentBranchSpot.incomingEdges().iterator().next().getSource().getDoublePosition( d );
							}
						}
						isBranchStartCoordinatesSet = true;
					}
					if (isPreviousCoordinatesSet)
						totalDistanceDuringCellLifeCycle += GeometryUtil.getEuclideanDistance( currentSpotCoordinates, previousSpotCoordinates );
					else
						totalDistanceDuringCellLifeCycle += GeometryUtil.getEuclideanDistance( currentSpotCoordinates, branchStartCoordinates );
					previousSpotCoordinates = Arrays.copyOf( currentSpotCoordinates, currentSpotCoordinates.length );
					isPreviousCoordinatesSet = true;
				}
				double directDistanceDuringCellLifeCycle = GeometryUtil.getEuclideanDistance( branchStartCoordinates, currentSpotCoordinates );
				double sinuosity = totalDistanceDuringCellLifeCycle / directDistanceDuringCellLifeCycle;
				output.map.set( currentBranchSpot, sinuosity );
				isRoot = false;
			}
		}
	}
}
