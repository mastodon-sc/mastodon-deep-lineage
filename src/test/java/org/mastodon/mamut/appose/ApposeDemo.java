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

import java.util.HashMap;
import java.util.Map;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.apposed.appose.TaskException;
import org.apposed.appose.BuildException;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import io.scif.img.ImgOpener;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class ApposeDemo
{
	public static void main( String[] args ) throws BuildException, InterruptedException, TaskException
	{
		// Specify the path to the TIFF file
		String filePath = "/home/pol_bia/stha735e/Documents/Mastodon/1135_n_stain_TO-PRO-3.tif";
		String starDistModelPath = "/home/pol_bia/stha735e/StarDist";
		ImgOpener imgOpener = new ImgOpener();
		Img< FloatType > img = imgOpener.openImgs( filePath, new FloatType() ).get( 0 );
		//Img< FloatType > img = ArrayImgs.floats( 20, 20, 20 ); // empty image
		Img< FloatType > shmImg = ShmImg.copyOf( img );

		String script = "import numpy as np" + "\n"
				+ "import appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ "from stardist.models import StarDist3D" + "\n"
				+ "np.random.seed(6)" + "\n"
				+ "axes_normalize = (0, 1, 2)" + "\n"
				+ "print(\"Loading StarDist pretrained 3D model\")" + "\n"
				// sb.append( "model = StarDist3D.from_pretrained('3D_demo')" ).append( "\n" );
				+ "model = StarDist3D(None, name='stardist-plant-nuclei-3d', basedir=r\"" + starDistModelPath + "\")" + "\n"
				+ "narr = image.ndarray()" + "\n"
				+ "image_normalized = normalize(narr, 1, 99.8, axis=axes_normalize)" + "\n"
				+ "print(\"Image shape:\", image_normalized.shape)" + "\n"
				+ "guessed_tiles = model._guess_n_tiles(image_normalized)" + "\n"
				+ "print(\"Guessed tiling:\", guessed_tiles)" + "\n"
				+ "label_image, details = model.predict_instances(image_normalized, axes='ZYX', n_tiles=guessed_tiles)" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = label_image" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";

		System.out.println( "Script: " + script );

		String content = "name: stardist\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - cudatoolkit=11.2\n"
				+ "  - cudnn=8.1.0\n"
				+ "  - numpy<1.24\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "      - numpy<1.24\n"
				+ "      - tensorflow==2.10\n"
				+ "      - stardist==0.8.5\n"
				+ "      - appose";
		Environment env = Appose.mamba().scheme( "environment.yml" ).content( content ).logDebug().build();
		System.out.println( "Created environment" );

		// Display the first image in a new BDV instance
		BdvStackSource<?> bdvSource1 = BdvFunctions.show(img, "Original Image");

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

			// Add the second image as a second channel in the same BDV instance
			BdvFunctions.show( labelImage, "Segmentation", Bdv.options().addTo( bdvSource1.getBdvHandle() ) );

			System.out.println( "label image size: " + labelImage.size() );
		}
	}
}
