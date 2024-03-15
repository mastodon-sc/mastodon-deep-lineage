/*-
 * #%L
 * mastodon-ellipsoid-fitting
 * %%
 * Copyright (C) 2015 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.importer.labelimage.demo;

import ij.ImagePlus;
import mpicbg.spim.data.sequence.DefaultVoxelDimensions;
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
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.mastodon.mamut.io.importer.labelimage.math.CovarianceMatrix;
import org.mastodon.mamut.io.importer.labelimage.math.MeansVector;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import javax.annotation.Nonnull;
import java.util.Arrays;
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
	 * Returns an example image with a single ellipsoid and black background.
	 */
	public static Img< FloatType > generateExampleImage()
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
		LabelImageUtils.scale( covariances, sigma, new DefaultVoxelDimensions( 3 ) );
		return new ValuePair<>( means, covariances );
	}
}
