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
package org.mastodon.mamut.detection;

import java.io.IOException;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.mastodon.mamut.detection.cellpose.Cellpose4;
import org.mastodon.mamut.detection.stardist.StarDist;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import io.scif.img.ImgOpener;

public class DetectionDemo
{
	public static void main( String[] args ) throws IOException
	{
		String filePath = "/home/pol_bia/stha735e/Documents/Mastodon/1135_n_stain_TO-PRO-3.tif";
		//String filePath =
		//		"D:\\DeepLineage\\Datasets\\StarDist Plant Nuclei 3D\\Training image dataset_Tiff Files\\1135\\1135_n_stain_TO-PRO-3.tif";
		ImgOpener imgOpener = new ImgOpener();
		Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		// Display the first image in a new BDV instance
		BdvStackSource< ? > bdvSource1 = BdvFunctions.show( img, "Original Image" );
		Environment environment = Appose.mamba().content( Cellpose4.ENV_FILE_CONTENT ).scheme( "environment.yml" ).logDebug().build();
		try (Service python = environment.python().init( Cellpose4.generateImportStatements() ))
		{
			Cellpose4 cellpose = new Cellpose4( python );
			long startTime = System.currentTimeMillis();
			Img< FloatType > cellposeSegmentation = cellpose.segmentImage( img );
			long endTime = System.currentTimeMillis();
			System.out.println( "Cellpose segmentation time: " + ( endTime - startTime ) + " ms" );
			// Add cellpose segmentation as a second channel in the same BDV instance
			if ( cellposeSegmentation != null )
				BdvFunctions.show( cellposeSegmentation, "Cellpose Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
		environment = Appose.mamba().content( StarDist.ENV_FILE_CONTENT ).scheme( "environment.yml" ).logDebug().build();
		try (Service python = environment.python().init( StarDist.generateImportStatements( StarDist.ModelType.DEMO, false ) ))
		{
			StarDist starDist = new StarDist( StarDist.ModelType.DEMO, python );
			long startTime = System.currentTimeMillis();
			Img< FloatType > starDistSegmentation = starDist.segmentImage( img );
			long endTime = System.currentTimeMillis();
			System.out.println( "StarDist segmentation time: " + ( endTime - startTime ) + " ms" );
			// Add star dist segmentation as a third channel in the same BDV instance
			if ( starDistSegmentation != null )
				BdvFunctions.show( starDistSegmentation, "StarDist Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
