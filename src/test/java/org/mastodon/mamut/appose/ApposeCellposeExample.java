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
package org.mastodon.mamut.appose;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;

import io.scif.img.ImgOpener;

public class ApposeCellposeExample
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{
		// Specify the path to the TIFF file
		String filePath = "target/test-classes/org/mastodon/mamut/appose/nuclei_3d.tif";
		ImgOpener imgOpener = new ImgOpener();
		Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		Img< FloatType > shmImg = ShmImg.copyOf( img );

		String script = "import numpy as np" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import logging" + "\n"
				+ "narr = image.ndarray()" + "\n"
				+ "print(\"Image shape: \", image.shape)" + "\n"
				+ "model = models.Cellpose(model_type=model_type, gpu=True)" + "\n"
				+ "\n"
				+ "logging.info(\"Starting Cellpose segmentation...\")" + "\n"
				+ "logging.info(\"Starting Cellpose segmentation...\")" + "\n"
				+ "\n"
				+ "segmentation, flows, styles, diams = model.eval(image, diameter=None, channels=[0, 0],do_3D=True, anisotropy=1.0, z_axis=0, normalize=True)"
				+ "\n"
				+ "logging.info(\"Segmentation complete.\")" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = segmentation" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";

		String envYmlPath = "target/test-classes/org/mastodon/mamut/appose/cellpose.yml";
		File envYmlFile = new File( envYmlPath );
		System.out.println( envYmlFile.getAbsoluteFile() );
		Environment env = Appose.file( envYmlFile, "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );

		try (Service python = env.python())
		{
			// Store our Img into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( shmImg ) );

			// Run the script!
			Service.Task task = python.task( script, inputs );
			task.waitFor();

			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
			{
				throw new RuntimeException( "Python script failed with error: " + task.error );
			}

			System.out.println( "Python script completed" );

			// Access the `label_image` output NDArray.
			NDArray labelImageArray = ( NDArray ) task.outputs.get( "label_image" );
			// Wrap the NDArray to an Img.

			Img< FloatType > labelImage = new ShmImg<>( labelImageArray );
			System.out.println( "label image size: " + labelImage.size() );
		}
	}
}
