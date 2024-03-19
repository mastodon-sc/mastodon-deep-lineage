package org.mastodon.mamut.io.importer.labelimage;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.EllipsoidIterable;
import org.mastodon.mamut.io.importer.labelimage.util.DemoUtils;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LabelImageUtilsTest
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private Model model;

	private AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

	@Before
	public void setUp()
	{
		model = new Model();
		TimePoints timePoints = new TimePoints( Collections.singletonList( new TimePoint( 0 ) ) );
		VoxelDimensions voxelDimensions = new FinalVoxelDimensions( "um", 1, 1, 1 );
		Map< Integer, ? extends BasicViewSetup > setups =
				Collections.singletonMap( 0, new BasicViewSetup( 0, "setup 0", new FinalDimensions( 10, 10, 10 ), voxelDimensions ) );
		sequenceDescription = new SequenceDescriptionMinimal( timePoints, setups, null, null );
	}

	@Test
	public void testScaleExceptions()
	{
		VoxelDimensions voxelDimensions = new FinalVoxelDimensions( "um", 1, 1, 1 );
		assertThrows( IllegalArgumentException.class, () -> LabelImageUtils.scale( new double[ 2 ][ 2 ], 1, voxelDimensions ) );
		assertThrows( IllegalArgumentException.class, () -> LabelImageUtils.scale( new double[ 3 ][ 2 ], 1, voxelDimensions ) );
	}

	@Test
	public void testCreateSpotFromLabelImageEmpty()
	{
		RandomAccessibleIntervalSource< FloatType > img =
				new RandomAccessibleIntervalSource<>( createImageCubeCorners( 0 ), new FloatType(), new AffineTransform3D(),
						"Segmentation" );

		IntFunction< RandomAccessibleInterval< RealType< ? > > > imgProvider = frameId -> Cast.unchecked( img.getSource( frameId, 0 ) );
		LabelImageUtils.createSpotsFromLabelImage( imgProvider, model, 1, sequenceDescription, null );
		assertEquals( 0, model.getGraph().vertices().size() );
	}

	@Test
	public void testCreateSpotFromNonLabelImage()
	{
		AbstractSource< FloatType > img = createNonLabelImage();

		IntFunction< RandomAccessibleInterval< RealType< ? > > > imgProvider = frameId -> Cast.unchecked( img.getSource( frameId, 0 ) );
		LabelImageUtils.createSpotsFromLabelImage( imgProvider, model, 1, sequenceDescription, null );
		assertEquals( 0, model.getGraph().vertices().size() );
	}

	@Test
	public void testCreateSpotFromWrongVoxelDimensions()
	{

		RandomAccessibleIntervalSource< FloatType > img =
				new RandomAccessibleIntervalSource<>( createImageCubeCorners( 1 ), new FloatType(), new AffineTransform3D(),
						"Segmentation" );

		VoxelDimensions wrongDimensions = new FinalVoxelDimensions( "um", 1, 1 );
		TimePoints timePoints = new TimePoints( Collections.singletonList( new TimePoint( 0 ) ) );
		Map< Integer, ? extends BasicViewSetup > setups =
				Collections.singletonMap( 0, new BasicViewSetup( 0, "setup 0", new FinalDimensions( 10, 10, 10 ), wrongDimensions ) );
		AbstractSequenceDescription< ?, ?, ? > faultySequenceDescription = new SequenceDescriptionMinimal( timePoints, setups, null, null );
		IntFunction< RandomAccessibleInterval< RealType< ? > > > imgProvider = frameId -> Cast.unchecked( img.getSource( frameId, 0 ) );
		assertThrows( IllegalArgumentException.class,
				() -> LabelImageUtils.createSpotsFromLabelImage( imgProvider, model, 1, faultySequenceDescription, null ) );

	}

	@Test
	public void testImportSpotsFromBdvChannel()
	{
		LegacyInjector.preinit();
		try (Context context = new Context())
		{
			int pixelValue = 1;
			Img< FloatType > img = createImageCubeCorners( pixelValue );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( img, model, context );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, projectModel.getSharedBdvData().getSources().get( 0 ).getSpimSource(),
					1 );

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

			assertNotNull( spot );
			assertEquals( 0, spot.getTimepoint() );
			assertEquals( 2, spot.getDoublePosition( 0 ), 0.01 );
			assertEquals( 2, spot.getDoublePosition( 1 ), 0.01 );
			assertEquals( 2, spot.getDoublePosition( 2 ), 0.01 );
			assertEquals( 0, spot.getInternalPoolIndex() );
			assertEquals( String.valueOf( pixelValue ), spot.getLabel() );
			assertEquals( 2.2, axisA, 0.2d );
			assertEquals( 2.2, axisB, 0.2d );
			assertEquals( 2.2, axisC, 0.2d );
			assertEquals( 5d, spot.getBoundingSphereRadiusSquared(), 1d );
			assertFalse( iter.hasNext() );
		}
	}

	@Test
	public void testImportSpotsFromImgPlus()
	{
		LegacyInjector.preinit();
		try (Context context = new Context())
		{
			double[] center = { 18, 21, 22 };
			double[][] givenCovariance = {
					{ 33, 14, 0 },
					{ 14, 32, 0 },
					{ 0, 0, 95 }
			};
			Spot spot = model.getGraph().addVertex().init( 0, center, givenCovariance );
			int pixelValue = 1;
			Img< FloatType > image = createImageFromSpot( spot, pixelValue );
			ImgPlus< FloatType > imgPlus = createImgPlus( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			LabelImageUtils.importSpotsFromImgPlus( projectModel, imgPlus, 1 );

			Iterator< Spot > iterator = model.getGraph().vertices().iterator();
			iterator.next();
			Spot createdSpot = iterator.next();
			double[] mean = createdSpot.positionAsDoubleArray();
			double[][] computedCovariance = new double[ 3 ][ 3 ];
			createdSpot.getCovariance( computedCovariance );

			logger.debug( "Given center: {}", Arrays.toString( center ) );
			logger.debug( "Computed mean: {}", Arrays.toString( mean ) );
			logger.debug( "Given covariance: {}", Arrays.deepToString( givenCovariance ) );
			logger.debug( "Computed covariance: {}", Arrays.deepToString( computedCovariance ) );

			assertArrayEquals( center, mean, 0.01d );
			assertArrayEquals( givenCovariance[ 0 ], computedCovariance[ 0 ], 10d );
			assertArrayEquals( givenCovariance[ 1 ], computedCovariance[ 1 ], 10d );
			assertArrayEquals( givenCovariance[ 2 ], computedCovariance[ 2 ], 10d );
			assertEquals( String.valueOf( pixelValue ), createdSpot.getLabel() );
		}
	}

	@Test
	public void testDimensionsMatch()
	{
		LegacyInjector.preinit();
		try (final Context context = new Context())
		{
			Img< FloatType > image = ArrayImgs.floats( 10, 10, 10, 2 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			ImgPlus< FloatType > imgPlus = createImgPlus( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
			assertTrue( LabelImageUtils.dimensionsMatch( projectModel.getSharedBdvData(), imgPlus ) );
		}
	}

	@Test
	public void testGetImgPlusDimensions()
	{
		Img< FloatType > image = ArrayImgs.floats( 100, 100, 100, 2 );
		ImgPlus< FloatType > imgPlus = createImgPlus( image, new FinalVoxelDimensions( "um", 1, 1, 1 ) );
		long[] dimensions = LabelImageUtils.getImgPlusDimensions( imgPlus );
		assertArrayEquals( new long[] { 100, 100, 100, 2 }, dimensions );
	}

	@Test
	public void testGetSourceNames()
	{
		LegacyInjector.preinit();
		try (final Context context = new Context())
		{
			Img< FloatType > image = ArrayImgs.floats( 100, 100, 100, 2 );
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );
			List< String > sourceNames = LabelImageUtils.getSourceNames( projectModel.getSharedBdvData() );
			assertEquals( 1, sourceNames.size() );
			assertEquals( "image channel 1", sourceNames.get( 0 ) );
		}
	}

	private ImgPlus< FloatType > createImgPlus( final Img< FloatType > img, final VoxelDimensions voxelDimensions )
	{
		final CalibratedAxis[] axes = { new DefaultLinearAxis( Axes.X, voxelDimensions.dimension( 0 ) ),
				new DefaultLinearAxis( Axes.Y, voxelDimensions.dimension( 1 ) ),
				new DefaultLinearAxis( Axes.Z, voxelDimensions.dimension( 2 ) ), new DefaultLinearAxis( Axes.TIME ) };
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

	private static Img< FloatType > createImageFromSpot( final Spot spot, int pixelValue )
	{
		long[] dimensions = { 40, 40, 40, 1 };
		Img< FloatType > image = ArrayImgs.floats( dimensions );
		AffineTransform3D transform = new AffineTransform3D();
		AbstractSource< FloatType > frame =
				new RandomAccessibleIntervalSource<>( Views.hyperSlice( image, 3, 0 ), new FloatType(), transform, "Ellipsoids" );

		final EllipsoidIterable< FloatType > ellipsoidIterable = new EllipsoidIterable<>( frame );
		ellipsoidIterable.reset( spot );
		ellipsoidIterable.forEach( pixel -> pixel.set( pixelValue ) );
		return image;
	}
}

