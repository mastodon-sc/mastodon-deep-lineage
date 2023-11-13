package org.mastodon.mamut.io.importer.labelimage;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.Context;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ImportSpotFromLabelsControllerTest
{
	private Model model;

	private int timepoint;

	@Before
	public void setUp()
	{
		model = new Model();
		ModelBranchGraph modelBranchGraph = model.getBranchGraph();
		modelBranchGraph.graphRebuilt();
		timepoint = 0;
	}

	@Test
	public void testGetEllipsoidFromImage()
	{
		AbstractSource< IntType > img = createImage();

		Context context = new Context();
		TimePoint timePoint = new TimePoint( timepoint );
		List< TimePoint > timePoints = Collections.singletonList( timePoint );
		VoxelDimensions voxelDimensions = new FinalVoxelDimensions( "um", 1, 1, 1 );
		ImportSpotFromLabelsController controller =
				new ImportSpotFromLabelsController( model, timePoints, img, context, voxelDimensions, 1 );

		controller.createSpotsFromLabels();

		Iterator< Spot > iter = model.getGraph().vertices().iterator();
		Spot spot = iter.next();
		double[][] covarianceMatrix = new double[ 3 ][ 3 ];
		spot.getCovariance( covarianceMatrix );
		final JamaEigenvalueDecomposition eigenvalueDecomposition = new JamaEigenvalueDecomposition( 3 );
		eigenvalueDecomposition.decomposeSymmetric( covarianceMatrix );
		final double[] eigenValues = eigenvalueDecomposition.getRealEigenvalues();
		double axisA = Math.sqrt( eigenValues[ 0 ] );
		double axisB = Math.sqrt( eigenValues[ 1 ] );
		double axisC = Math.sqrt( eigenValues[ 2 ] );
		double radiusSquared = spot.getBoundingSphereRadiusSquared();

		assertNotNull( spot );
		assertEquals( 0, spot.getTimepoint() );
		assertEquals( 2, spot.getDoublePosition( 0 ), 0.01 );
		assertEquals( 2, spot.getDoublePosition( 1 ), 0.01 );
		assertEquals( 2, spot.getDoublePosition( 2 ), 0.01 );
		assertEquals( 0, spot.getInternalPoolIndex() );
		assertEquals( "0", spot.getLabel() );
		assertEquals( 1, axisA, 0.01d );
		assertEquals( 1, axisB, 0.01d );
		assertEquals( 1, axisC, 0.01d );
		assertEquals( 1, radiusSquared, 0.01d );
		assertFalse( iter.hasNext() );
	}

	private static AbstractSource< IntType > createImage()
	{
		Img< IntType > img = new ArrayImgFactory<>( new IntType() ).create( 4, 4, 4 );
		RandomAccess< IntType > ra = img.randomAccess();
		int label = 1;
		// 8 corners of a cube
		ra.setPositionAndGet( 1, 1, 1 ).set( label );
		ra.setPositionAndGet( 1, 3, 1 ).set( label );
		ra.setPositionAndGet( 3, 1, 1 ).set( label );
		ra.setPositionAndGet( 3, 3, 1 ).set( label );
		ra.setPositionAndGet( 1, 1, 3 ).set( label );
		ra.setPositionAndGet( 1, 3, 3 ).set( label );
		ra.setPositionAndGet( 3, 1, 3 ).set( label );
		ra.setPositionAndGet( 3, 3, 3 ).set( label );

		return new RandomAccessibleIntervalSource<>( img, new IntType(), new AffineTransform3D(), "Segmentation" );
	}
}
