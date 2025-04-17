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
package org.mastodon.mamut.io.importer.labelimage.util;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectSaver;
import org.mastodon.mamut.util.LabelImageUtils;
import org.mastodon.mamut.io.importer.labelimage.math.CovarianceMatrix;
import org.mastodon.mamut.io.importer.labelimage.math.MeansVector;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DemoUtils
{
	private DemoUtils()
	{
		// prevent from instantiation
	}

	public static ProjectModel wrapAsAppModel( final Img< FloatType > image, final Model model, final Context context )
	{
		final SharedBigDataViewerData sharedBigDataViewerData = asSharedBdvDataXyz( image );
		return ProjectModel.create( context, model, sharedBigDataViewerData, null );
	}

	public static ProjectModel wrapAsAppModel( final Img< FloatType > image, final Model model, final Context context, final File file )
			throws IOException
	{
		final SharedBigDataViewerData sharedBigDataViewerData = asSharedBdvDataXyz( image );
		MamutProject mamutProject = new MamutProject( file );
		File datasetXmlFile = File.createTempFile( "test", ".xml" );
		mamutProject.setDatasetXmlFile( datasetXmlFile );
		return ProjectModel.create( context, model, sharedBigDataViewerData, mamutProject );
	}

	public static File saveAppModelToTempFile( final Img< FloatType > image, final Model model ) throws IOException
	{
		File file = File.createTempFile( "test", ".mastodon" );
		try (Context context = new Context())
		{
			ProjectModel appModel1 = DemoUtils.wrapAsAppModel( image, model, context, file );
			ProjectSaver.saveProject( file, appModel1 );
		}
		return file;
	}

	public static SharedBigDataViewerData asSharedBdvDataXyz( final Img< FloatType > image1 )
	{
		final ImagePlus image =
				ImgToVirtualStack.wrap( new ImgPlus<>( image1, "image", new AxisType[] { Axes.X, Axes.Y, Axes.Z, Axes.TIME } ) );
		return Objects.requireNonNull( SharedBigDataViewerData.fromImagePlus( image ) );
	}

	public static void showBdvWindow( @Nonnull final ProjectModel appModel )
	{
		appModel.getWindowManager().createView( MamutViewBdv.class );
	}

	/**
	 * Returns an example image with a single ellipsoid.
	 *
	 * @param center center of the ellipsoid
	 * @param cov covariance matrix of the ellipsoid
	 * @param dimensions dimensions of the image
	 * @param background value of the background
	 * @param pixelValue value of the ellipsoid
	 */
	public static Img< FloatType > generateExampleImage(
			final double[] center, final double[][] cov, final long[] dimensions, final int background, final int pixelValue
	)
	{
		Img< FloatType > image = ArrayImgs.floats( dimensions );
		MultiVariateNormalDistributionRenderer.renderMultivariateNormalDistribution( center, cov, image );
		LoopBuilder.setImages( image ).forEachPixel( pixel -> {
			if ( pixel.get() > 500 )
				pixel.set( pixelValue );
			else
				pixel.set( background );
		} );
		return image;
	}

	/**
	 * Returns an example t stack that contains two identical frames where both contain a single ellipsoid and black background.
	 */
	public static Img< FloatType > generateExampleTStack()
	{
		double[] center = { 40, 80, 50 };
		double[][] givenCovariance = {
				{ 400, 20, -10 },
				{ 20, 200, 30 },
				{ -10, 30, 100 }
		};
		long[] dimensions = { 100, 100, 100 };
		int background = 0;
		int pixelValue = 1;
		Img< FloatType > frame = generateExampleImage( center, givenCovariance, dimensions, background, pixelValue );
		List< Img< FloatType > > twoIdenticalFrames = Arrays.asList( frame, frame );
		return ImgView.wrap( Views.stack( twoIdenticalFrames ) );
	}

	/**
	 * Returns an image with two ellipsoids having the values 1 and 3. The background is black, i.e. 0.
	 */
	public static Img< FloatType > generateNonSequentialLabelImage()
	{
		double[] center1 = { 10, 10, 10 };
		double[][] givenCovariance1 = {
				{ 400, 20, -10 },
				{ 20, 200, 30 },
				{ -10, 30, 100 }
		};
		double[] center3 = { 90, 90, 90 };
		double[][] givenCovariance3 = {
				{ 400, 20, -10 },
				{ 20, 200, 30 },
				{ -10, 30, 100 }
		};
		long[] dimensions = { 100, 100, 100 };
		int background = 0;
		int pixelValue1 = 1;
		int pixelValue3 = 3;
		Img< FloatType > img1 = generateExampleImage( center1, givenCovariance1, dimensions, background, pixelValue1 );
		Img< FloatType > img3 = generateExampleImage( center3, givenCovariance3, dimensions, background, pixelValue3 );

		// use imglib2 to xor the two images
		Img< FloatType > xorImage = ArrayImgs.floats( dimensions );
		LoopBuilder.setImages( img1, img3, xorImage ).forEachPixel( ( p1, p2, x ) -> {
			if ( p1.get() == 0 && p2.get() == 0 )
				x.set( 0 );
			else if ( p1.get() == 0 && p2.get() != 0 )
				x.set( p2.get() );
			else if ( p1.get() != 0 && p2.get() == 0 )
				x.set( p1.get() );
			else
				x.set( 4 ); // NB: there is no overlap
		} );
		List< Img< FloatType > > singleFrameImage = Collections.singletonList( xorImage );
		return ImgView.wrap( Views.stack( singleFrameImage ) );
	}

	/**
	 * Computes the covariance matrix of the pixels whose value equals the given {@code pixelValue}.
	 * Uses an online algorithm to compute the covariance matrix, cf. <a href=https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online>Online algorithm for covariance</a>
	 *
	 * @param image the image
	 * @param pixelValue the pixel value
	 */
	public static Pair< double[], double[][] > computeMeanCovarianceOnline( final Img< RealType< ? > > image, final int pixelValue,
			double sigma )
	{
		Cursor< RealType< ? > > cursor = image.cursor();
		int[] position = new int[ 3 ];
		cursor.reset();
		MeansVector mean = new MeansVector( 3 );
		CovarianceMatrix cov = new CovarianceMatrix( 3 );
		while ( cursor.hasNext() )
			if ( cursor.next().getRealDouble() == pixelValue )
			{
				cursor.localize( position );
				mean.addValues( position );
				cov.addValues( position );
			}
		double[] means = mean.get();
		double[][] covariances = cov.get();
		LabelImageUtils.scale( covariances, sigma );
		return new ValuePair<>( means, covariances );
	}
}
