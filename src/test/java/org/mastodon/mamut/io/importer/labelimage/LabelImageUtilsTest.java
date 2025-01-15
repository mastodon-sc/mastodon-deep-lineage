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
package org.mastodon.mamut.io.importer.labelimage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectCreator;
import org.mastodon.mamut.io.importer.labelimage.util.CircleRenderer;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.io.importer.labelimage.util.LineRenderer;
import org.mastodon.mamut.io.importer.labelimage.util.SphereRenderer;
import org.mastodon.mamut.io.importer.labelimage.util.SpotRenderer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.VoxelDimensions;

class LabelImageUtilsTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private Model model;

	private AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

	private IntFunction< AffineTransform3D > simpleTransformProvider;

	@BeforeEach
	void setUp()
	{
		model = new Model();
		TimePoints timePoints = new TimePoints( Collections.singletonList( new TimePoint( 0 ) ) );
		VoxelDimensions voxelDimensions = new FinalVoxelDimensions( "um", 1, 1, 1 );
		Map< Integer, ? extends BasicViewSetup > setups =
				Collections.singletonMap( 0, new BasicViewSetup( 0, "setup 0", new FinalDimensions( 10, 10, 10 ), voxelDimensions ) );
		sequenceDescription = new SequenceDescriptionMinimal( timePoints, setups, null, null );
		simpleTransformProvider = frameId -> new AffineTransform3D();
	}

	@Test
	void testCreateSpotFromLabelImageEmpty()
	{
		RandomAccessibleIntervalSource< FloatType > img = new RandomAccessibleIntervalSource<>( createImageCubeCorners( 0 ),
				new FloatType(), new AffineTransform3D(), "Segmentation" );

		IntFunction< RandomAccessibleInterval< RealType< ? > > > imgProvider = frameId -> Cast.unchecked( img.getSource( frameId, 0 ) );
		LabelImageUtils.createSpotsFromLabelImage( imgProvider, simpleTransformProvider, model, 1, false, sequenceDescription, null );
		assertEquals( 0, model.getGraph().vertices().size() );
	}

	@Test
	void testCreateSpotFromNonLabelImage()
	{
		AbstractSource< FloatType > img = createNonLabelImage();

		IntFunction< RandomAccessibleInterval< RealType< ? > > > imgProvider = frameId -> Cast.unchecked( img.getSource( frameId, 0 ) );
		LabelImageUtils.createSpotsFromLabelImage( imgProvider, simpleTransformProvider, model, 1, false, sequenceDescription, null );
		assertEquals( 15_625, model.getGraph().vertices().size() );
	}

	@Test
	void testImportSpotSphere()
	{
		try (Context context = new Context())
		{
			Img< FloatType > img = ArrayImgs.floats( 12, 12, 12 );
			SphereRenderer.renderSphere( new int[] { 5, 5, 5 }, 5, 1, img );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, projectModel.getSharedBdvData().getSources().get( 0 ).getSpimSource(),
					1, false );

			Iterator< Spot > iter = model.getGraph().vertices().iterator();
			Spot spot = iter.next();
			Triple< Double, Double, Double > semiAxes = getSemiAxesOfSpot( spot );

			assertNotNull( spot );
			assertEquals( 0, spot.getTimepoint() );
			assertArrayEquals( new double[] { 5, 5, 5 }, spot.positionAsDoubleArray(), 0.01 );
			assertEquals( String.valueOf( 1 ), spot.getLabel() );
			assertEquals( 5, semiAxes.getLeft(), 0.05d );
			assertEquals( 5, semiAxes.getMiddle(), 0.05d );
			assertEquals( 5, semiAxes.getRight(), 0.05d );
		}
	}

	@Test
	void testImportSpotSinglePixel()
	{
		try (Context context = new Context())
		{
			Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
			SphereRenderer.renderSphere( new int[] { 5, 5, 5 }, 0.5, 1, img );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, projectModel.getSharedBdvData().getSources().get( 0 ).getSpimSource(),
					1, false );

			Iterator< Spot > iter = model.getGraph().vertices().iterator();
			Spot spot = iter.next();
			Triple< Double, Double, Double > semiAxes = getSemiAxesOfSpot( spot );

			assertNotNull( spot );
			assertEquals( 0, spot.getTimepoint() );
			assertArrayEquals( new double[] { 5, 5, 5 }, spot.positionAsDoubleArray(), 0.01 );
			assertEquals( String.valueOf( 1 ), spot.getLabel() );
			assertEquals( 0.5, semiAxes.getLeft(), 0.1d );
			assertEquals( 0.5, semiAxes.getMiddle(), 0.1d );
			assertEquals( 0.5, semiAxes.getRight(), 0.1d );
		}
	}

	@Test
	void testImportSpotCircle()
	{
		try (Context context = new Context())
		{
			Img< FloatType > img = ArrayImgs.floats( 12, 12, 12 );
			CircleRenderer.renderCircle( new int[] { 5, 5, 5 }, 5, 1, img, CircleRenderer.Plane.XY );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, projectModel.getSharedBdvData().getSources().get( 0 ).getSpimSource(),
					1, false );

			Iterator< Spot > iter = model.getGraph().vertices().iterator();
			Spot spot = iter.next();
			Triple< Double, Double, Double > semiAxes = getSemiAxesOfSpot( spot );

			assertNotNull( spot );
			assertEquals( 0, spot.getTimepoint() );
			assertArrayEquals( new double[] { 5, 5, 5 }, spot.positionAsDoubleArray(), 0.01 );
			assertEquals( String.valueOf( 1 ), spot.getLabel() );
			assertEquals( 0.5, semiAxes.getLeft(), 0.05d );
			assertEquals( 5, semiAxes.getMiddle(), 1d );
			assertEquals( 5, semiAxes.getRight(), 1d );
		}
	}

	@Test
	void testImportSpotLine()
	{
		try (Context context = new Context())
		{
			Img< FloatType > img = ArrayImgs.floats( 12, 12, 12 );
			LineRenderer.renderLine( new int[] { 0, 5, 5 }, new int[] { 10, 5, 5 }, 1, img );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, projectModel.getSharedBdvData().getSources().get( 0 ).getSpimSource(),
					1, false );

			Iterator< Spot > iter = model.getGraph().vertices().iterator();
			Spot spot = iter.next();
			Triple< Double, Double, Double > semiAxes = getSemiAxesOfSpot( spot );

			assertNotNull( spot );
			assertEquals( 0, spot.getTimepoint() );
			assertArrayEquals( new double[] { 5, 5, 5 }, spot.positionAsDoubleArray(), 0.01 );
			assertEquals( String.valueOf( 1 ), spot.getLabel() );
			assertEquals( 0.5, semiAxes.getLeft(), 0.01d );
			assertEquals( 0.5, semiAxes.getMiddle(), 0.01d );
			assertEquals( 7.5, semiAxes.getRight(), 1d );
		}
	}

	@Test
	void testImportSpotsFrom3DImgPlus()
	{
		try (Context context = new Context())
		{
			double[] givenCenter = { 18, 21, 22 };
			double[][] givenCovariance = {
					{ 33, 14, 0 },
					{ 14, 32, 0 },
					{ 0, 0, 95 }
			};
			Spot spot = model.getGraph().addVertex().init( 0, givenCenter, givenCovariance );
			int pixelValue = 1;
			long[] dimensions = { 40, 40, 40, 1 };
			Img< FloatType > image = createImageFromSpot( spot, pixelValue, dimensions );
			ImgPlus< FloatType > imgPlus = createImgPlus3DAndT( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			LabelImageUtils.importSpotsFromImgPlus( projectModel, 0, imgPlus, 1, false );

			Iterator< Spot > iterator = model.getGraph().vertices().iterator();
			iterator.next();
			Spot createdSpot = iterator.next();

			createSpotEquals( createdSpot, givenCenter, givenCovariance, pixelValue );
		}
	}

	@Test
	void testImportSpotsFrom2DImgPlus() throws SpimDataException
	{
		try (Context context = new Context())
		{
			double[] givenCenter = { 18, 21, 0 };
			double[][] givenCovariance = {
					{ 33, 14, 0 },
					{ 14, 32, 0 },
					{ 0, 0, 1 }
			};
			Spot spot = model.getGraph().addVertex().init( 0, givenCenter, givenCovariance );
			int pixelValue = 1;
			long[] dimensions3Dt = { 40, 40, 1, 1 };
			Img< FloatType > img3Dt = createImageFromSpot( spot, pixelValue, dimensions3Dt );
			long[] dimensions2Dt = { 40, 40, 1 };
			Img< FloatType > img2Dt = ArrayImgs.floats( dimensions2Dt );
			LoopBuilder.setImages( Views.hyperSlice( img3Dt, 2, 0 ), img2Dt ).forEachPixel( ( p, q ) -> q.set( p ) );
			ImgPlus< FloatType > imgPlus = createImgPlus2DAndT( img2Dt, new FinalVoxelDimensions( "um", 1, 1 ) );
			ImagePlus imagePlus = ImageJFunctions.wrap( img2Dt, "label image" );
			imagePlus.setDimensions( 1, 1, 1 );
			final ProjectModel appModel = ProjectCreator.createProjectFromImp( imagePlus, context );

			LabelImageUtils.importSpotsFromImgPlus( appModel, 0, imgPlus, 1, false );
			Iterator< Spot > iterator = appModel.getModel().getGraph().vertices().iterator();
			Spot createdSpot = iterator.next();

			createSpotEquals( createdSpot, givenCenter, givenCovariance, pixelValue );
		}
	}

	private static void createSpotEquals( final Spot createdSpot, final double[] center, final double[][] givenCovariance,
			final int pixelValue )
	{
		double[] mean = createdSpot.positionAsDoubleArray();
		double[][] computedCovariance = new double[ 3 ][ 3 ];
		createdSpot.getCovariance( computedCovariance );

		logger.debug( "Given center: {}", Arrays.toString( center ) );
		logger.debug( "Computed mean: {}", Arrays.toString( mean ) );
		logger.debug( "Given covariance: {}", Arrays.deepToString( givenCovariance ) );
		logger.debug( "Computed covariance: {}", Arrays.deepToString( computedCovariance ) );

		assertArrayEquals( center, mean, 0.01d );
		assertArrayEquals( givenCovariance[ 0 ], computedCovariance[ 0 ], 20d );
		assertArrayEquals( givenCovariance[ 1 ], computedCovariance[ 1 ], 20d );
		assertArrayEquals( givenCovariance[ 2 ], computedCovariance[ 2 ], 20d );
		assertEquals( String.valueOf( pixelValue ), createdSpot.getLabel() );
	}

	@Test
	void testImportSpotsFromImgPlusAndLinkSameLabels()
	{
		try (Context context = new Context())
		{
			Img< FloatType > twoFramesImage = DemoUtils.generateExampleTStack();
			ImgPlus< FloatType > imgPlus = createImgPlus3DAndT( twoFramesImage, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( twoFramesImage, model, context );
			LabelImageUtils.importSpotsFromImgPlus( projectModel, 0, imgPlus, 1, true );

			assertEquals( 2, model.getGraph().vertices().size() );
			assertEquals( 1, model.getGraph().edges().size() );
			assertEquals( 1, model.getSpatioTemporalIndex().getSpatialIndex( 0 ).size() );
			assertEquals( 1, model.getSpatioTemporalIndex().getSpatialIndex( 1 ).size() );
		}
	}

	@Test
	void testImportSpotsFromImgPlusNonSequentialLabels()
	{
		try (Context context = new Context())
		{
			Img< FloatType > image = DemoUtils.generateNonSequentialLabelImage();
			ImgPlus< FloatType > imgPlus = createImgPlus3DAndT( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			LabelImageUtils.importSpotsFromImgPlus( projectModel, 0, imgPlus, 1, true );

			assertEquals( 2, model.getGraph().vertices().size() );
			assertEquals( 0, model.getGraph().edges().size() );
			assertEquals( 2, model.getSpatioTemporalIndex().getSpatialIndex( 0 ).size() );
		}
	}

	@Test
	void testDimensionsMatch()
	{
		try (final Context context = new Context())
		{
			Img< FloatType > image = ArrayImgs.floats( 10, 10, 10, 2 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			ImgPlus< FloatType > imgPlus = createImgPlus3DAndT( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
			assertTrue( LabelImageUtils.dimensionsMatch( projectModel.getSharedBdvData(), imgPlus ) );
		}
	}

	@Test
	void testGetImgPlusDimensions3DAndT()
	{
		int x = 100;
		int y = 100;
		int z = 100;
		int t = 2;
		Img< FloatType > image = ArrayImgs.floats( x, y, z, t );
		ImgPlus< FloatType > imgPlus = createImgPlus3DAndT( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
		long[] dimensions = LabelImageUtils.getImgPlusDimensions( imgPlus );
		assertArrayEquals( new long[] { x, y, z, t }, dimensions );
	}

	@Test
	void testGetImgPlusDimensions2DAndT()
	{
		int x = 100;
		int y = 100;
		int z = 1;
		int t = 2;
		Img< FloatType > image = ArrayImgs.floats( x, y, t );
		ImgPlus< FloatType > imgPlus = createImgPlus2DAndT( image, new FinalVoxelDimensions( "um", 1, 1 ) );
		long[] dimensions = LabelImageUtils.getImgPlusDimensions( imgPlus );
		assertArrayEquals( new long[] { x, y, z, t }, dimensions );
	}

	@Test
	void testGetSourceNames()
	{
		try (final Context context = new Context())
		{
			Img< FloatType > image = ArrayImgs.floats( 10, 10, 10, 2 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			List< String > sourceNames = LabelImageUtils.getSourceNames( projectModel.getSharedBdvData() );
			assertEquals( 1, sourceNames.size() );
			assertEquals( "image channel 1", sourceNames.get( 0 ) );
		}
	}

	@Test
	void testGetSourceIndex()
	{
		try (final Context context = new Context())
		{
			Img< FloatType > image = ArrayImgs.floats( 10, 10, 10, 2 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			SharedBigDataViewerData sharedBdvData = projectModel.getSharedBdvData();
			int sourceIndex0 = LabelImageUtils.getSourceIndex( "image channel 1", sharedBdvData );
			assertEquals( 0, sourceIndex0 );
			assertThrows( IllegalArgumentException.class, () -> LabelImageUtils.getSourceIndex( "unknown", sharedBdvData ) );
		}
	}

	private ImgPlus< FloatType > createImgPlus3DAndT( final Img< FloatType > img, final VoxelDimensions voxelDimensions )
	{
		final CalibratedAxis[] axes = { new DefaultLinearAxis( Axes.X, voxelDimensions.dimension( 0 ) ),
				new DefaultLinearAxis( Axes.Y, voxelDimensions.dimension( 1 ) ),
				new DefaultLinearAxis( Axes.Z, voxelDimensions.dimension( 2 ) ), new DefaultLinearAxis( Axes.TIME ) };
		return new ImgPlus<>( img, "Result", axes );
	}

	private ImgPlus< FloatType > createImgPlus2DAndT( final Img< FloatType > img, final VoxelDimensions voxelDimensions )
	{
		final CalibratedAxis[] axes = { new DefaultLinearAxis( Axes.X, voxelDimensions.dimension( 0 ) ),
				new DefaultLinearAxis( Axes.Y, voxelDimensions.dimension( 1 ) ), new DefaultLinearAxis( Axes.TIME ) };
		return new ImgPlus<>( img, "Result", axes );
	}

	private static Img< FloatType > createImageCubeCorners( int pixelValue )
	{
		Img< FloatType > img = new ArrayImgFactory<>( new FloatType() ).create( 4, 4, 4 );
		RandomAccess< FloatType > ra = img.randomAccess();
		// 8 corners of a cube
		ra.setPositionAndGet( 1, 1, 1 ).set( pixelValue );
		ra.setPositionAndGet( 1, 3, 1 ).set( pixelValue );
		ra.setPositionAndGet( 3, 1, 1 ).set( pixelValue );
		ra.setPositionAndGet( 3, 3, 1 ).set( pixelValue );
		ra.setPositionAndGet( 1, 1, 3 ).set( pixelValue );
		ra.setPositionAndGet( 1, 3, 3 ).set( pixelValue );
		ra.setPositionAndGet( 3, 1, 3 ).set( pixelValue );
		ra.setPositionAndGet( 3, 3, 3 ).set( pixelValue );

		return img;
	}

	private static AbstractSource< FloatType > createNonLabelImage()
	{
		Img< FloatType > img = new ArrayImgFactory<>( new FloatType() ).create( 25, 25, 25 );
		AtomicInteger value = new AtomicInteger( 0 );
		LoopBuilder.setImages( img ).forEachPixel( floatType -> floatType.set( value.incrementAndGet() ) );

		return new RandomAccessibleIntervalSource<>( img, new FloatType(), new AffineTransform3D(),
				"Segmentation" );
	}

	private static Img< FloatType > createImageFromSpot( final Spot spot, int pixelValue, final long[] dimensions )
	{
		Img< FloatType > image = ArrayImgs.floats( dimensions );
		IntervalView< FloatType > frame = Views.hyperSlice( image, 3, 0 );
		AbstractSource< FloatType > source =
				new RandomAccessibleIntervalSource<>( frame, new FloatType(), new AffineTransform3D(), "Ellipsoids" );
		SpotRenderer.renderSpot( spot, pixelValue, source );
		return image;
	}

	private static Triple< Double, Double, Double > getSemiAxesOfSpot( final Spot spot )
	{
		double[][] covarianceMatrix = new double[ 3 ][ 3 ];
		spot.getCovariance( covarianceMatrix );
		final JamaEigenvalueDecomposition eigenvalueDecomposition = new JamaEigenvalueDecomposition( 3 );
		eigenvalueDecomposition.decomposeSymmetric( covarianceMatrix );
		final double[] eigenValues = eigenvalueDecomposition.getRealEigenvalues();
		double semiAxisA = Math.sqrt( eigenValues[ 0 ] );
		double semiAxisB = Math.sqrt( eigenValues[ 1 ] );
		double semiAxisC = Math.sqrt( eigenValues[ 2 ] );
		return Triple.of( semiAxisA, semiAxisB, semiAxisC );
	}
}

