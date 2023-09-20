package org.mastodon.mamut.segment;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import mpicbg.spim.data.sequence.TimePoint;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Cast;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.segment.config.LabelOptions;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class SegmentUsingEllipsoidsControllerTest
{
	private Model model;

	private Spot spot;

	private BranchSpot branchSpot;

	private int timepoint;

	private final long[] center = new long[] { 50, 50, 50 };

	@Before
	public void setUp()
	{
		model = new Model();
		ModelGraph modelGraph = model.getGraph();
		spot = modelGraph.addVertex();
		ModelBranchGraph modelBranchGraph = model.getBranchGraph();
		modelBranchGraph.graphRebuilt();
		branchSpot = modelBranchGraph.getBranchVertex( spot, modelBranchGraph.vertexRef() );
		timepoint = 0;
		spot.init( timepoint, Arrays.stream( center ).asDoubleStream().toArray(), 5d );
	}

	@Test
	public void testSaveEllipsoidSegmentationToFile() throws IOException
	{
		AbstractSource< IntType > source = createRandomSource();
		Context context = new Context();
		TimePoint timePoint = new TimePoint( timepoint );
		List< TimePoint > timePoints = Collections.singletonList( timePoint );
		SegmentUsingEllipsoidsController segmentUsingEllipsoidsController =
				new SegmentUsingEllipsoidsController( model, timePoints, Cast.unchecked( source ), context );
		File outputSpot = getTempFile( "resultSpot" );
		File outputBranchSpot = getTempFile( "resultBranchSpot" );
		File outputTrack = getTempFile( "resultTrack" );
		segmentUsingEllipsoidsController.saveEllipsoidSegmentationToFile( LabelOptions.SPOT_ID, outputSpot, true, false );
		segmentUsingEllipsoidsController.saveEllipsoidSegmentationToFile( LabelOptions.BRANCH_SPOT_ID, outputBranchSpot, true, false );
		segmentUsingEllipsoidsController.saveEllipsoidSegmentationToFile( LabelOptions.TRACK_ID, outputTrack, true, false );

		ImgOpener imgOpener = new ImgOpener( context );
		SCIFIOImgPlus< IntType > imgSpot = getIntTypeSCIFIOImgPlus( imgOpener, outputSpot );
		SCIFIOImgPlus< IntType > imgBranchSpot = getIntTypeSCIFIOImgPlus( imgOpener, outputBranchSpot );
		SCIFIOImgPlus< IntType > imgTrack = getIntTypeSCIFIOImgPlus( imgOpener, outputTrack );

		// check that the random content has been replaced by the spot id in the center of the spot
		assertEquals( spot.getInternalPoolIndex(), imgSpot.getAt( center ).get() );
		assertEquals( branchSpot.getInternalPoolIndex(), imgBranchSpot.getAt( center ).get() );
		assertEquals( 0, imgTrack.getAt( center ).get() );
		// check that the random content has NOT been replaced outside the ellipsoid of the spot
		long[] corner = new long[] { 0, 0, 0 };
		assertNotEquals( spot.getInternalPoolIndex(), imgSpot.getAt( corner ).get() );
		assertNotEquals( branchSpot.getInternalPoolIndex(), imgBranchSpot.getAt( corner ).get() );
		assertNotEquals( 0, imgTrack.getAt( corner ).get() );
	}

	private static SCIFIOImgPlus< IntType > getIntTypeSCIFIOImgPlus( ImgOpener imgOpener, File outputSpot )
	{
		List< SCIFIOImgPlus< ? > > imgsSpot = imgOpener.openImgs( outputSpot.getAbsolutePath() );
		SCIFIOImgPlus< ? > imgSpot = imgsSpot.get( 0 );
		return Cast.unchecked( imgSpot );
	}

	private static File getTempFile( final String prefix ) throws IOException
	{
		File outputSpot = File.createTempFile( prefix, ".tif" );
		outputSpot.deleteOnExit();
		return outputSpot;
	}

	@Test
	public void testExceptions() throws IOException
	{
		SegmentUsingEllipsoidsController controller =
				new SegmentUsingEllipsoidsController( model, Collections.emptyList(), null, new Context() );
		File file = File.createTempFile( "foo", "foo" );
		file.deleteOnExit();
		assertThrows(
				IllegalArgumentException.class,
				() -> controller.saveEllipsoidSegmentationToFile( LabelOptions.SPOT_ID, null, true, false )
		);
		assertThrows( IllegalArgumentException.class, () -> controller.saveEllipsoidSegmentationToFile( null, file, true, false ) );
		assertThrows( IllegalArgumentException.class, () -> controller.showEllipsoidSegmentationInBDV( null ) );
	}

	private static AbstractSource< IntType > createRandomSource()
	{
		Img< IntType > randomImg = RandomImgs.seed( 0 ).nextImage( new IntType() {}, 100, 100, 100 );
		return new RandomAccessibleIntervalSource<>( randomImg, new IntType(), new AffineTransform3D(), "Segmentation" );
	}

	@Test
	public void testShowEllipsoidSegmentationInBDV()
	{
		AbstractSource< IntType > source = createRandomSource();
		Context context = new Context();
		TimePoint timePoint = new TimePoint( timepoint );
		List< TimePoint > timePoints = Collections.singletonList( timePoint );
		SegmentUsingEllipsoidsController segmentUsingEllipsoidsController =
				new SegmentUsingEllipsoidsController( model, timePoints, Cast.unchecked( source ), context );
		segmentUsingEllipsoidsController.showEllipsoidSegmentationInBDV( LabelOptions.SPOT_ID );

		// check that the random content has been replaced by the spot id in the center of the spot
		assertEquals( spot.getInternalPoolIndex(), source.getSource( timepoint, 0 ).getAt( center ).get() );
		// check that the random content has NOT been replaced outside the ellipsoid of the spot
		long[] corner = new long[] { 0, 0, 0 };
		assertNotEquals( spot.getInternalPoolIndex(), source.getSource( timepoint, 0 ).getAt( corner ).get() );
	}

}
