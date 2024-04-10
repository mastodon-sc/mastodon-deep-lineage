/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.io.exporter.labelimage;

import bdv.util.AbstractSource;
import bdv.util.RandomAccessibleIntervalSource;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import mpicbg.spim.data.sequence.DefaultVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Cast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.io.exporter.labelimage.config.LabelOptions;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExportLabelImageControllerTest
{
	private Model model;

	private Spot spot;

	private BranchSpot branchSpot;

	private int timepoint;

	private final long[] center = new long[] { 50, 50, 50 };

	@BeforeEach
	void setUp()
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
	void testSaveLabelImageToFile() throws IOException
	{
		try (final Context context = new Context())
		{
			AbstractSource< IntType > source = createRandomSource();
			TimePoint timePoint = new TimePoint( timepoint );
			List< TimePoint > timePoints = Collections.singletonList( timePoint );
			VoxelDimensions voxelDimensions = new DefaultVoxelDimensions( 3 );
			voxelDimensions.dimensions( new double[] { 1, 1, 1 } );
			ExportLabelImageController exportLabelImageController =
					new ExportLabelImageController( model, timePoints, Cast.unchecked( source ), context, voxelDimensions );
			File outputSpot = getTempFile( "resultSpot" );
			File outputBranchSpot = getTempFile( "resultBranchSpot" );
			File outputTrack = getTempFile( "resultTrack" );
			exportLabelImageController.saveLabelImageToFile( LabelOptions.SPOT_ID, outputSpot, false, 1 );
			exportLabelImageController.saveLabelImageToFile( LabelOptions.BRANCH_SPOT_ID, outputBranchSpot, false, 1 );
			exportLabelImageController.saveLabelImageToFile( LabelOptions.TRACK_ID, outputTrack, false, 1 );

			ImgOpener imgOpener = new ImgOpener( context );
			SCIFIOImgPlus< IntType > imgSpot = getIntTypeSCIFIOImgPlus( imgOpener, outputSpot );
			SCIFIOImgPlus< IntType > imgBranchSpot = getIntTypeSCIFIOImgPlus( imgOpener, outputBranchSpot );
			SCIFIOImgPlus< IntType > imgTrack = getIntTypeSCIFIOImgPlus( imgOpener, outputTrack );

			// check that the spot id / branchSpot id / track id is used as value in the center of the spot
			Assertions.assertNotNull( imgSpot );
			Assertions.assertEquals( 3, imgSpot.dimensionsAsLongArray().length );
			Assertions.assertEquals( 100, imgSpot.dimension( 0 ) );
			Assertions.assertEquals( 100, imgSpot.dimension( 1 ) );
			Assertions.assertEquals( 100, imgSpot.dimension( 2 ) );
			Assertions.assertEquals(
					spot.getInternalPoolIndex() + ExportLabelImageController.LABEL_ID_OFFSET, imgSpot.getAt( center ).get() );
			Assertions.assertEquals(
					branchSpot.getInternalPoolIndex() + ExportLabelImageController.LABEL_ID_OFFSET, imgBranchSpot.getAt( center ).get() );
			Assertions.assertEquals( ExportLabelImageController.LABEL_ID_OFFSET, imgTrack.getAt( center ).get() );
			// check that there is no value set outside the ellipsoid of the spot
			long[] corner = new long[] { 0, 0, 0 };
			Assertions.assertEquals( 0, imgSpot.getAt( corner ).get() );
			Assertions.assertEquals( 0, imgBranchSpot.getAt( corner ).get() );
			Assertions.assertEquals( 0, imgTrack.getAt( corner ).get() );
		}
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
	void testExceptions() throws IOException
	{
		try (final Context context = new Context())
		{
			ExportLabelImageController controller =
					new ExportLabelImageController( model, Collections.emptyList(), null, context, null );
			File file = File.createTempFile( "foo", "foo" );
			file.deleteOnExit();
			assertThrows(
					IllegalArgumentException.class,
					() -> controller.saveLabelImageToFile( LabelOptions.SPOT_ID, null, false, 1 )
			);
			assertThrows( IllegalArgumentException.class, () -> controller.saveLabelImageToFile( null, file, false, 1 ) );
		}
	}

	private static AbstractSource< IntType > createRandomSource()
	{
		Img< IntType > randomImg = RandomImgs.seed( 0 ).nextImage( new IntType() {}, 100, 100, 100 );
		return new RandomAccessibleIntervalSource<>( randomImg, new IntType(), new AffineTransform3D(), "Label Image" );
	}
}
