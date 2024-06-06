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
package org.mastodon.mamut.io.importer.labelimage.util;

import bdv.viewer.SourceAndConverter;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.importer.labelimage.LabelImageUtils;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

public class SmallLabelDemo
{
	public static void main( String[] args )
	{
		LegacyInjector.preinit();
		try (final Context context = new Context())
		{
			long[] dimensions = { 100, 100, 100 };
			Img< FloatType > image = ArrayImgs.floats( dimensions );
			Model model = new Model();
			ProjectModel projectModel = DemoUtils.wrapAsAppModel( image, model, context );

			int[] center1 = { 20, 80, 50 };
			double radius1 = 0.5d;

			int[] center2 = { 40, 80, 50 };
			double radius2 = 1d;

			int[] center3 = { 60, 80, 50 };
			double radius3 = 10d;

			SphereRenderer.renderSphere( center1, radius1, 1, image );
			SphereRenderer.renderSphere( center2, radius2, 2, image );
			SphereRenderer.renderSphere( center3, radius3, 3, image );

			int[] center4 = { 20, 50, 50 };
			double radius4 = 0.5d;

			int[] center5 = { 40, 50, 50 };
			double radius5 = 1d;

			int[] center6 = { 60, 50, 50 };
			double radius6 = 10d;

			CircleRenderer.renderCircle( center4, radius4, 4, image, CircleRenderer.Plane.XY );
			CircleRenderer.renderCircle( center5, radius5, 5, image, CircleRenderer.Plane.XY );
			CircleRenderer.renderCircle( center6, radius6, 6, image, CircleRenderer.Plane.XY );

			int[] start = { 80, 45, 50 };
			int[] end = { 80, 55, 50 };

			LineRenderer.renderLine( start, end, 7, image );
			SourceAndConverter< ? > sourceAndConverter = projectModel.getSharedBdvData().getSources().get( 0 );
			LabelImageUtils.importSpotsFromBdvChannel( projectModel, sourceAndConverter.getSpimSource(), 1d, false );
			DemoUtils.showBdvWindow( projectModel );
		}
	}
}
